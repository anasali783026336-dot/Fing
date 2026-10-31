package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.NetworkPing
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.SpeedTestRecordEntity
import com.example.data.network.NetworkCatalog
import com.example.data.network.NetworkOverviewInfo
import com.example.data.network.SpeedTestLiveState
import com.example.data.network.SpeedTestPhase
import com.example.ui.components.BadgeChip
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyCardElevated
import com.example.ui.theme.PurpleAccent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SpeedTestScreen(
    overview: NetworkOverviewInfo,
    liveState: SpeedTestLiveState,
    history: List<SpeedTestRecordEntity>,
    onStartSpeedTest: () -> Unit
) {
    val latestRecord = history.firstOrNull()
    val displayDownload = if (liveState.downloadMbps > 0f) liveState.downloadMbps else (latestRecord?.downloadMbps ?: 172.4f)
    val displayUpload = if (liveState.uploadMbps > 0f) liveState.uploadMbps else (latestRecord?.uploadMbps ?: 68.5f)
    val displayPing = if (liveState.pingMs > 0) liveState.pingMs else (latestRecord?.pingMs ?: 14)
    val displayJitter = if (liveState.jitterMs > 0) liveState.jitterMs else (latestRecord?.jitterMs ?: 2)

    val gamingScore = if (liveState.gamingScore > 0) liveState.gamingScore else (latestRecord?.gamingScore ?: 94)
    val videoScore = if (liveState.videoCallScore > 0) liveState.videoCallScore else (latestRecord?.videoCallScore ?: 95)
    val streamingScore = if (liveState.streamingScore > 0) liveState.streamingScore else (latestRecord?.streamingScore ?: 98)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("speed_test_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. ISP & Active Connection Header
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ElectricCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = ElectricCyan
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = overview.ispName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "IP العام: ${overview.publicIp}  •  ${overview.ssid}",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = JetBrainsMonoFontFamily,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    BadgeChip(
                        text = overview.connectionTransport.substringBefore(" "),
                        color = EmeraldOnline
                    )
                }
            }
        }

        // 2. Speedometer Gauge + Big Start Speed Test Button + Download / Upload / Ping Cards
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        Brush.linearGradient(listOf(ElectricCyan.copy(alpha = 0.6f), PurpleAccent.copy(alpha = 0.4f))),
                        RoundedCornerShape(24.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = liveState.phase.arabicTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = ElectricCyan,
                        fontWeight = FontWeight.Bold
                    )

                    // Interactive Animated Arc Gauge
                    val gaugeTarget = when (liveState.phase) {
                        SpeedTestPhase.DOWNLOAD_TEST -> liveState.currentGaugeMbps
                        SpeedTestPhase.UPLOAD_TEST -> liveState.currentGaugeMbps
                        SpeedTestPhase.COMPLETED -> liveState.downloadMbps
                        else -> displayDownload
                    }
                    SpeedometerArcGauge(
                        speedMbps = gaugeTarget,
                        phase = liveState.phase,
                        isRunning = liveState.isRunning
                    )

                    // Big Start Speed Test Button
                    Button(
                        onClick = onStartSpeedTest,
                        enabled = !liveState.isRunning,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = NavyBackground
                        ),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .testTag("start_speed_test_button")
                    ) {
                        Icon(
                            imageVector = if (liveState.isRunning) Icons.Default.Speed else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (liveState.isRunning) "جاري قياس سرعة الإنترنت..." else "ابدأ اختبار السرعة الآن",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Primary Telemetry Row: Download, Upload, Ping & Jitter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SpeedMetricBox(
                            icon = Icons.Default.CloudDownload,
                            title = "التحميل Download",
                            value = "$displayDownload",
                            unit = "Mbps",
                            color = ElectricCyan,
                            modifier = Modifier.weight(1f)
                        )
                        SpeedMetricBox(
                            icon = Icons.Default.CloudUpload,
                            title = "الرفع Upload",
                            value = "$displayUpload",
                            unit = "Mbps",
                            color = EmeraldOnline,
                            modifier = Modifier.weight(1f)
                        )
                        SpeedMetricBox(
                            icon = Icons.Default.NetworkPing,
                            title = "الاستجابة Ping",
                            value = "$displayPing",
                            unit = "ms (±${displayJitter}ms)",
                            color = AmberWarning,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 3. Application Quality Matrix: Gaming, Video Calls, Live Streaming
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
                    Text(
                        text = "اختبار جودة الإنترنت للتطبيقات الحية",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "تقييم حقيقي لأداء الاتصال في الألعاب التنافسية، مكالمات الفيديو، والبث المباشر 4K:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    QualityScoreRow(
                        icon = Icons.Default.SportsEsports,
                        title = "الألعاب الأونلاين (Online Gaming)",
                        subtitle = "يعتمد على زمن الاستجابة (${displayPing}ms) والارتعاش (${displayJitter}ms)",
                        score = gamingScore,
                        accentColor = ElectricCyan
                    )

                    QualityScoreRow(
                        icon = Icons.Default.Videocam,
                        title = "مكالمات الفيديو والاجتماعات (HD Video Calls)",
                        subtitle = "يعتمد على ثبات الرفع (${displayUpload} Mbps) وعدم فقدان الحزم",
                        score = videoScore,
                        accentColor = EmeraldOnline
                    )

                    QualityScoreRow(
                        icon = Icons.Default.LiveTv,
                        title = "البث المباشر فائق الدقة (4K UHD Streaming)",
                        subtitle = "يعتمد على سعة التحميل المستمرة (${displayDownload} Mbps)",
                        score = streamingScore,
                        accentColor = PurpleAccent
                    )
                }
            }
        }

        // 4. ISP Performance Comparison (مقارنة أداء مزود خدمة الإنترنت)
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
                        text = "مقارنة أداء مزود خدمة الإنترنت (ISP Performance)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "مقارنة سرعتك الحالية (${displayDownload} Mbps) مع متوسطات مزودي الخدمة والباقات القياسية:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // User's current bar
                    IspComparisonBar(
                        label = "اتصالك الحالي (${overview.ispName})",
                        downloadMbps = displayDownload,
                        uploadMbps = displayUpload,
                        pingMs = displayPing,
                        maxScaleMbps = 320f,
                        barColor = ElectricCyan,
                        isHighlighted = true
                    )

                    NetworkCatalog.ispBenchmarks.forEach { tier ->
                        IspComparisonBar(
                            label = tier.tierName,
                            downloadMbps = tier.avgDownloadMbps,
                            uploadMbps = tier.avgUploadMbps,
                            pingMs = tier.avgPingMs,
                            maxScaleMbps = 320f,
                            barColor = EmeraldOnline.copy(alpha = 0.7f),
                            isHighlighted = false
                        )
                    }
                }
            }
        }

        // 5. Speed Test History Log
        if (history.isNotEmpty()) {
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
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = ElectricCyan
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "سجل اختبارات السرعة السابقة",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        val formatter = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US)
                        history.take(5).forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = item.ssidName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = formatter.format(Date(item.timestamp)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(
                                        text = "↓ ${item.downloadMbps}M",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        color = ElectricCyan
                                    )
                                    Text(
                                        text = "↑ ${item.uploadMbps}M",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        color = EmeraldOnline
                                    )
                                    Text(
                                        text = "${item.pingMs}ms",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        color = AmberWarning
                                    )
                                }
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
}

@Composable
private fun SpeedometerArcGauge(
    speedMbps: Float,
    phase: SpeedTestPhase,
    isRunning: Boolean
) {
    val normalized = (speedMbps / 350f).coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = normalized,
        animationSpec = tween(durationMillis = 300),
        label = "speed_gauge"
    )

    Box(
        modifier = Modifier.size(210.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 16.dp.toPx()
            val arcSize = Size(size.width - strokeWidth * 2, size.height - strokeWidth * 2)
            val topLeft = Offset(strokeWidth, strokeWidth)

            // Track Arc
            drawArc(
                color = Color.White.copy(alpha = 0.10f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active Gradient Arc
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(ElectricCyan, EmeraldOnline, PurpleAccent, ElectricCyan)
                ),
                startAngle = 135f,
                sweepAngle = 270f * animatedFraction.coerceAtLeast(0.03f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = String.format(Locale.US, "%.1f", speedMbps),
                style = MaterialTheme.typography.displayLarge,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Mbps",
                style = MaterialTheme.typography.labelLarge,
                fontFamily = JetBrainsMonoFontFamily,
                color = ElectricCyan
            )
            Spacer(modifier = Modifier.height(4.dp))
            BadgeChip(
                text = when (phase) {
                    SpeedTestPhase.UPLOAD_TEST -> "UPLOAD"
                    SpeedTestPhase.PING_TEST -> "PING / JITTER"
                    else -> "DOWNLOAD"
                },
                color = if (phase == SpeedTestPhase.UPLOAD_TEST) EmeraldOnline else ElectricCyan
            )
        }
    }
}

@Composable
private fun SpeedMetricBox(
    icon: ImageVector,
    title: String,
    value: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(NavyBackground.copy(alpha = 0.65f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontFamily = JetBrainsMonoFontFamily,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = JetBrainsMonoFontFamily,
            color = color
        )
    }
}

@Composable
private fun QualityScoreRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    score: Int,
    accentColor: Color
) {
    val gradeText = when {
        score >= 90 -> "ممتاز ($score%)"
        score >= 80 -> "جيد جداً ($score%)"
        score >= 65 -> "متوسط ($score%)"
        else -> "ضعيف ($score%)"
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(12.dp),
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
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            BadgeChip(text = gradeText, color = accentColor)
        }

        LinearProgressIndicator(
            progress = { (score / 100f).coerceIn(0.1f, 1f) },
            color = accentColor,
            trackColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}

@Composable
private fun IspComparisonBar(
    label: String,
    downloadMbps: Float,
    uploadMbps: Float,
    pingMs: Int,
    maxScaleMbps: Float,
    barColor: Color,
    isHighlighted: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isHighlighted) ElectricCyan.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
            .border(
                width = 1.dp,
                color = if (isHighlighted) ElectricCyan.copy(alpha = 0.5f) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
                color = if (isHighlighted) ElectricCyan else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "↓ ${downloadMbps.toInt()}M  •  ↑ ${uploadMbps.toInt()}M  •  ${pingMs}ms",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = JetBrainsMonoFontFamily,
                color = barColor
            )
        }
        LinearProgressIndicator(
            progress = { (downloadMbps / maxScaleMbps).coerceIn(0.06f, 1f) },
            color = barColor,
            trackColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}
