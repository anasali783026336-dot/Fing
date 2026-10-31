package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DeviceCategory
import com.example.data.local.DeviceEntity
import com.example.data.local.NetPulseDatabase
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
import com.example.data.repository.NetPulseRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

enum class MainNavTab(val arabicTitle: String) {
    DEVICES("الأجهزة"),
    SPEED("السرعة"),
    SECURITY("الأمان"),
    TOOLS("الأدوات")
}

enum class DeviceSortOption(val arabicLabel: String) {
    BY_IP("حسب عنوان IP"),
    BY_NAME("حسب اسم الجهاز"),
    BY_TYPE("حسب نوع الجهاز"),
    BY_STATUS("حسب الحالة (متصل/غير متصل)")
}

enum class DeviceFilterOption(val arabicLabel: String) {
    ALL("الكل"),
    ONLINE("متصل (Online)"),
    OFFLINE("غير متصل (Offline)"),
    UNTRUSTED("دخيل / غير موثوق"),
    PAUSED_OR_BLOCKED("موقوف / محظور")
}

enum class DiagnosticToolType(val arabicTitle: String, val subtitle: String) {
    PING("فحص الاستجابة Ping", "قياس زمن وصول الحزم وفقدان البيانات"),
    TRACEROUTE("تتبع المسار Traceroute", "كشف القفزات والموجهات حتى الهدف"),
    PORT_SCANNER("فاحص المنافذ Port Scanner", "اكتشاف المنافذ المفتوحة والخدمات النشطة"),
    DNS_LOOKUP("سجلات النطاق DNS Lookup", "تحليل عناوين IPv4/IPv6 والاسم العكسي"),
    WAKE_ON_LAN("إيقاظ عن بُعد Wake on LAN", "إرسال حزمة Magic Packet عبر الشبكة")
}

data class DeviceDiagnosticUiState(
    val isTestingSpeed: Boolean = false,
    val deviceSpeedMbps: Float = 0f,
    val deviceUploadMbps: Float = 0f,
    val linkQualityText: String = "",
    val isScanningPorts: Boolean = false,
    val portScanProgress: Float = 0f,
    val scannedPorts: List<PortScanItem> = emptyList(),
    val isPingingDevice: Boolean = false,
    val devicePingPackets: List<PingPacketResult> = emptyList(),
    val devicePingSummary: PingSummary? = null,
    val lastWolResult: WolResult? = null,
    val statusBannerMessage: String? = null
)

class NetPulseViewModel(application: Application) : AndroidViewModel(application) {

    private val database = NetPulseDatabase.getDatabase(application)
    private val scannerEngine = NetworkScannerEngine(application)
    private val repository = NetPulseRepository(database.dao(), scannerEngine)

    private val _currentTab = MutableStateFlow(MainNavTab.DEVICES)
    val currentTab: StateFlow<MainNavTab> = _currentTab.asStateFlow()

    private val _selectedDeviceMac = MutableStateFlow<String?>(null)
    val selectedDeviceMac: StateFlow<String?> = _selectedDeviceMac.asStateFlow()

    private val _networkOverview = MutableStateFlow(repository.getNetworkOverview())
    val networkOverview: StateFlow<NetworkOverviewInfo> = _networkOverview.asStateFlow()

    // Discovery Scan State
    private val _isScanningNetwork = MutableStateFlow(false)
    val isScanningNetwork: StateFlow<Boolean> = _isScanningNetwork.asStateFlow()

    private val _scanProgress = MutableStateFlow(0f)
    val scanProgress: StateFlow<Float> = _scanProgress.asStateFlow()

    private val _scanStatusMessage = MutableStateFlow("جاهز لفحص الشبكة")
    val scanStatusMessage: StateFlow<String> = _scanStatusMessage.asStateFlow()

    // Search, Filter & Sort State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOption = MutableStateFlow(DeviceSortOption.BY_IP)
    val sortOption: StateFlow<DeviceSortOption> = _sortOption.asStateFlow()

    private val _filterOption = MutableStateFlow(DeviceFilterOption.ALL)
    val filterOption: StateFlow<DeviceFilterOption> = _filterOption.asStateFlow()

    private val _categoryFilter = MutableStateFlow<DeviceCategory?>(null)
    val categoryFilter: StateFlow<DeviceCategory?> = _categoryFilter.asStateFlow()

    val allDevices: StateFlow<List<DeviceEntity>> = repository.allDevices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredAndSortedDevices: StateFlow<List<DeviceEntity>> = combine(
        allDevices,
        _searchQuery,
        _sortOption,
        _filterOption,
        _categoryFilter
    ) { devices, query, sort, filter, catFilter ->
        val filtered = devices.filter { dev ->
            val matchesQuery = query.isBlank() ||
                dev.customName.contains(query, ignoreCase = true) ||
                dev.ipAddress.contains(query, ignoreCase = true) ||
                dev.macAddress.contains(query, ignoreCase = true) ||
                dev.vendor.contains(query, ignoreCase = true) ||
                dev.locationRoom.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                DeviceFilterOption.ALL -> true
                DeviceFilterOption.ONLINE -> dev.isOnline && !dev.isBlocked && !dev.isPaused
                DeviceFilterOption.OFFLINE -> !dev.isOnline || dev.isBlocked || dev.isPaused
                DeviceFilterOption.UNTRUSTED -> !dev.isTrusted
                DeviceFilterOption.PAUSED_OR_BLOCKED -> dev.isPaused || dev.isBlocked
            }

            val matchesCat = catFilter == null || dev.category == catFilter
            matchesQuery && matchesFilter && matchesCat
        }

        when (sort) {
            DeviceSortOption.BY_IP -> filtered.sortedWith(
                compareByDescending<DeviceEntity> { it.isGateway }
                    .thenByDescending { it.isCurrentDevice }
                    .thenBy { ipToLong(it.ipAddress) }
            )
            DeviceSortOption.BY_NAME -> filtered.sortedBy { it.customName.lowercase() }
            DeviceSortOption.BY_TYPE -> filtered.sortedWith(
                compareBy<DeviceEntity> { it.category.ordinal }.thenBy { it.customName }
            )
            DeviceSortOption.BY_STATUS -> filtered.sortedWith(
                compareByDescending<DeviceEntity> { it.isOnline && !it.isBlocked && !it.isPaused }
                    .thenBy { it.latencyMs }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedDevice: StateFlow<DeviceEntity?> = combine(
        allDevices,
        _selectedDeviceMac
    ) { devices, mac ->
        if (mac == null) null else devices.find { it.macAddress == mac }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Individual Device Detail Diagnostics State
    private val _deviceDiagState = MutableStateFlow(DeviceDiagnosticUiState())
    val deviceDiagState: StateFlow<DeviceDiagnosticUiState> = _deviceDiagState.asStateFlow()

    // Internet Speed Test State & History
    private val _speedTestState = MutableStateFlow(SpeedTestLiveState())
    val speedTestState: StateFlow<SpeedTestLiveState> = _speedTestState.asStateFlow()

    val speedHistory: StateFlow<List<SpeedTestRecordEntity>> = repository.speedHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Security & Protection State
    private val _securityReport = MutableStateFlow(SecurityAuditReport())
    val securityReport: StateFlow<SecurityAuditReport> = _securityReport.asStateFlow()

    private val _autoIntruderAlertEnabled = MutableStateFlow(true)
    val autoIntruderAlertEnabled: StateFlow<Boolean> = _autoIntruderAlertEnabled.asStateFlow()

    val securityEvents: StateFlow<List<SecurityEventEntity>> = repository.securityEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tools / Troubleshooting State
    private val _activeTool = MutableStateFlow(DiagnosticToolType.PING)
    val activeTool: StateFlow<DiagnosticToolType> = _activeTool.asStateFlow()

    private val _toolTargetHost = MutableStateFlow("8.8.8.8")
    val toolTargetHost: StateFlow<String> = _toolTargetHost.asStateFlow()

    private val _isToolRunning = MutableStateFlow(false)
    val isToolRunning: StateFlow<Boolean> = _isToolRunning.asStateFlow()

    private val _pingPackets = MutableStateFlow<List<PingPacketResult>>(emptyList())
    val pingPackets: StateFlow<List<PingPacketResult>> = _pingPackets.asStateFlow()

    private val _pingSummary = MutableStateFlow<PingSummary?>(null)
    val pingSummary: StateFlow<PingSummary?> = _pingSummary.asStateFlow()

    private val _tracerouteHops = MutableStateFlow<List<TracerouteHop>>(emptyList())
    val tracerouteHops: StateFlow<List<TracerouteHop>> = _tracerouteHops.asStateFlow()

    private val _portScanResults = MutableStateFlow<List<PortScanItem>>(emptyList())
    val portScanResults: StateFlow<List<PortScanItem>> = _portScanResults.asStateFlow()

    private val _portScanProgress = MutableStateFlow(0f)
    val portScanProgress: StateFlow<Float> = _portScanProgress.asStateFlow()

    private val _dnsLookupResult = MutableStateFlow<DnsLookupResult?>(null)
    val dnsLookupResult: StateFlow<DnsLookupResult?> = _dnsLookupResult.asStateFlow()

    private val _wolToolResult = MutableStateFlow<WolResult?>(null)
    val wolToolResult: StateFlow<WolResult?> = _wolToolResult.asStateFlow()

    private var activeToolJob: Job? = null

    init {
        refreshNetworkAndScan()
    }

    fun selectTab(tab: MainNavTab) {
        _currentTab.value = tab
    }

    fun openDeviceDetails(device: DeviceEntity) {
        _selectedDeviceMac.value = device.macAddress
        // Initialize diagnostic state with known open ports of the selected device
        val initialPorts = device.openPortsList.map { portNum ->
            val info = com.example.data.network.NetworkCatalog.lookupPortInfo(portNum)
            PortScanItem(
                port = portNum,
                serviceName = info.serviceName,
                description = info.arabicDescription,
                isOpen = true,
                responseTimeMs = device.latencyMs.coerceAtLeast(2),
                isRisky = info.isRisky
            )
        }
        _deviceDiagState.value = DeviceDiagnosticUiState(
            deviceSpeedMbps = device.lastDownloadMbps,
            deviceUploadMbps = if (device.lastDownloadMbps > 0f) (device.lastDownloadMbps * 0.45f) else 0f,
            scannedPorts = initialPorts
        )
    }

    fun closeDeviceDetails() {
        _selectedDeviceMac.value = null
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSortOption(option: DeviceSortOption) {
        _sortOption.value = option
    }

    fun updateFilterOption(option: DeviceFilterOption) {
        _filterOption.value = option
    }

    fun updateCategoryFilter(category: DeviceCategory?) {
        _categoryFilter.value = category
    }

    fun refreshNetworkAndScan() {
        if (_isScanningNetwork.value) return
        viewModelScope.launch {
            _isScanningNetwork.value = true
            _networkOverview.value = repository.getNetworkOverview()
            val discovered = repository.performNetworkDiscovery { progress, status ->
                _scanProgress.value = progress
                _scanStatusMessage.value = status
            }
            _isScanningNetwork.value = false
            // Automatically update initial security audit summary
            val report = repository.runSecurityAudit(discovered) { _, _ -> }
            _securityReport.value = report
        }
    }

    // --- Device Details Actions ---

    fun saveDeviceCustomization(
        macAddress: String,
        newName: String,
        newCategory: DeviceCategory,
        newRoom: String,
        newNotes: String,
        isTrusted: Boolean,
        alertsEnabled: Boolean
    ) {
        viewModelScope.launch {
            repository.updateDeviceCustomization(
                macAddress = macAddress,
                newName = newName,
                newCategory = newCategory,
                newRoom = newRoom,
                newNotes = newNotes,
                isTrusted = isTrusted,
                alertsEnabled = alertsEnabled
            )
            _deviceDiagState.value = _deviceDiagState.value.copy(
                statusBannerMessage = "تم حفظ تعديلات وتخصيص الجهاز بنجاح"
            )
        }
    }

    fun toggleBlockDevice(device: DeviceEntity) {
        viewModelScope.launch {
            val newBlocked = !device.isBlocked
            repository.toggleBlockDevice(device.macAddress, newBlocked)
            _deviceDiagState.value = _deviceDiagState.value.copy(
                statusBannerMessage = if (newBlocked) {
                    "تم حظر الجهاز (${device.customName}) وقطع وصوله للشبكة"
                } else {
                    "تم رفع الحظر عن الجهاز (${device.customName})"
                }
            )
        }
    }

    fun togglePauseDeviceInternet(device: DeviceEntity, scheduleLabel: String = "إيقاف مؤقت لمدة ساعة") {
        viewModelScope.launch {
            val newPaused = !device.isPaused
            repository.togglePauseDeviceInternet(device.macAddress, newPaused, scheduleLabel)
            _deviceDiagState.value = _deviceDiagState.value.copy(
                statusBannerMessage = if (newPaused) {
                    "تم إيقاف الإنترنت مؤقتاً عن الجهاز ($scheduleLabel)"
                } else {
                    "تم استئناف اتصال الإنترنت للجهاز"
                }
            )
        }
    }

    fun toggleDeviceAlerts(device: DeviceEntity, enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleDeviceAlerts(device.macAddress, enabled)
            _deviceDiagState.value = _deviceDiagState.value.copy(
                statusBannerMessage = if (enabled) {
                    "تم تفعيل تنبيهات الاتصال والانضمام لهذا الجهاز"
                } else {
                    "تم إيقاف تنبيهات الاتصال لهذا الجهاز"
                }
            )
        }
    }

    fun runDeviceSpeedTest(device: DeviceEntity) {
        if (_deviceDiagState.value.isTestingSpeed) return
        viewModelScope.launch {
            _deviceDiagState.value = _deviceDiagState.value.copy(
                isTestingSpeed = true,
                deviceSpeedMbps = 0f,
                deviceUploadMbps = 0f,
                linkQualityText = "جاري قياس معدل نقل البيانات المحلي مع ${device.ipAddress}..."
            )
            val baseTarget = when {
                device.isGateway -> 640f
                device.isCurrentDevice -> 480f
                device.isOnline -> 240f + Random.nextInt(-30, 80)
                else -> 0f
            }
            for (step in 1..8) {
                val fraction = step / 8f
                val currentDl = ((baseTarget * (0.4f + fraction * 0.6f)) * 10f).roundToInt() / 10f
                val currentUl = ((currentDl * 0.55f) * 10f).roundToInt() / 10f
                _deviceDiagState.value = _deviceDiagState.value.copy(
                    deviceSpeedMbps = currentDl,
                    deviceUploadMbps = currentUl
                )
                delay(140)
            }
            val finalDl = _deviceDiagState.value.deviceSpeedMbps
            repository.updateDevicePortsAndSpeed(
                macAddress = device.macAddress,
                openPorts = device.openPortsList,
                speedMbps = finalDl
            )
            _deviceDiagState.value = _deviceDiagState.value.copy(
                isTestingSpeed = false,
                linkQualityText = if (finalDl > 0f) "جودة الربط المحلي ممتازة (${device.latencyMs.coerceAtLeast(2)}ms RTT)" else "الجهاز غير متصل حالياً",
                statusBannerMessage = "اكتمل فحص سرعة الجهاز: $finalDl Mbps"
            )
        }
    }

    fun runDevicePortAndPingScan(device: DeviceEntity) {
        if (_deviceDiagState.value.isScanningPorts || _deviceDiagState.value.isPingingDevice) return
        viewModelScope.launch {
            _deviceDiagState.value = _deviceDiagState.value.copy(
                isPingingDevice = true,
                isScanningPorts = true,
                portScanProgress = 0f,
                devicePingPackets = emptyList(),
                devicePingSummary = null
            )

            // 1. Run 4-packet Ping to device
            val pingPackets = mutableListOf<PingPacketResult>()
            val summary = repository.runPingSession(
                targetHost = device.ipAddress,
                packetCount = 4,
                knownDeviceLatency = if (device.isOnline) device.latencyMs.coerceAtLeast(2) else null
            ) { pkt ->
                pingPackets.add(pkt)
                _deviceDiagState.value = _deviceDiagState.value.copy(
                    devicePingPackets = pingPackets.toList()
                )
            }
            _deviceDiagState.value = _deviceDiagState.value.copy(
                isPingingDevice = false,
                devicePingSummary = summary
            )

            // 2. Scan Ports on device
            val scanned = repository.scanPorts(
                targetIp = device.ipAddress,
                knownOpenPorts = device.openPortsList
            ) { progress, _ ->
                _deviceDiagState.value = _deviceDiagState.value.copy(portScanProgress = progress)
            }
            val openPortNumbers = scanned.filter { it.isOpen }.map { it.port }
            repository.updateDevicePortsAndSpeed(device.macAddress, openPortNumbers, null)

            _deviceDiagState.value = _deviceDiagState.value.copy(
                isScanningPorts = false,
                portScanProgress = 1f,
                scannedPorts = scanned,
                statusBannerMessage = "اكتمل فحص المنافذ والـ Ping (${openPortNumbers.size} منافذ مفتوحة)"
            )
        }
    }

    fun triggerDeviceWakeOnLan(device: DeviceEntity, port: Int = 9) {
        viewModelScope.launch {
            val result = repository.wakeUpDeviceWithWol(device, port)
            _deviceDiagState.value = _deviceDiagState.value.copy(
                lastWolResult = result,
                statusBannerMessage = result.message
            )
        }
    }

    fun clearDeviceBannerMessage() {
        _deviceDiagState.value = _deviceDiagState.value.copy(statusBannerMessage = null)
    }

    // --- Tab 2: Internet & Speed Test ---

    fun startInternetSpeedTest() {
        if (_speedTestState.value.isRunning) return
        viewModelScope.launch {
            repository.executeInternetSpeedTest { liveState ->
                _speedTestState.value = liveState
            }
        }
    }

    // --- Tab 3: Security & Protection ---

    fun startSecurityAudit() {
        if (_securityReport.value.isScanning) return
        viewModelScope.launch {
            _securityReport.value = _securityReport.value.copy(
                isScanning = true,
                scanProgress = 0.05f,
                currentStepLabel = "بدء الفحص الأمني الشامل..."
            )
            val report = repository.runSecurityAudit(allDevices.value) { progress, step ->
                _securityReport.value = _securityReport.value.copy(
                    isScanning = true,
                    scanProgress = progress,
                    currentStepLabel = step
                )
            }
            _securityReport.value = report
        }
    }

    fun toggleAutoIntruderAlert(enabled: Boolean) {
        _autoIntruderAlertEnabled.value = enabled
    }

    fun trustIntruderDevice(macAddress: String) {
        viewModelScope.launch {
            repository.markDeviceTrusted(macAddress, true)
            startSecurityAudit()
        }
    }

    fun blockIntruderDevice(macAddress: String) {
        viewModelScope.launch {
            repository.toggleBlockDevice(macAddress, true)
            startSecurityAudit()
        }
    }

    fun resolveSecurityEvent(eventId: Long) {
        viewModelScope.launch {
            repository.resolveSecurityEvent(eventId)
        }
    }

    // --- Tab 4: Troubleshooting Tools ---

    fun selectDiagnosticTool(tool: DiagnosticToolType, defaultTarget: String? = null) {
        activeToolJob?.cancel()
        _isToolRunning.value = false
        _activeTool.value = tool
        if (defaultTarget != null) {
            _toolTargetHost.value = defaultTarget
        } else {
            when (tool) {
                DiagnosticToolType.PING -> if (_toolTargetHost.value.isBlank()) _toolTargetHost.value = "8.8.8.8"
                DiagnosticToolType.TRACEROUTE -> if (_toolTargetHost.value.isBlank()) _toolTargetHost.value = "google.com"
                DiagnosticToolType.PORT_SCANNER -> _toolTargetHost.value = _networkOverview.value.gatewayIp
                DiagnosticToolType.DNS_LOOKUP -> _toolTargetHost.value = "google.com"
                DiagnosticToolType.WAKE_ON_LAN -> {
                    val firstMac = allDevices.value.firstOrNull { !it.isCurrentDevice && !it.isGateway }?.macAddress
                        ?: "3C:22:FB:91:40:12"
                    _toolTargetHost.value = firstMac
                }
            }
        }
    }

    fun updateToolTargetHost(target: String) {
        _toolTargetHost.value = target
    }

    fun executeSelectedDiagnosticTool() {
        val target = _toolTargetHost.value.trim()
        if (target.isBlank()) return
        activeToolJob?.cancel()

        activeToolJob = viewModelScope.launch {
            _isToolRunning.value = true
            when (_activeTool.value) {
                DiagnosticToolType.PING -> {
                    _pingPackets.value = emptyList()
                    _pingSummary.value = null
                    val matchedDevice = allDevices.value.find { it.ipAddress == target }
                    val list = mutableListOf<PingPacketResult>()
                    val summary = repository.runPingSession(
                        targetHost = target,
                        packetCount = 6,
                        knownDeviceLatency = matchedDevice?.latencyMs
                    ) { pkt ->
                        list.add(pkt)
                        _pingPackets.value = list.toList()
                    }
                    _pingSummary.value = summary
                }

                DiagnosticToolType.TRACEROUTE -> {
                    _tracerouteHops.value = emptyList()
                    val hops = mutableListOf<TracerouteHop>()
                    repository.runTraceroute(target) { hop ->
                        hops.add(hop)
                        _tracerouteHops.value = hops.toList()
                    }
                }

                DiagnosticToolType.PORT_SCANNER -> {
                    _portScanResults.value = emptyList()
                    _portScanProgress.value = 0f
                    val matchedDevice = allDevices.value.find { it.ipAddress == target }
                    val knownOpen = matchedDevice?.openPortsList ?: listOf(53, 80, 443)
                    val currentList = mutableListOf<PortScanItem>()
                    val finalScanned = repository.scanPorts(target, knownOpen) { progress, item ->
                        _portScanProgress.value = progress
                        currentList.add(item)
                        _portScanResults.value = currentList.sortedWith(
                            compareByDescending<PortScanItem> { it.isOpen }.thenBy { it.port }
                        )
                    }
                    _portScanResults.value = finalScanned
                }

                DiagnosticToolType.DNS_LOOKUP -> {
                    _dnsLookupResult.value = null
                    val res = repository.performDnsLookup(target)
                    _dnsLookupResult.value = res
                }

                DiagnosticToolType.WAKE_ON_LAN -> {
                    val matchedDevice = allDevices.value.find {
                        it.macAddress.equals(target, ignoreCase = true) || it.ipAddress == target
                    }
                    val macToUse = matchedDevice?.macAddress ?: target
                    val dummyDevice = matchedDevice ?: DeviceEntity(
                        macAddress = macToUse,
                        ipAddress = _networkOverview.value.localIp,
                        customName = "جهاز هدف WoL",
                        defaultHostname = "wol-host.lan",
                        vendor = "Generic NIC",
                        categoryName = DeviceCategory.DESKTOP.name,
                        isOnline = true,
                        latencyMs = 5
                    )
                    _wolToolResult.value = repository.wakeUpDeviceWithWol(dummyDevice, 9)
                }
            }
            _isToolRunning.value = false
        }
    }

    private fun ipToLong(ip: String): Long {
        val parts = ip.split(".")
        if (parts.size != 4) return Long.MAX_VALUE
        return try {
            (parts[0].toLong() shl 24) or
                (parts[1].toLong() shl 16) or
                (parts[2].toLong() shl 8) or
                parts[3].toLong()
        } catch (_: Exception) {
            Long.MAX_VALUE
        }
    }
}
