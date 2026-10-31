package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.NetworkPing
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PauseCircleFilled
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.DeviceCategory
import com.example.data.local.DeviceEntity
import com.example.ui.components.BadgeChip
import com.example.ui.components.MonoMetricTag
import com.example.ui.components.StatusPulseDot
import com.example.ui.components.accentColor
import com.example.ui.components.icon
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.PurpleAccent
import com.example.ui.viewmodel.DeviceDiagnosticUiState

@Composable
fun DeviceDetailsScreen(
    device: DeviceEntity,
    gatewayIp: String,
    diagState: DeviceDiagnosticUiState,
    onBack: () -> Unit,
    onSaveCustomization: (String, DeviceCategory, String, String, Boolean, Boolean) -> Unit,
    onToggleBlock: () -> Unit,
    onTogglePause: (String) -> Unit,
    onToggleAlerts: (Boolean) -> Unit,
    onRunDeviceSpeedTest: () -> Unit,
    onRunPortAndPingScan: () -> Unit,
    onTriggerWakeOnLan: (Int) -> Unit,
    onDismissBanner: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    var showEditDialog by remember { mutableStateOf(false) }
    var showPauseScheduleDialog by remember { mutableStateOf(false) }

    val categoryColor = device.category.accentColor()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("device_details_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Bar with Back Button + Customize Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("device_details_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = ElectricCyan
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "تفاصيل وإدارة الجهاز",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${device.ipAddress}  •  ${device.defaultHostname}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = { showEditDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricCyan.copy(alpha = 0.16f),
                        contentColor = ElectricCyan
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("customize_device_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "تخصيص",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تعديل البيانات", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Status Feedback Banner
        diagState.statusBannerMessage?.let { msg ->
            item {
                Surface(
                    color = EmeraldOnline.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, EmeraldOnline.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                        .clickable { onDismissBanner() }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldOnline
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodyMedium,
                            color = EmeraldOnline,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 1. Device Identity & Network Telemetry Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, categoryColor.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(62.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(categoryColor.copy(alpha = 0.16f))
                                .border(1.5.dp, categoryColor.copy(alpha = 0.45f), RoundedCornerShape(18.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = device.category.icon(),
                                contentDescription = device.category.arabicLabel,
                                tint = categoryColor,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = device.customName,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${device.vendor}  •  ${device.category.arabicLabel}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatusPulseDot(
                                    isOnline = device.isOnline,
                                    isPaused = device.isPaused,
                                    isBlocked = device.isBlocked
                                )
                                val stateText = when {
                                    device.isBlocked -> "محظور من الشبكة (Blocked)"
                                    device.isPaused -> "موقوف مؤقتاً (${device.pauseScheduleLabel.ifBlank { "Paused" }})"
                                    device.isOnline -> "متصل الآن • استجابة ${device.latencyMs}ms"
                                    else -> "غير متصل حالياً (Offline)"
                                }
                                Text(
                                    text = stateText,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = when {
                                        device.isBlocked -> CoralDanger
                                        device.isPaused -> AmberWarning
                                        device.isOnline -> EmeraldOnline
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MonoMetricTag(
                            label = "عنوان IP",
                            value = device.ipAddress,
                            accentColor = ElectricCyan,
                            modifier = Modifier.weight(1f)
                        )
                        MonoMetricTag(
                            label = "عنوان MAC",
                            value = device.macAddress,
                            accentColor = EmeraldOnline,
                            modifier = Modifier.weight(1.3f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MonoMetricTag(
                            label = "الموقع / الغرفة",
                            value = device.locationRoom,
                            accentColor = AmberWarning,
                            modifier = Modifier.weight(1f)
                        )
                        MonoMetricTag(
                            label = "حالة الموثوقية",
                            value = if (device.isTrusted) "جهاز موثوق" else "غير موثوق (مشبوه)",
                            accentColor = if (device.isTrusted) EmeraldOnline else CoralDanger,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (device.notes.isNotBlank()) {
                        Text(
                            text = "ملاحظات: ${device.notes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 2. Internet Access Control: Pause Internet / Block Device / Router Admin
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = AmberWarning
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "التحكم بالوصول للإنترنت والحظر (Block & Pause)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "يمكنك إيقاف الإنترنت مؤقتاً عن الجهاز (الرقابة الأبوية / جدول التوقف) أو حظر عنوان MAC الخاص به من الشبكة.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Pause / Resume Button
                        Button(
                            onClick = {
                                if (device.isPaused) {
                                    onTogglePause("")
                                } else {
                                    showPauseScheduleDialog = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (device.isPaused) EmeraldOnline else AmberWarning,
                                contentColor = NavyBackground
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pause_device_button")
                        ) {
                            Icon(
                                imageVector = if (device.isPaused) Icons.Default.PlayCircleFilled else Icons.Default.PauseCircleFilled,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (device.isPaused) "استئناف الإنترنت" else "إيقاف مؤقت (Pause)",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Block / Unblock Button
                        Button(
                            onClick = onToggleBlock,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (device.isBlocked) ElectricCyan else CoralDanger,
                                contentColor = if (device.isBlocked) NavyBackground else Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("block_device_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (device.isBlocked) "إلغاء الحظر" else "حظر الجهاز (Block)",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://$gatewayIp"))
                                context.startActivity(intent)
                            } catch (_: Exception) {
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Launch,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "فتح لوحة إدارة الراوتر ($gatewayIp) لتصفية MAC",
                            color = ElectricCyan
                        )
                    }
                }
            }
        }

        // 3. Connection & Join Alerts + Wake on LAN (WoL) Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Alerts Switch Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = ElectricCyan
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "تنبيهات الاتصال والانضمام",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "إشعار فوري عند دخول الجهاز للشبكة أو انقطاعه عنها",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = device.alertsEnabled,
                            onCheckedChange = onToggleAlerts,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NavyBackground,
                                checkedTrackColor = ElectricCyan
                            ),
                            modifier = Modifier.testTag("device_alerts_switch")
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    // Wake on LAN (WoL) Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PowerSettingsNew,
                                    contentDescription = null,
                                    tint = EmeraldOnline
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "إيقاظ الجهاز عن بُعد (Wake on LAN)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "إرسال حزمة سحرية (Magic Packet UDP:9) لتشغيل الحاسوب أو الخادم عن بُعد",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { onTriggerWakeOnLan(9) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldOnline,
                                contentColor = NavyBackground
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("wake_on_lan_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("إرسال WoL", fontWeight = FontWeight.Bold)
                        }
                    }

                    diagState.lastWolResult?.let { wol ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = wol.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = EmeraldOnline,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Payload: ${wol.packetHexPreview}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    color = ElectricCyan
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Device-Specific Speed & Throughput Test Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = ElectricCyan
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "اختبار سرعة الربط مع هذا الجهاز",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "قياس سرعة تبادل البيانات المحلية (LAN Throughput)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = onRunDeviceSpeedTest,
                            enabled = !diagState.isTestingSpeed,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricCyan,
                                contentColor = NavyBackground
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("device_speed_test_button")
                        ) {
                            Text(
                                text = if (diagState.isTestingSpeed) "جاري القياس..." else "فحص السرعة",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MonoMetricTag(
                            label = "سرعة التنزيل المحلية",
                            value = "${diagState.deviceSpeedMbps} Mbps",
                            accentColor = ElectricCyan,
                            modifier = Modifier.weight(1f)
                        )
                        MonoMetricTag(
                            label = "سرعة الرفع المحلية",
                            value = "${diagState.deviceUploadMbps} Mbps",
                            accentColor = EmeraldOnline,
                            modifier = Modifier.weight(1f)
                        )
                        MonoMetricTag(
                            label = "زمن الاستجابة",
                            value = "${device.latencyMs} ms",
                            accentColor = AmberWarning,
                            modifier = Modifier.weight(0.8f)
                        )
                    }

                    if (diagState.linkQualityText.isNotBlank()) {
                        Text(
                            text = diagState.linkQualityText,
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldOnline
                        )
                    }
                }
            }
        }

        // 5. Device Port Scanner & Live Ping Section
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Radar,
                                contentDescription = null,
                                tint = PurpleAccent
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "فحص المنافذ والـ Ping للجهاز",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "كشف الخدمات النشطة واختبار استقرار الحزم",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = onRunPortAndPingScan,
                            enabled = !diagState.isScanningPorts && !diagState.isPingingDevice,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PurpleAccent,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("device_port_ping_scan_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.NetworkPing,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (diagState.isScanningPorts || diagState.isPingingDevice) "جاري الفحص..." else "فحص شامل",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    AnimatedVisibility(visible = diagState.isScanningPorts || diagState.isPingingDevice) {
                        LinearProgressIndicator(
                            progress = { diagState.portScanProgress.coerceIn(0.05f, 1f) },
                            color = PurpleAccent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }

                    // Live Ping Results for this device
                    diagState.devicePingSummary?.let { summary ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "نتائج Ping (${summary.target}): مرسل ${summary.sent} • مستلم ${summary.received} • الفقد ${summary.lostPercent.toInt()}%",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = EmeraldOnline
                                )
                                Text(
                                    text = "Min: ${summary.minMs}ms  |  Avg: ${summary.avgMs}ms  |  Max: ${summary.maxMs}ms  |  Jitter: ${summary.jitterMs}ms",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    color = ElectricCyan
                                )
                            }
                        }
                    }

                    // Open Ports List
                    val openPorts = diagState.scannedPorts.filter { it.isOpen }
                    Text(
                        text = "المنافذ المفتوحة المكتشفة (${openPorts.size}):",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (openPorts.isEmpty()) {
                        Text(
                            text = "لم يتم رصد منافذ TCP مفتوحة أو اضغط على (فحص شامل) لاختبار جميع المنافذ.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        openPorts.forEach { portItem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    BadgeChip(
                                        text = "TCP/${portItem.port}",
                                        color = if (portItem.isRisky) CoralDanger else ElectricCyan
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = portItem.serviceName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = portItem.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                BadgeChip(
                                    text = "مفتوح (${portItem.responseTimeMs}ms)",
                                    color = if (portItem.isRisky) AmberWarning else EmeraldOnline
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Edit & Customize Device Dialog
    if (showEditDialog) {
        var nameInput by remember(device) { mutableStateOf(device.customName) }
        var selectedCategory by remember(device) { mutableStateOf(device.category) }
        var roomInput by remember(device) { mutableStateOf(device.locationRoom) }
        var notesInput by remember(device) { mutableStateOf(device.notes) }
        var trustedState by remember(device) { mutableStateOf(device.isTrusted) }
        var alertsState by remember(device) { mutableStateOf(device.alertsEnabled) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("تعديل وتخصيص بيانات الجهاز") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("اسم الجهاز") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "نوع الجهاز:",
                        style = MaterialTheme.typography.labelLarge
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(DeviceCategory.entries) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                leadingIcon = {
                                    Icon(
                                        imageVector = cat.icon(),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = { Text(cat.arabicLabel) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = roomInput,
                        onValueChange = { roomInput = it },
                        label = { Text("الموقع / الغرفة (مثال: غرفة المعيشة)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("ملاحظات إضافية") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("جهاز موثوق ومعتمد على الشبكة")
                        Switch(
                            checked = trustedState,
                            onCheckedChange = { trustedState = it }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveCustomization(
                            nameInput.trim(),
                            selectedCategory,
                            roomInput.trim(),
                            notesInput.trim(),
                            trustedState,
                            alertsState
                        )
                        showEditDialog = false
                    }
                ) {
                    Text("حفظ التعديلات")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Pause Schedule Picker Dialog
    if (showPauseScheduleDialog) {
        val schedules = listOf(
            "إيقاف مؤقت لمدة 30 دقيقة",
            "إيقاف مؤقت لمدة ساعة",
            "إيقاف وقت النوم (حتى 7:00 صباحاً)",
            "إيقاف مفتوح حتى الاستئناف يدوياً"
        )
        AlertDialog(
            onDismissRequest = { showPauseScheduleDialog = false },
            title = { Text("إيقاف الإنترنت مؤقتاً عن الجهاز") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "اختر مدة إيقاف الإنترنت عن (${device.customName}):",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    schedules.forEach { label ->
                        Surface(
                            onClick = {
                                onTogglePause(label)
                                showPauseScheduleDialog = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = AmberWarning,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPauseScheduleDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
