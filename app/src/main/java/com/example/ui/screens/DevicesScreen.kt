package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PauseCircleFilled
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SignalWifi4Bar
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.DeviceCategory
import com.example.data.local.DeviceEntity
import com.example.data.network.NetworkOverviewInfo
import com.example.ui.components.AnimatedRadarScannerCircle
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
import com.example.ui.theme.NavyCardElevated
import com.example.ui.theme.SlateOffline
import com.example.ui.viewmodel.DeviceFilterOption
import com.example.ui.viewmodel.DeviceSortOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesScreen(
    overview: NetworkOverviewInfo,
    allDevicesCount: Int,
    onlineDevicesCount: Int,
    intruderCount: Int,
    devices: List<DeviceEntity>,
    isScanning: Boolean,
    scanProgress: Float,
    scanStatusMessage: String,
    searchQuery: String,
    sortOption: DeviceSortOption,
    filterOption: DeviceFilterOption,
    categoryFilter: DeviceCategory?,
    onRefreshScan: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSortOptionChange: (DeviceSortOption) -> Unit,
    onFilterOptionChange: (DeviceFilterOption) -> Unit,
    onCategoryFilterChange: (DeviceCategory?) -> Unit,
    onDeviceClick: (DeviceEntity) -> Unit,
    onQuickRenameDevice: (DeviceEntity, String) -> Unit,
    onNavigateToSecurity: () -> Unit
) {
    var showSortFilterSheet by remember { mutableStateOf(false) }
    var deviceToQuickRename by remember { mutableStateOf<DeviceEntity?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        onRefreshScan()
    }

    PullToRefreshBox(
        isRefreshing = isScanning,
        onRefresh = onRefreshScan,
        modifier = Modifier
            .fillMaxSize()
            .testTag("devices_pull_to_refresh")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Network Header Card (SSID, Router IP, Local IP, Refresh & Radar)
            item {
                NetworkHeaderCard(
                    overview = overview,
                    totalCount = allDevicesCount,
                    onlineCount = onlineDevicesCount,
                    isScanning = isScanning,
                    scanProgress = scanProgress,
                    scanStatusMessage = scanStatusMessage,
                    onRefreshScan = onRefreshScan,
                    onRequestSsidPermission = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                )
            }

            // Intruder warning banner if any untrusted device is active
            if (intruderCount > 0) {
                item {
                    Card(
                        onClick = onNavigateToSecurity,
                        colors = CardDefaults.cardColors(
                            containerColor = CoralDanger.copy(alpha = 0.14f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CoralDanger.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                            .testTag("intruder_alert_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = "تنبيه أمني",
                                tint = CoralDanger,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "تم رصد $intruderCount جهاز غير موثوق على الشبكة!",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = CoralDanger,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "اضغط هنا لمراجعة الأجهزة الدخيلة أو حظرها فوراً",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "عرض",
                                tint = CoralDanger
                            )
                        }
                    }
                }
            }

            // 2. Search Bar + Sort/Filter Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = {
                            Text(
                                text = "ابحث بالاسم، IP، MAC، أو المصنّع...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "بحث",
                                tint = ElectricCyan
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "مسح البحث"
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("device_search_input")
                    )

                    Surface(
                        onClick = { showSortFilterSheet = true },
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .size(54.dp)
                            .border(
                                1.dp,
                                ElectricCyan.copy(alpha = 0.45f),
                                RoundedCornerShape(14.dp)
                            )
                            .testTag("filter_sort_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "فلترة وفرز الأجهزة",
                                tint = ElectricCyan
                            )
                        }
                    }
                }
            }

            // 3. Quick Status & Category Filter Chips + Current Sort Indicator
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        items(DeviceFilterOption.entries) { option ->
                            val selected = filterOption == option
                            FilterChip(
                                selected = selected,
                                onClick = { onFilterOptionChange(option) },
                                label = {
                                    Text(
                                        text = option.arabicLabel,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElectricCyan.copy(alpha = 0.2f),
                                    selectedLabelColor = ElectricCyan
                                ),
                                modifier = Modifier.testTag("filter_chip_${option.name.lowercase()}")
                            )
                        }
                    }

                    // Category filter row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        item {
                            val allCatSelected = categoryFilter == null
                            FilterChip(
                                selected = allCatSelected,
                                onClick = { onCategoryFilterChange(null) },
                                label = { Text("كل الأنواع", style = MaterialTheme.typography.bodySmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldOnline.copy(alpha = 0.18f),
                                    selectedLabelColor = EmeraldOnline
                                )
                            )
                        }
                        items(DeviceCategory.entries) { cat ->
                            val selected = categoryFilter == cat
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    onCategoryFilterChange(if (selected) null else cat)
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = cat.icon(),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (selected) cat.accentColor() else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                label = { Text(cat.arabicLabel, style = MaterialTheme.typography.bodySmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = cat.accentColor().copy(alpha = 0.2f),
                                    selectedLabelColor = cat.accentColor()
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الأجهزة المكتشفة (${devices.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showSortFilterSheet = true }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "ترتيب",
                                tint = ElectricCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = sortOption.arabicLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = ElectricCyan,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // 4. Connected Devices List
            if (devices.isEmpty()) {
                item {
                    EmptyDevicesPlaceholder(onResetFilters = {
                        onSearchQueryChange("")
                        onFilterOptionChange(DeviceFilterOption.ALL)
                        onCategoryFilterChange(null)
                    })
                }
            } else {
                items(
                    items = devices,
                    key = { it.macAddress }
                ) { device ->
                    ConnectedDeviceCard(
                        device = device,
                        onClick = { onDeviceClick(device) },
                        onQuickRenameClick = { deviceToQuickRename = device }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Sort & Filter Modal Bottom Sheet
    if (showSortFilterSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showSortFilterSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "فرز وفلترة أجهزة الشبكة",
                    style = MaterialTheme.typography.titleLarge,
                    color = ElectricCyan
                )
                Text(
                    text = "ترتيب القائمة حسب:",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                DeviceSortOption.entries.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onSortOptionChange(option)
                            }
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = sortOption == option,
                            onClick = { onSortOptionChange(option) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = option.arabicLabel,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                Text(
                    text = "تصفية الحالة:",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(DeviceFilterOption.entries) { filter ->
                        FilterChip(
                            selected = filterOption == filter,
                            onClick = { onFilterOptionChange(filter) },
                            label = { Text(filter.arabicLabel) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { showSortFilterSheet = false },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = NavyBackground)
                ) {
                    Text("تطبيق الفرز والفلترة", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Quick Rename Dialog right from the main card
    deviceToQuickRename?.let { targetDevice ->
        var editedName by remember(targetDevice) { mutableStateOf(targetDevice.customName) }
        AlertDialog(
            onDismissRequest = { deviceToQuickRename = null },
            title = { Text("تعديل اسم الجهاز") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "IP: ${targetDevice.ipAddress}  •  MAC: ${targetDevice.macAddress}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("الاسم المخصص للجهاز") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onQuickRenameDevice(targetDevice, editedName.trim())
                        deviceToQuickRename = null
                    }
                ) {
                    Text("حفظ الاسم")
                }
            },
            dismissButton = {
                TextButton(onClick = { deviceToQuickRename = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun NetworkHeaderCard(
    overview: NetworkOverviewInfo,
    totalCount: Int,
    onlineCount: Int,
    isScanning: Boolean,
    scanProgress: Float,
    scanStatusMessage: String,
    onRefreshScan: () -> Unit,
    onRequestSsidPermission: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(ElectricCyan.copy(alpha = 0.65f), EmeraldOnline.copy(alpha = 0.25f))
                ),
                shape = RoundedCornerShape(22.dp)
            )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.img_network_radar_hero_1793452793567),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = 0.24f,
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(22.dp))
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                NavyBackground.copy(alpha = 0.55f),
                                NavyBackground.copy(alpha = 0.90f)
                            )
                        )
                    )
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SignalWifi4Bar,
                                contentDescription = "شبكة الواي فاي",
                                tint = ElectricCyan,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = overview.ssid,
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            IconButton(
                                onClick = onRequestSsidPermission,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = "تحديث اسم SSID الدقيق",
                                    tint = ElectricCyan.copy(alpha = 0.8f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = "${overview.connectionTransport}  •  إشارة ${overview.signalQualityPercent}% (${overview.rssiDbm} dBm)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AnimatedRadarScannerCircle(
                        isScanning = isScanning,
                        onlineCount = onlineCount
                    )
                }

                // Gateway IP, Device IP, BSSID/Subnet Metrics Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MonoMetricTag(
                        label = "عنوان IP الراوتر",
                        value = overview.gatewayIp,
                        accentColor = ElectricCyan,
                        modifier = Modifier.weight(1f)
                    )
                    MonoMetricTag(
                        label = "عنوان IP جهازك",
                        value = overview.localIp,
                        accentColor = EmeraldOnline,
                        modifier = Modifier.weight(1f)
                    )
                    MonoMetricTag(
                        label = "سرعة الربط",
                        value = "${overview.linkSpeedMbps} Mbps",
                        accentColor = AmberWarning,
                        modifier = Modifier.weight(0.9f)
                    )
                }

                AnimatedVisibility(visible = isScanning) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        LinearProgressIndicator(
                            progress = { scanProgress },
                            color = ElectricCyan,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                        Text(
                            text = scanStatusMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = ElectricCyan
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "إجمالي الأجهزة: $totalCount  |  نشط الآن: $onlineCount",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = onRefreshScan,
                        enabled = !isScanning,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = NavyBackground
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("refresh_network_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "إعادة فحص الشبكة",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isScanning) "جاري الفحص..." else "تحديث وفحص",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectedDeviceCard(
    device: DeviceEntity,
    onClick: () -> Unit,
    onQuickRenameClick: () -> Unit
) {
    val categoryColor = device.category.accentColor()
    val statusLabel = when {
        device.isBlocked -> "محظور"
        device.isPaused -> "موقوف مؤقتاً"
        device.isOnline -> "متصل (${device.latencyMs}ms)"
        else -> "غير متصل"
    }
    val statusColor = when {
        device.isBlocked -> CoralDanger
        device.isPaused -> AmberWarning
        device.isOnline -> EmeraldOnline
        else -> SlateOffline
    }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = when {
                    !device.isTrusted -> CoralDanger.copy(alpha = 0.55f)
                    device.isGateway -> ElectricCyan.copy(alpha = 0.45f)
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                },
                shape = RoundedCornerShape(18.dp)
            )
            .testTag("device_card_${device.ipAddress}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Device Type Icon Box
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(categoryColor.copy(alpha = 0.15f))
                        .border(1.dp, categoryColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = device.category.icon(),
                        contentDescription = device.category.arabicLabel,
                        tint = categoryColor,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Device Name, Vendor & Role Badges
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = device.customName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        IconButton(
                            onClick = onQuickRenameClick,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "تعديل اسم الجهاز",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    Text(
                        text = "${device.vendor}  •  ${device.category.arabicLabel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Online/Offline Status Pulse + Label
                Column(horizontalAlignment = Alignment.End) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StatusPulseDot(
                            isOnline = device.isOnline,
                            isPaused = device.isPaused,
                            isBlocked = device.isBlocked
                        )
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (device.isGateway) {
                            BadgeChip(text = "الراوتر", color = ElectricCyan)
                        }
                        if (device.isCurrentDevice) {
                            BadgeChip(text = "جهازك", color = EmeraldOnline)
                        }
                        if (!device.isTrusted) {
                            BadgeChip(text = "غير موثوق", color = CoralDanger)
                        }
                        if (device.alertsEnabled) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "التنبيهات مفعلة",
                                tint = AmberWarning,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

            // Bottom Technical Row: IP Address, MAC Address, Open Ports Count
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column {
                        Text(
                            text = "عنوان IP",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = device.ipAddress,
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = JetBrainsMonoFontFamily,
                            color = ElectricCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column {
                        Text(
                            text = "عنوان MAC",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = device.macAddress,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = JetBrainsMonoFontFamily,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (device.openPortsList.isNotEmpty()) {
                        BadgeChip(
                            text = "${device.openPortsList.size} منافذ مفتوحة",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "عرض التفاصيل",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyDevicesPlaceholder(onResetFilters: () -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Router,
                contentDescription = null,
                tint = ElectricCyan,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "لا توجد أجهزة مطابقة لخيارات البحث أو الفلترة",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "جرب مسح شروط التصفية لعرض جميع الأجهزة المكتشفة على الشبكة",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onResetFilters) {
                Text("إعادة ضبط الفلاتر", color = ElectricCyan, fontWeight = FontWeight.Bold)
            }
        }
    }
}
