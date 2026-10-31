package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.NetworkPing
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.DeviceEntity
import com.example.data.network.DnsLookupResult
import com.example.data.network.NetworkOverviewInfo
import com.example.data.network.PingPacketResult
import com.example.data.network.PingSummary
import com.example.data.network.PortScanItem
import com.example.data.network.TracerouteHop
import com.example.data.network.WolResult
import com.example.ui.components.BadgeChip
import com.example.ui.components.MonoMetricTag
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyCardElevated
import com.example.ui.theme.PurpleAccent
import com.example.ui.viewmodel.DiagnosticToolType

@Composable
fun ToolsScreen(
    overview: NetworkOverviewInfo,
    devices: List<DeviceEntity>,
    activeTool: DiagnosticToolType,
    targetHost: String,
    isToolRunning: Boolean,
    pingPackets: List<PingPacketResult>,
    pingSummary: PingSummary?,
    tracerouteHops: List<TracerouteHop>,
    portScanResults: List<PortScanItem>,
    portScanProgress: Float,
    dnsLookupResult: DnsLookupResult?,
    wolResult: WolResult?,
    onSelectTool: (DiagnosticToolType, String?) -> Unit,
    onTargetHostChange: (String) -> Unit,
    onExecuteTool: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("tools_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Tool Selector Cards Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "أدوات استكشاف الأخطاء وتشخيص الشبكة",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(DiagnosticToolType.entries) { tool ->
                        val isSelected = activeTool == tool
                        ToolSelectorCard(
                            tool = tool,
                            isSelected = isSelected,
                            onClick = { onSelectTool(tool, null) }
                        )
                    }
                }
            }
        }

        // 2. Active Tool Input & Quick Presets Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ElectricCyan.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = activeTool.icon(),
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = activeTool.arabicTitle,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = activeTool.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Target Input Field + Execute Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = targetHost,
                            onValueChange = onTargetHostChange,
                            label = {
                                Text(
                                    text = when (activeTool) {
                                        DiagnosticToolType.WAKE_ON_LAN -> "عنوان MAC أو IP للجهاز"
                                        DiagnosticToolType.PORT_SCANNER -> "عنوان IP للجهاز المستهدف"
                                        else -> "النطاق أو عنوان IP (مثال: 8.8.8.8 أو google.com)"
                                    }
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tool_target_input")
                        )

                        Button(
                            onClick = onExecuteTool,
                            enabled = !isToolRunning && targetHost.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricCyan,
                                contentColor = NavyBackground
                            ),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 15.dp),
                            modifier = Modifier.testTag("run_tool_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "بدء",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isToolRunning) "نشط..." else "تشغيل",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Quick Target Presets
                    Text(
                        text = "أهداف سريعة للاختبار:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (activeTool == DiagnosticToolType.WAKE_ON_LAN) {
                            items(devices.filter { !it.isGateway && !it.isCurrentDevice }) { dev ->
                                FilterChip(
                                    selected = targetHost.equals(dev.macAddress, ignoreCase = true),
                                    onClick = { onTargetHostChange(dev.macAddress) },
                                    label = { Text("${dev.customName} (${dev.macAddress})") }
                                )
                            }
                        } else if (activeTool == DiagnosticToolType.PORT_SCANNER) {
                            item {
                                FilterChip(
                                    selected = targetHost == overview.gatewayIp,
                                    onClick = { onTargetHostChange(overview.gatewayIp) },
                                    label = { Text("الراوتر (${overview.gatewayIp})") }
                                )
                            }
                            items(devices.take(5)) { dev ->
                                FilterChip(
                                    selected = targetHost == dev.ipAddress,
                                    onClick = { onTargetHostChange(dev.ipAddress) },
                                    label = { Text("${dev.customName} (${dev.ipAddress})") }
                                )
                            }
                        } else {
                            val quickTargets = listOf(
                                "الراوتر (${overview.gatewayIp})" to overview.gatewayIp,
                                "Google DNS (8.8.8.8)" to "8.8.8.8",
                                "Cloudflare (1.1.1.1)" to "1.1.1.1",
                                "google.com" to "google.com",
                                "cloudflare.com" to "cloudflare.com"
                            )
                            items(quickTargets) { (label, host) ->
                                FilterChip(
                                    selected = targetHost == host,
                                    onClick = { onTargetHostChange(host) },
                                    label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ElectricCyan.copy(alpha = 0.2f),
                                        selectedLabelColor = ElectricCyan
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Tool-Specific Live Output Panel
        item {
            when (activeTool) {
                DiagnosticToolType.PING -> PingOutputPanel(
                    isRunning = isToolRunning,
                    packets = pingPackets,
                    summary = pingSummary
                )

                DiagnosticToolType.TRACEROUTE -> TracerouteOutputPanel(
                    isRunning = isToolRunning,
                    hops = tracerouteHops
                )

                DiagnosticToolType.PORT_SCANNER -> PortScannerOutputPanel(
                    isRunning = isToolRunning,
                    progress = portScanProgress,
                    results = portScanResults
                )

                DiagnosticToolType.DNS_LOOKUP -> DnsLookupOutputPanel(
                    isRunning = isToolRunning,
                    result = dnsLookupResult
                )

                DiagnosticToolType.WAKE_ON_LAN -> WolToolOutputPanel(
                    result = wolResult
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun DiagnosticToolType.icon(): ImageVector = when (this) {
    DiagnosticToolType.PING -> Icons.Default.NetworkPing
    DiagnosticToolType.TRACEROUTE -> Icons.Default.AltRoute
    DiagnosticToolType.PORT_SCANNER -> Icons.Default.Radar
    DiagnosticToolType.DNS_LOOKUP -> Icons.Default.Dns
    DiagnosticToolType.WAKE_ON_LAN -> Icons.Default.PowerSettingsNew
}

@Composable
private fun ToolSelectorCard(
    tool: DiagnosticToolType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) ElectricCyan.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .width(155.dp)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) ElectricCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("tool_tab_${tool.name.lowercase()}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = tool.icon(),
                contentDescription = tool.arabicTitle,
                tint = if (isSelected) ElectricCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(26.dp)
            )
            Text(
                text = tool.arabicTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) ElectricCyan else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun PingOutputPanel(
    isRunning: Boolean,
    packets: List<PingPacketResult>,
    summary: PingSummary?
) {
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
                    imageVector = Icons.Default.Terminal,
                    contentDescription = null,
                    tint = EmeraldOnline
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "مخرجات فحص الاستجابة (Ping Telemetry)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isRunning) {
                LinearProgressIndicator(
                    color = ElectricCyan,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            summary?.let { sum ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MonoMetricTag("المتوسط Avg", "${sum.avgMs} ms", EmeraldOnline, Modifier.weight(1f))
                    MonoMetricTag("الأدنى Min", "${sum.minMs} ms", ElectricCyan, Modifier.weight(1f))
                    MonoMetricTag("الأعلى Max", "${sum.maxMs} ms", AmberWarning, Modifier.weight(1f))
                    MonoMetricTag("الفقد Loss", "${sum.lostPercent.toInt()}%", if (sum.lostPercent > 0f) CoralDanger else EmeraldOnline, Modifier.weight(1f))
                }
            }

            if (packets.isEmpty() && !isRunning) {
                Text(
                    text = "اضغط على (تشغيل) لإرسال حزم ICMP/TCP Ping وقياس زمن الاستجابة الفعلي.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(NavyBackground)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    packets.forEach { pkt ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = pkt.statusText,
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = JetBrainsMonoFontFamily,
                                color = if (pkt.isSuccess) Color.White else CoralDanger,
                                modifier = Modifier.weight(1f)
                            )
                            BadgeChip(
                                text = if (pkt.isSuccess) "${pkt.timeMs} ms" else "TIMEOUT",
                                color = if (pkt.isSuccess) EmeraldOnline else CoralDanger
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TracerouteOutputPanel(
    isRunning: Boolean,
    hops: List<TracerouteHop>
) {
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
                text = "مسار الحزم والقفزات (Traceroute Hops)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (isRunning) {
                LinearProgressIndicator(
                    color = ElectricCyan,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (hops.isEmpty() && !isRunning) {
                Text(
                    text = "اضغط على (تشغيل) لتتبع القفزات (Hops) من الراوتر المحلي حتى الخادم الهدف.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                hops.forEach { hop ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                            .border(
                                width = 1.dp,
                                color = if (hop.isDestination) EmeraldOnline.copy(alpha = 0.5f) else Color.Transparent,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (hop.isDestination) EmeraldOnline else ElectricCyan),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${hop.hopNumber}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyBackground
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = hop.hostOrIp,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${hop.ipAddress}  •  ${hop.locationHint}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        BadgeChip(
                            text = "${hop.rttMs} ms",
                            color = if (hop.isDestination) EmeraldOnline else ElectricCyan
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PortScannerOutputPanel(
    isRunning: Boolean,
    progress: Float,
    results: List<PortScanItem>
) {
    val openCount = results.count { it.isOpen }
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "نتائج فاحص المنافذ (Port Scanner)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (results.isNotEmpty()) {
                    BadgeChip(
                        text = "$openCount منافذ مفتوحة من ${results.size}",
                        color = EmeraldOnline
                    )
                }
            }

            AnimatedVisibility(visible = isRunning) {
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0.05f, 1f) },
                    color = ElectricCyan,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (results.isEmpty() && !isRunning) {
                Text(
                    text = "أدخل عنوان IP أو اختر جهازاً من الأعلى واضغط (تشغيل) لفحص المنافذ الشائعة والخدمات.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                results.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (item.isOpen) EmeraldOnline.copy(alpha = 0.10f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            BadgeChip(
                                text = "TCP/${item.port}",
                                color = if (item.isOpen) ElectricCyan else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = item.serviceName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = item.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        BadgeChip(
                            text = if (item.isOpen) "مفتوح (${item.responseTimeMs}ms)" else "مغلق",
                            color = if (item.isOpen) EmeraldOnline else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DnsLookupOutputPanel(
    isRunning: Boolean,
    result: DnsLookupResult?
) {
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
                text = "نتائج استعلام النطاق (DNS Lookup)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (isRunning) {
                LinearProgressIndicator(color = ElectricCyan, modifier = Modifier.fillMaxWidth())
            }

            if (result == null && !isRunning) {
                Text(
                    text = "أدخل اسم النطاق (مثل google.com) واضغط (تشغيل) لعرض سجلات IPv4/IPv6 والاسم الرسمي.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (result != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MonoMetricTag("خادم DNS", result.dnsServerUsed, ElectricCyan, Modifier.weight(1f))
                    MonoMetricTag("زمن الاستعلام", "${result.queryTimeMs} ms", EmeraldOnline, Modifier.weight(1f))
                }
                MonoMetricTag(
                    label = "الاسم الرسمي (Canonical Host)",
                    value = result.canonicalHostname,
                    accentColor = AmberWarning,
                    modifier = Modifier.fillMaxWidth()
                )

                result.errorMessage?.let { err ->
                    Text(text = err, style = MaterialTheme.typography.bodySmall, color = CoralDanger)
                }

                result.records.forEach { rec ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BadgeChip(text = rec.type, color = ElectricCyan)
                        Text(
                            text = rec.value,
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "TTL ${rec.ttlSeconds}s",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WolToolOutputPanel(result: WolResult?) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "حالة الإيقاظ عبر الشبكة (Wake on LAN)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (result == null) {
                Text(
                    text = "اختر جهازاً من الأعلى أو أدخل عنوان MAC واضغط (تشغيل) لإرسال الحزمة السحرية UDP Magic Packet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = result.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (result.success) EmeraldOnline else CoralDanger,
                    fontWeight = FontWeight.Bold
                )
                MonoMetricTag("عنوان البث Broadcast IP", "${result.broadcastIp}:${result.port}", ElectricCyan, Modifier.fillMaxWidth())
                MonoMetricTag("محتوى الحزمة السحرية", result.packetHexPreview, EmeraldOnline, Modifier.fillMaxWidth())
            }
        }
    }
}
