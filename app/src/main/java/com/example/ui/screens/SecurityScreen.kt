package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.GppMaybe
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.DeviceEntity
import com.example.data.local.SecurityEventEntity
import com.example.data.network.NetworkOverviewInfo
import com.example.data.network.SecurityAuditReport
import com.example.data.network.VulnerabilityFinding
import com.example.ui.components.BadgeChip
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyCardElevated

@Composable
fun SecurityScreen(
    overview: NetworkOverviewInfo,
    auditReport: SecurityAuditReport,
    devices: List<DeviceEntity>,
    securityEvents: List<SecurityEventEntity>,
    autoIntruderAlertEnabled: Boolean,
    onStartSecurityAudit: () -> Unit,
    onToggleAutoIntruderAlert: (Boolean) -> Unit,
    onTrustDevice: (String) -> Unit,
    onBlockDevice: (String) -> Unit,
    onInspectDevice: (DeviceEntity) -> Unit
) {
    val untrustedDevices = devices.filter { !it.isTrusted && !it.isBlocked }
    val scoreColor = when {
        auditReport.overallScore >= 85 -> EmeraldOnline
        auditReport.overallScore >= 70 -> AmberWarning
        else -> CoralDanger
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("security_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Security Shield Hero Card (Score + Audit Button)
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        Brush.linearGradient(listOf(scoreColor.copy(alpha = 0.65f), ElectricCyan.copy(alpha = 0.35f))),
                        RoundedCornerShape(24.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = scoreColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "درع الحماية والفحص الأمني",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "فحص ثغرات الراوتر (${overview.gatewayIp})، تشفير الواي فاي، وكشف المتسللين",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Circular Score Indicator
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(scoreColor.copy(alpha = 0.14f))
                                .border(2.dp, scoreColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${auditReport.overallScore}%",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = scoreColor
                                )
                                Text(
                                    text = "الأمان",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = auditReport.isScanning) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            LinearProgressIndicator(
                                progress = { auditReport.scanProgress },
                                color = ElectricCyan,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                            Text(
                                text = auditReport.currentStepLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = ElectricCyan
                            )
                        }
                    }

                    Button(
                        onClick = onStartSecurityAudit,
                        enabled = !auditReport.isScanning,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = NavyBackground
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("start_security_audit_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Policy,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (auditReport.isScanning) "جاري فحص الثغرات..." else "فحص الثغرات الأمنية للراوتر والأجهزة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. Wi-Fi Security & Encryption Matrix Card
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
                    Text(
                        text = "اختبار أمان الواي فاي ونوع التشفير",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    SecurityCheckRow(
                        icon = Icons.Default.Lock,
                        title = "نوع تشفير الشبكة (${overview.ssid})",
                        value = overview.encryptionType,
                        statusText = "تشفير قوي",
                        isSafe = true
                    )
                    SecurityCheckRow(
                        icon = Icons.Default.Router,
                        title = "حماية لوحة تحكم الراوتر (${overview.gatewayIp})",
                        value = "منافذ الإدارة الخارجية (Telnet 23 / WAN) مغلقة",
                        statusText = "محمي",
                        isSafe = !auditReport.routerAdminExposed
                    )
                    SecurityCheckRow(
                        icon = Icons.Default.Dns,
                        title = "أمان خادم النطاقات (DNS Security)",
                        value = "DNS الأساسي: ${overview.dnsServers.firstOrNull() ?: "8.8.8.8"} (بدون اختطاف)",
                        statusText = "سليم",
                        isSafe = auditReport.dnsSafe
                    )
                }
            }
        }

        // 3. Intruder Detection & Auto-Alert Guard Section
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = if (untrustedDevices.isNotEmpty()) CoralDanger.copy(alpha = 0.5f)
                        else EmeraldOnline.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(20.dp)
                    )
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
                                    text = "كاشف المتسللين والتنبيه التلقائي",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "تنبيه المستخدم تلقائياً فور دخول أي جهاز جديد للشبكة",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = autoIntruderAlertEnabled,
                            onCheckedChange = onToggleAutoIntruderAlert,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NavyBackground,
                                checkedTrackColor = ElectricCyan
                            ),
                            modifier = Modifier.testTag("intruder_auto_alert_switch")
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    if (untrustedDevices.isEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = EmeraldOnline
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "جميع الأجهزة المتصلة حالياً موثوقة ومعتمدة لديك.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = EmeraldOnline,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        Text(
                            text = "أجهزة مشبوهة / غير موثوقة (${untrustedDevices.size}):",
                            style = MaterialTheme.typography.titleSmall,
                            color = CoralDanger,
                            fontWeight = FontWeight.Bold
                        )

                        untrustedDevices.forEach { intruder ->
                            Surface(
                                color = CoralDanger.copy(alpha = 0.10f),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, CoralDanger.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = intruder.customName,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = CoralDanger
                                            )
                                            Text(
                                                text = "IP: ${intruder.ipAddress}  •  MAC: ${intruder.macAddress}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontFamily = JetBrainsMonoFontFamily
                                            )
                                            Text(
                                                text = "المصنّع: ${intruder.vendor}  •  منافذ مفتوحة: ${intruder.openPortsCsv.ifBlank { "لا يوجد" }}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        BadgeChip(text = "دخيل محتمل", color = CoralDanger)
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { onBlockDevice(intruder.macAddress) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = CoralDanger,
                                                contentColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("block_intruder_${intruder.macAddress}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Block,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("حظر فوري", fontWeight = FontWeight.Bold)
                                        }

                                        OutlinedButton(
                                            onClick = { onTrustDevice(intruder.macAddress) },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("trust_intruder_${intruder.macAddress}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = EmeraldOnline,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("اعتماد كموثوق", color = EmeraldOnline)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Detailed Vulnerability Findings List
        item {
            Text(
                text = "نتائج فحص الثغرات للأجهزة والراوتر (${auditReport.findings.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        items(
            items = auditReport.findings,
            key = { it.id }
        ) { finding ->
            VulnerabilityCard(finding = finding)
        }

        // 5. Security Event Log
        if (securityEvents.isNotEmpty()) {
            item {
                Text(
                    text = "سجل تنبيهات المراقبة الأمنية",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            items(
                items = securityEvents.take(6),
                key = { it.id }
            ) { event ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val color = when (event.severity) {
                            "HIGH" -> CoralDanger
                            "MEDIUM" -> AmberWarning
                            else -> ElectricCyan
                        }
                        Icon(
                            imageVector = if (event.severity == "HIGH") Icons.Default.WarningAmber else Icons.Default.Security,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = event.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = event.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SecurityCheckRow(
    icon: ImageVector,
    title: String,
    value: String,
    statusText: String,
    isSafe: Boolean
) {
    val color = if (isSafe) EmeraldOnline else CoralDanger
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ElectricCyan,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        BadgeChip(text = statusText, color = color)
    }
}

@Composable
private fun VulnerabilityCard(finding: VulnerabilityFinding) {
    val (color, icon, badgeLabel) = when (finding.severity) {
        "HIGH" -> Triple(CoralDanger, Icons.Default.GppBad, "خطورة عالية")
        "MEDIUM" -> Triple(AmberWarning, Icons.Default.GppMaybe, "تحذير متوسط")
        "LOW" -> Triple(ElectricCyan, Icons.Default.GppMaybe, "تنبيه منخفض")
        else -> Triple(EmeraldOnline, Icons.Default.GppGood, "آمن")
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = finding.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                BadgeChip(text = badgeLabel, color = color)
            }

            Text(
                text = finding.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "التوصية: ${finding.recommendation}",
                        style = MaterialTheme.typography.bodySmall,
                        color = color,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = finding.targetIp,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = JetBrainsMonoFontFamily,
                        color = ElectricCyan
                    )
                }
            }
        }
    }
}
