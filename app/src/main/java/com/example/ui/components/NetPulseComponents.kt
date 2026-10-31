package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DevicesOther
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.LaptopMac
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.local.DeviceCategory
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.SlateOffline
import kotlin.math.cos
import kotlin.math.sin

fun DeviceCategory.icon(): ImageVector = when (this) {
    DeviceCategory.ROUTER -> Icons.Default.Router
    DeviceCategory.SMARTPHONE -> Icons.Default.Smartphone
    DeviceCategory.LAPTOP -> Icons.Default.LaptopMac
    DeviceCategory.DESKTOP -> Icons.Default.Dns
    DeviceCategory.SMART_TV -> Icons.Default.Tv
    DeviceCategory.CAMERA -> Icons.Default.Videocam
    DeviceCategory.PRINTER -> Icons.Default.Print
    DeviceCategory.CONSOLE -> Icons.Default.SportsEsports
    DeviceCategory.IOT -> Icons.Default.DevicesOther
}

fun DeviceCategory.accentColor(): Color = when (this) {
    DeviceCategory.ROUTER -> ElectricCyan
    DeviceCategory.SMARTPHONE -> EmeraldOnline
    DeviceCategory.LAPTOP -> Color(0xFF38BDF8)
    DeviceCategory.DESKTOP -> PurpleAccent
    DeviceCategory.SMART_TV -> Color(0xFFF472B6)
    DeviceCategory.CAMERA -> AmberWarning
    DeviceCategory.PRINTER -> Color(0xFFA3E635)
    DeviceCategory.CONSOLE -> Color(0xFF818CF8)
    DeviceCategory.IOT -> Color(0xFFFB923C)
}

@Composable
fun StatusPulseDot(
    isOnline: Boolean,
    isPaused: Boolean = false,
    isBlocked: Boolean = false,
    size: Dp = 10.dp
) {
    val dotColor = when {
        isBlocked -> CoralDanger
        isPaused -> AmberWarning
        isOnline -> EmeraldOnline
        else -> SlateOffline
    }

    Box(contentAlignment = Alignment.Center) {
        if (isOnline && !isPaused && !isBlocked) {
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 2.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1400, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "pulse_scale"
            )
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.55f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1400, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "pulse_alpha"
            )
            Box(
                modifier = Modifier
                    .size(size * scale)
                    .clip(CircleShape)
                    .background(dotColor.copy(alpha = alpha))
            )
        }
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(dotColor)
        )
    }
}

@Composable
fun BadgeChip(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = color.copy(alpha = 0.14f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun MonoMetricTag(
    label: String,
    value: String,
    accentColor: Color = ElectricCyan,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontFamily = JetBrainsMonoFontFamily,
            fontWeight = FontWeight.Bold,
            color = accentColor
        )
    }
}

@Composable
fun AnimatedRadarScannerCircle(
    isScanning: Boolean,
    onlineCount: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_angle"
    )

    Box(
        modifier = modifier.size(74.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val radius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // Concentric radar rings
            drawCircle(
                color = ElectricCyan.copy(alpha = 0.18f),
                radius = radius,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )
            drawCircle(
                color = ElectricCyan.copy(alpha = 0.30f),
                radius = radius * 0.68f,
                center = center,
                style = Stroke(width = 1.2.dp.toPx())
            )
            drawCircle(
                color = EmeraldOnline.copy(alpha = 0.22f),
                radius = radius * 0.36f,
                center = center
            )

            if (isScanning) {
                val rad = Math.toRadians(sweepAngle.toDouble())
                val endX = center.x + (radius * cos(rad)).toFloat()
                val endY = center.y + (radius * sin(rad)).toFloat()
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(ElectricCyan, EmeraldOnline),
                        start = center,
                        end = Offset(endX, endY)
                    ),
                    start = center,
                    end = Offset(endX, endY),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$onlineCount",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                color = EmeraldOnline
            )
            Text(
                text = "متصل",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
