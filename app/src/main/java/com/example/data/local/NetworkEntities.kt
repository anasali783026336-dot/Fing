package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DeviceCategory(val arabicLabel: String, val englishLabel: String) {
    ROUTER("راوتر / بوابة", "Router"),
    SMARTPHONE("هاتف ذكي", "Smartphone"),
    LAPTOP("حاسوب محمول", "Laptop"),
    DESKTOP("حاسوب مكتبي / خادم", "Desktop / Server"),
    SMART_TV("تلفاز ذكي", "Smart TV"),
    CAMERA("كاميرا مراقبة", "IP Camera"),
    PRINTER("طابعة شبكية", "Network Printer"),
    CONSOLE("منصة ألعاب", "Game Console"),
    IOT("جهاز ذكي IoT", "Smart IoT");

    companion object {
        fun fromName(name: String): DeviceCategory {
            return entries.find { it.name.equals(name, ignoreCase = true) } ?: IOT
        }
    }
}

@Entity(tableName = "network_devices")
data class DeviceEntity(
    @PrimaryKey val macAddress: String,
    val ipAddress: String,
    val customName: String,
    val defaultHostname: String,
    val vendor: String,
    val categoryName: String,
    val isOnline: Boolean,
    val latencyMs: Int,
    val isGateway: Boolean = false,
    val isCurrentDevice: Boolean = false,
    val isTrusted: Boolean = true,
    val isBlocked: Boolean = false,
    val isPaused: Boolean = false,
    val pauseScheduleLabel: String = "",
    val alertsEnabled: Boolean = false,
    val openPortsCsv: String = "",
    val locationRoom: String = "غير محدد",
    val notes: String = "",
    val lastDownloadMbps: Float = 0f,
    val firstSeenTimestamp: Long = System.currentTimeMillis(),
    val lastSeenTimestamp: Long = System.currentTimeMillis()
) {
    val category: DeviceCategory
        get() = DeviceCategory.fromName(categoryName)

    val openPortsList: List<Int>
        get() = if (openPortsCsv.isBlank()) emptyList()
        else openPortsCsv.split(",").mapNotNull { it.trim().toIntOrNull() }
}

@Entity(tableName = "speed_test_records")
data class SpeedTestRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val downloadMbps: Float,
    val uploadMbps: Float,
    val pingMs: Int,
    val jitterMs: Int,
    val packetLossPercent: Float,
    val ispName: String,
    val ssidName: String,
    val gamingScore: Int,
    val videoCallScore: Int,
    val streamingScore: Int
)

@Entity(tableName = "security_events")
data class SecurityEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val title: String,
    val description: String,
    val severity: String, // HIGH, MEDIUM, LOW, INFO
    val deviceMac: String = "",
    val deviceIp: String = "",
    val recommendation: String = "",
    val isResolved: Boolean = false
)
