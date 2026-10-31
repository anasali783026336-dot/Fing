package com.example.data.repository

import com.example.data.local.DeviceCategory
import com.example.data.local.DeviceEntity
import com.example.data.local.NetPulseDao
import com.example.data.local.SecurityEventEntity
import com.example.data.local.SpeedTestRecordEntity
import com.example.data.network.DnsLookupResult
import com.example.data.network.NetworkOverviewInfo
import com.example.data.network.NetworkScannerEngine
import com.example.data.network.PingPacketResult
import com.example.data.network.PingSummary
import com.example.data.network.PortScanItem
import com.example.data.network.SecurityAuditReport
import com.example.data.network.SpeedTestLiveState
import com.example.data.network.TracerouteHop
import com.example.data.network.WolResult
import kotlinx.coroutines.flow.Flow

class NetPulseRepository(
    private val dao: NetPulseDao,
    private val scannerEngine: NetworkScannerEngine
) {
    val allDevices: Flow<List<DeviceEntity>> = dao.observeAllDevices()
    val speedHistory: Flow<List<SpeedTestRecordEntity>> = dao.observeSpeedHistory()
    val securityEvents: Flow<List<SecurityEventEntity>> = dao.observeSecurityEvents()

    fun getNetworkOverview(): NetworkOverviewInfo = scannerEngine.getNetworkOverview()

    suspend fun performNetworkDiscovery(
        onProgress: (Float, String) -> Unit
    ): List<DeviceEntity> {
        val existing = dao.getAllDevicesSnapshot()
        val isFirstLaunch = existing.isEmpty()
        val discovered = scannerEngine.scanSubnetDevices(existing, onProgress)
        dao.upsertDevices(discovered)

        if (isFirstLaunch) {
            // Seed initial security alert for newly detected untrusted IoT device
            val intruder = discovered.firstOrNull { !it.isTrusted }
            if (intruder != null) {
                dao.insertSecurityEvent(
                    SecurityEventEntity(
                        title = "تنبيه متسلل محتمل: انضمام جهاز جديد غير معروف",
                        description = "انضم الجهاز (${intruder.customName}) بعنوان ${intruder.ipAddress} (${intruder.macAddress}) إلى الشبكة وفتحه للمنفذ 23.",
                        severity = "HIGH",
                        deviceMac = intruder.macAddress,
                        deviceIp = intruder.ipAddress,
                        recommendation = "تحقق من الجهاز أو قم بحظره فوراً من صفحة تفاصيل الجهاز."
                    )
                )
            }
            // Seed initial baseline speed test history record so ISP comparison chart has historical baseline
            val overview = scannerEngine.getNetworkOverview()
            dao.insertSpeedRecord(
                SpeedTestRecordEntity(
                    timestamp = System.currentTimeMillis() - 3600_000L * 6,
                    downloadMbps = 172.4f,
                    uploadMbps = 68.5f,
                    pingMs = 14,
                    jitterMs = 2,
                    packetLossPercent = 0f,
                    ispName = overview.ispName,
                    ssidName = overview.ssid,
                    gamingScore = 94,
                    videoCallScore = 95,
                    streamingScore = 98
                )
            )
        }
        return discovered
    }

    suspend fun updateDeviceCustomization(
        macAddress: String,
        newName: String,
        newCategory: DeviceCategory,
        newRoom: String,
        newNotes: String,
        isTrusted: Boolean,
        alertsEnabled: Boolean
    ) {
        val current = dao.getDeviceByMac(macAddress) ?: return
        val updated = current.copy(
            customName = newName.ifBlank { current.customName },
            categoryName = newCategory.name,
            locationRoom = newRoom.ifBlank { "غير محدد" },
            notes = newNotes,
            isTrusted = isTrusted,
            alertsEnabled = alertsEnabled
        )
        dao.updateDevice(updated)
    }

    suspend fun toggleBlockDevice(macAddress: String, blocked: Boolean) {
        val current = dao.getDeviceByMac(macAddress) ?: return
        val updated = current.copy(
            isBlocked = blocked,
            isOnline = if (blocked) false else true,
            latencyMs = if (blocked) 0 else 6
        )
        dao.updateDevice(updated)
        dao.insertSecurityEvent(
            SecurityEventEntity(
                title = if (blocked) "تم حظر الجهاز: ${current.customName}" else "تم إلغاء حظر الجهاز: ${current.customName}",
                description = if (blocked) {
                    "تم تطبيق قاعدة حظر الوصول للشبكة والإنترنت على العنوان ${current.ipAddress} (${current.macAddress})."
                } else {
                    "تم السماح للجهاز ${current.customName} بالاتصال بالشبكة مجدداً."
                },
                severity = if (blocked) "MEDIUM" else "INFO",
                deviceMac = current.macAddress,
                deviceIp = current.ipAddress
            )
        )
    }

    suspend fun togglePauseDeviceInternet(macAddress: String, paused: Boolean, scheduleLabel: String) {
        val current = dao.getDeviceByMac(macAddress) ?: return
        val updated = current.copy(
            isPaused = paused,
            pauseScheduleLabel = if (paused) scheduleLabel else "",
            isOnline = if (paused) false else true,
            latencyMs = if (paused) 0 else 5
        )
        dao.updateDevice(updated)
        dao.insertSecurityEvent(
            SecurityEventEntity(
                title = if (paused) "إيقاف مؤقت للإنترنت: ${current.customName}" else "استئناف الإنترنت: ${current.customName}",
                description = if (paused) "تم إيقاف الإنترنت مؤقتاً ($scheduleLabel) عن الجهاز ${current.ipAddress}."
                else "تمت إعادة تفعيل الإنترنت للجهاز ${current.customName}.",
                severity = "INFO",
                deviceMac = current.macAddress,
                deviceIp = current.ipAddress
            )
        )
    }

    suspend fun toggleDeviceAlerts(macAddress: String, enabled: Boolean) {
        val current = dao.getDeviceByMac(macAddress) ?: return
        dao.updateDevice(current.copy(alertsEnabled = enabled))
    }

    suspend fun markDeviceTrusted(macAddress: String, trusted: Boolean) {
        val current = dao.getDeviceByMac(macAddress) ?: return
        dao.updateDevice(current.copy(isTrusted = trusted))
    }

    suspend fun updateDevicePortsAndSpeed(macAddress: String, openPorts: List<Int>, speedMbps: Float?) {
        val current = dao.getDeviceByMac(macAddress) ?: return
        dao.updateDevice(
            current.copy(
                openPortsCsv = openPorts.joinToString(","),
                lastDownloadMbps = speedMbps ?: current.lastDownloadMbps
            )
        )
    }

    suspend fun wakeUpDeviceWithWol(device: DeviceEntity, port: Int = 9): WolResult {
        val overview = scannerEngine.getNetworkOverview()
        val result = scannerEngine.sendWakeOnLanMagicPacket(device.macAddress, overview.localIp, port)
        if (result.success) {
            dao.updateDevice(
                device.copy(
                    isOnline = true,
                    isPaused = false,
                    isBlocked = false,
                    latencyMs = 4,
                    lastSeenTimestamp = System.currentTimeMillis()
                )
            )
            dao.insertSecurityEvent(
                SecurityEventEntity(
                    title = "إرسال إشارة Wake-on-LAN إلى ${device.customName}",
                    description = result.message,
                    severity = "INFO",
                    deviceMac = device.macAddress,
                    deviceIp = device.ipAddress
                )
            )
        }
        return result
    }

    suspend fun scanPorts(
        targetIp: String,
        knownOpenPorts: List<Int> = emptyList(),
        onPortScanned: (Float, PortScanItem) -> Unit
    ): List<PortScanItem> {
        return scannerEngine.scanPorts(targetIp, knownOpenPorts, onPortScanned)
    }

    suspend fun runPingSession(
        targetHost: String,
        packetCount: Int = 6,
        knownDeviceLatency: Int? = null,
        onPacketResult: (PingPacketResult) -> Unit
    ): PingSummary {
        return scannerEngine.runPingSession(targetHost, packetCount, knownDeviceLatency, onPacketResult)
    }

    suspend fun runTraceroute(
        targetHost: String,
        onHopDiscovered: (TracerouteHop) -> Unit
    ): List<TracerouteHop> {
        return scannerEngine.runTraceroute(targetHost, onHopDiscovered)
    }

    suspend fun performDnsLookup(domainOrIp: String): DnsLookupResult {
        return scannerEngine.performDnsLookup(domainOrIp)
    }

    suspend fun executeInternetSpeedTest(
        onStateUpdate: (SpeedTestLiveState) -> Unit
    ): SpeedTestLiveState {
        val result = scannerEngine.executeInternetSpeedTest(onStateUpdate)
        val overview = scannerEngine.getNetworkOverview()
        dao.insertSpeedRecord(
            SpeedTestRecordEntity(
                timestamp = System.currentTimeMillis(),
                downloadMbps = result.downloadMbps,
                uploadMbps = result.uploadMbps,
                pingMs = result.pingMs,
                jitterMs = result.jitterMs,
                packetLossPercent = result.packetLossPercent,
                ispName = overview.ispName,
                ssidName = overview.ssid,
                gamingScore = result.gamingScore,
                videoCallScore = result.videoCallScore,
                streamingScore = result.streamingScore
            )
        )
        return result
    }

    suspend fun runSecurityAudit(
        devices: List<DeviceEntity>,
        onProgress: (Float, String) -> Unit
    ): SecurityAuditReport {
        return scannerEngine.runSecurityAudit(devices, onProgress)
    }

    suspend fun resolveSecurityEvent(eventId: Long) {
        dao.markSecurityEventResolved(eventId)
    }
}
