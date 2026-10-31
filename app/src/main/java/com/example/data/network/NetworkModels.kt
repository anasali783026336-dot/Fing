package com.example.data.network

import com.example.data.local.DeviceCategory

data class NetworkOverviewInfo(
    val ssid: String = "Wi-Fi Home 5G",
    val bssid: String = "A4:2B:B0:C8:11:01",
    val gatewayIp: String = "192.168.1.1",
    val localIp: String = "192.168.1.105",
    val subnetMask: String = "255.255.255.0",
    val dnsServers: List<String> = listOf("8.8.8.8", "1.1.1.1"),
    val linkSpeedMbps: Int = 866,
    val frequencyMhz: Int = 5180,
    val rssiDbm: Int = -46,
    val signalQualityPercent: Int = 94,
    val encryptionType: String = "WPA3-SAE / WPA2-PSK (AES)",
    val ispName: String = "FiberNet High-Speed Telecom",
    val publicIp: String = "185.72.140.29",
    val isConnected: Boolean = true,
    val connectionTransport: String = "Wi-Fi 6 (802.11ax)"
)

data class PortServiceInfo(
    val port: Int,
    val serviceName: String,
    val arabicDescription: String,
    val isRisky: Boolean = false,
    val riskReason: String = ""
)

data class PortScanItem(
    val port: Int,
    val serviceName: String,
    val description: String,
    val isOpen: Boolean,
    val responseTimeMs: Int,
    val isRisky: Boolean = false
)

data class PingPacketResult(
    val sequence: Int,
    val targetHost: String,
    val resolvedIp: String,
    val timeMs: Int,
    val isSuccess: Boolean,
    val ttl: Int = 64,
    val statusText: String = ""
)

data class PingSummary(
    val target: String,
    val resolvedIp: String,
    val sent: Int,
    val received: Int,
    val lostPercent: Float,
    val minMs: Int,
    val maxMs: Int,
    val avgMs: Int,
    val jitterMs: Int,
    val packets: List<PingPacketResult>
)

data class TracerouteHop(
    val hopNumber: Int,
    val hostOrIp: String,
    val ipAddress: String,
    val rttMs: Int,
    val isDestination: Boolean,
    val isTimeout: Boolean = false,
    val locationHint: String = ""
)

data class DnsRecordItem(
    val type: String, // A, AAAA, PTR, NS, CNAME
    val value: String,
    val ttlSeconds: Int = 300
)

data class DnsLookupResult(
    val domainOrIp: String,
    val canonicalHostname: String,
    val queryTimeMs: Int,
    val dnsServerUsed: String,
    val records: List<DnsRecordItem>,
    val errorMessage: String? = null
)

data class WolResult(
    val success: Boolean,
    val macAddress: String,
    val broadcastIp: String,
    val port: Int,
    val packetHexPreview: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class SpeedTestPhase(val arabicTitle: String) {
    IDLE("جاهز لبدء الفحص"),
    PING_TEST("جاري قياس الاستجابة (Ping & Jitter)..."),
    DOWNLOAD_TEST("جاري فحص سرعة التحميل (Download)..."),
    UPLOAD_TEST("جاري فحص سرعة الرفع (Upload)..."),
    COMPLETED("اكتمل فحص السرعة وجودة الاتصال")
}

data class SpeedTestLiveState(
    val isRunning: Boolean = false,
    val phase: SpeedTestPhase = SpeedTestPhase.IDLE,
    val progress: Float = 0f,
    val currentGaugeMbps: Float = 0f,
    val downloadMbps: Float = 0f,
    val uploadMbps: Float = 0f,
    val pingMs: Int = 0,
    val jitterMs: Int = 0,
    val packetLossPercent: Float = 0f,
    val downloadSamples: List<Float> = emptyList(),
    val uploadSamples: List<Float> = emptyList(),
    val gamingScore: Int = 0,
    val gamingLabel: String = "غير مقاس",
    val videoCallScore: Int = 0,
    val videoCallLabel: String = "غير مقاس",
    val streamingScore: Int = 0,
    val streamingLabel: String = "غير مقاس",
    val serverLocation: String = "Frankfurt / MENA Edge CDN",
    val errorMessage: String? = null
)

data class IspTierBenchmark(
    val tierName: String,
    val avgDownloadMbps: Float,
    val avgUploadMbps: Float,
    val avgPingMs: Int
)

data class VulnerabilityFinding(
    val id: String,
    val title: String,
    val description: String,
    val severity: String, // HIGH, MEDIUM, LOW, SAFE
    val targetDeviceName: String,
    val targetIp: String,
    val recommendation: String,
    val isSafe: Boolean
)

data class SecurityAuditReport(
    val isScanning: Boolean = false,
    val scanProgress: Float = 0f,
    val currentStepLabel: String = "",
    val overallScore: Int = 92,
    val encryptionStatus: String = "آمن (WPA3/WPA2-AES)",
    val encryptionSafe: Boolean = true,
    val routerAdminExposed: Boolean = false,
    val upnpExposed: Boolean = false,
    val dnsSafe: Boolean = true,
    val intruderCount: Int = 0,
    val riskyOpenPortsCount: Int = 0,
    val findings: List<VulnerabilityFinding> = emptyList(),
    val lastAuditTimestamp: Long = System.currentTimeMillis()
)

object NetworkCatalog {
    val commonPorts: List<PortServiceInfo> = listOf(
        PortServiceInfo(21, "FTP", "نقل الملفات غير المشفر", isRisky = true, riskReason = "ينقل كلمات المرور والملفات بدون تشفير"),
        PortServiceInfo(22, "SSH", "الإدارة الطرفية الآمنة", isRisky = false),
        PortServiceInfo(23, "Telnet", "تحكم عن بُعد غير مشفر", isRisky = true, riskReason = "خطر أمني عالٍ، يسهل اعتراض الجلسة"),
        PortServiceInfo(53, "DNS", "خدمة أسماء النطاقات", isRisky = false),
        PortServiceInfo(80, "HTTP", "واجهة الويب / لوحة الراوتر", isRisky = false),
        PortServiceInfo(139, "NetBIOS", "مشاركة ملفات الشبكة المحلية", isRisky = true, riskReason = "قد يكشف أسماء ومجلدات الأجهزة على الشبكة"),
        PortServiceInfo(443, "HTTPS", "اتصال ويب مشفر SSL/TLS", isRisky = false),
        PortServiceInfo(445, "SMB", "مشاركة ملفات وطابعات Windows", isRisky = true, riskReason = "مستهدف بهجمات الشبكة إذا لم يتم تقييده"),
        PortServiceInfo(554, "RTSP", "بث كاميرات المراقبة الحية", isRisky = true, riskReason = "منفذ بث فيديو قد يسمح بالمشاهدة إذا كانت كلمة المرور افتراضية"),
        PortServiceInfo(1900, "UPnP / SSDP", "الاكتشاف التلقائي للأجهزة", isRisky = true, riskReason = "يسمح للتطبيقات بفتح منافذ في الراوتر تلقائياً"),
        PortServiceInfo(3389, "RDP", "سطح المكتب البعيد Windows", isRisky = true, riskReason = "عرضة لمحاولات تخمين كلمة المرور"),
        PortServiceInfo(5000, "UPnP / NAS", "خادم الوسائط والتخزين الشبكي", isRisky = false),
        PortServiceInfo(8008, "Google Cast", "بث الوسائط والتلفاز الذكي", isRisky = false),
        PortServiceInfo(8009, "Cast TLS", "قناة تحكم أجهزة البث الذكية", isRisky = false),
        PortServiceInfo(8080, "HTTP-Proxy", "منفذ ويب بديل / إدارة عن بُعد", isRisky = true, riskReason = "غالباً يستخدم للإدارة الخارجية للراوتر"),
        PortServiceInfo(8443, "HTTPS-Alt", "لوحة تحكم آمنة بديلة", isRisky = false),
        PortServiceInfo(9100, "JetDirect", "الطباعة الشبكية المباشرة RAW", isRisky = false),
        PortServiceInfo(62078, "iDevice Sync", "مزامنة أجهزة Apple عبر Wi-Fi", isRisky = false)
    )

    val ispBenchmarks = listOf(
        IspTierBenchmark("الجيل الخامس / فايبر فائق (5G / FTTH Pro)", 300f, 85f, 11),
        IspTierBenchmark("فايبر منزلي قياسي (Home Fiber 100M)", 100f, 35f, 16),
        IspTierBenchmark("متوسط مزودي الخدمة بالمنطقة (Regional ISP Avg)", 78f, 24f, 23),
        IspTierBenchmark("اتصال ADSL / VDSL تقليدي", 24f, 5f, 42)
    )

    fun lookupPortInfo(port: Int): PortServiceInfo {
        return commonPorts.find { it.port == port }
            ?: PortServiceInfo(port, "TCP/$port", "منفذ خدمة مخصص ($port)", isRisky = false)
    }

    fun inferCategoryFromOpenPorts(ports: List<Int>, isGateway: Boolean, isSelf: Boolean): DeviceCategory {
        if (isGateway) return DeviceCategory.ROUTER
        if (isSelf) return DeviceCategory.SMARTPHONE
        return when {
            9100 in ports -> DeviceCategory.PRINTER
            554 in ports -> DeviceCategory.CAMERA
            8008 in ports || 8009 in ports -> DeviceCategory.SMART_TV
            62078 in ports -> DeviceCategory.SMARTPHONE
            445 in ports || 3389 in ports || 139 in ports -> DeviceCategory.LAPTOP
            22 in ports -> DeviceCategory.DESKTOP
            else -> DeviceCategory.IOT
        }
    }
}
