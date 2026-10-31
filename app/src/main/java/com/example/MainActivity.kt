package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.BadgeChip
import com.example.ui.components.StatusPulseDot
import com.example.ui.screens.DeviceDetailsScreen
import com.example.ui.screens.DevicesScreen
import com.example.ui.screens.OnboardingTutorialScreen
import com.example.ui.screens.SecurityScreen
import com.example.ui.screens.SpeedTestScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NetPulseTheme
import com.example.ui.viewmodel.MainNavTab
import com.example.ui.viewmodel.NetPulseViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NetPulseTheme(darkTheme = true) {
                NetPulseApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetPulseApp(viewModel: NetPulseViewModel = viewModel()) {
    val showOnboarding by viewModel.showOnboardingTutorial.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val selectedDevice by viewModel.selectedDevice.collectAsStateWithLifecycle()
    val overview by viewModel.networkOverview.collectAsStateWithLifecycle()
    val allDevices by viewModel.allDevices.collectAsStateWithLifecycle()
    val filteredDevices by viewModel.filteredAndSortedDevices.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanningNetwork.collectAsStateWithLifecycle()
    val scanProgress by viewModel.scanProgress.collectAsStateWithLifecycle()
    val scanStatusMessage by viewModel.scanStatusMessage.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val filterOption by viewModel.filterOption.collectAsStateWithLifecycle()
    val categoryFilter by viewModel.categoryFilter.collectAsStateWithLifecycle()

    val deviceDiagState by viewModel.deviceDiagState.collectAsStateWithLifecycle()
    val speedTestState by viewModel.speedTestState.collectAsStateWithLifecycle()
    val speedHistory by viewModel.speedHistory.collectAsStateWithLifecycle()
    val securityReport by viewModel.securityReport.collectAsStateWithLifecycle()
    val securityEvents by viewModel.securityEvents.collectAsStateWithLifecycle()
    val autoIntruderAlert by viewModel.autoIntruderAlertEnabled.collectAsStateWithLifecycle()

    val activeTool by viewModel.activeTool.collectAsStateWithLifecycle()
    val toolTargetHost by viewModel.toolTargetHost.collectAsStateWithLifecycle()
    val isToolRunning by viewModel.isToolRunning.collectAsStateWithLifecycle()
    val pingPackets by viewModel.pingPackets.collectAsStateWithLifecycle()
    val pingSummary by viewModel.pingSummary.collectAsStateWithLifecycle()
    val tracerouteHops by viewModel.tracerouteHops.collectAsStateWithLifecycle()
    val portScanResults by viewModel.portScanResults.collectAsStateWithLifecycle()
    val portScanProgress by viewModel.portScanProgress.collectAsStateWithLifecycle()
    val dnsLookupResult by viewModel.dnsLookupResult.collectAsStateWithLifecycle()
    val wolToolResult by viewModel.wolToolResult.collectAsStateWithLifecycle()

    val onlineCount = allDevices.count { it.isOnline && !it.isBlocked && !it.isPaused }
    val intruderCount = allDevices.count { !it.isTrusted && !it.isBlocked }

    // Handle back button when on non-home tab
    BackHandler(enabled = !showOnboarding && selectedDevice == null && currentTab != MainNavTab.DEVICES) {
        viewModel.selectTab(MainNavTab.DEVICES)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpandedScreen = maxWidth >= 700.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                if (!showOnboarding && selectedDevice == null) {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(ElectricCyan.copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Radar,
                                        contentDescription = null,
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "NetPulse • محلل الشبكات الذكي",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${overview.ssid} (${overview.gatewayIp})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        actions = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(end = 12.dp)
                            ) {
                                // Interactive Tutorial Re-launch Button
                                Surface(
                                    onClick = { viewModel.openOnboardingTutorial() },
                                    shape = RoundedCornerShape(10.dp),
                                    color = ElectricCyan.copy(alpha = 0.15f),
                                    modifier = Modifier
                                        .border(1.dp, ElectricCyan.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                                        .testTag("open_tutorial_button")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.School,
                                            contentDescription = "الجولة التعليمية",
                                            tint = ElectricCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "دليل الشبكة",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ElectricCyan,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                StatusPulseDot(isOnline = overview.isConnected)
                                BadgeChip(
                                    text = "$onlineCount متصل",
                                    color = EmeraldOnline
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = NavySurface,
                            titleContentColor = Color.White
                        )
                    )
                }
            },
            bottomBar = {
                if (!showOnboarding && !isExpandedScreen && selectedDevice == null) {
                    NavigationBar(
                        containerColor = NavySurface,
                        tonalElevation = 8.dp
                    ) {
                        MainNavTab.entries.forEach { tab ->
                            val selected = currentTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(tab) },
                                icon = {
                                    BadgedBox(
                                        badge = {
                                            if (tab == MainNavTab.SECURITY && intruderCount > 0) {
                                                Badge(containerColor = CoralDanger) {
                                                    Text("$intruderCount", color = Color.White)
                                                }
                                            } else if (tab == MainNavTab.DEVICES && onlineCount > 0) {
                                                Badge(containerColor = ElectricCyan) {
                                                    Text("$onlineCount", color = NavyBackground)
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = tab.icon(selected),
                                            contentDescription = tab.arabicTitle
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        text = tab.arabicTitle,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = NavyBackground,
                                    selectedTextColor = ElectricCyan,
                                    indicatorColor = ElectricCyan,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (!showOnboarding && isExpandedScreen && selectedDevice == null) {
                    NavigationRail(
                        containerColor = NavySurface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        MainNavTab.entries.forEach { tab ->
                            val selected = currentTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(tab) },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon(selected),
                                        contentDescription = tab.arabicTitle
                                    )
                                },
                                label = { Text(tab.arabicTitle) },
                                modifier = Modifier.testTag("rail_tab_${tab.name.lowercase()}")
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    val activeDevice = selectedDevice
                    when {
                        showOnboarding -> {
                            OnboardingTutorialScreen(
                                overview = overview,
                                onFinishTutorial = { viewModel.completeOnboardingTutorial() },
                                onJumpToFeature = { tab, triggerAction ->
                                    viewModel.jumpFromTutorialToFeature(tab, triggerAction)
                                }
                            )
                        }

                        activeDevice != null -> {
                            DeviceDetailsScreen(
                                device = activeDevice,
                                gatewayIp = overview.gatewayIp,
                                diagState = deviceDiagState,
                                onBack = { viewModel.closeDeviceDetails() },
                                onSaveCustomization = { newName, newCategory, newRoom, newNotes, isTrusted, alertsEnabled ->
                                    viewModel.saveDeviceCustomization(
                                        macAddress = activeDevice.macAddress,
                                        newName = newName,
                                        newCategory = newCategory,
                                        newRoom = newRoom,
                                        newNotes = newNotes,
                                        isTrusted = isTrusted,
                                        alertsEnabled = alertsEnabled
                                    )
                                },
                                onToggleBlock = { viewModel.toggleBlockDevice(activeDevice) },
                                onTogglePause = { label -> viewModel.togglePauseDeviceInternet(activeDevice, label) },
                                onToggleAlerts = { enabled -> viewModel.toggleDeviceAlerts(activeDevice, enabled) },
                                onRunDeviceSpeedTest = { viewModel.runDeviceSpeedTest(activeDevice) },
                                onRunPortAndPingScan = { viewModel.runDevicePortAndPingScan(activeDevice) },
                                onTriggerWakeOnLan = { port -> viewModel.triggerDeviceWakeOnLan(activeDevice, port) },
                                onDismissBanner = { viewModel.clearDeviceBannerMessage() }
                            )
                        }

                        else -> {
                            Crossfade(targetState = currentTab, label = "main_tab_crossfade") { tab ->
                                when (tab) {
                                    MainNavTab.DEVICES -> DevicesScreen(
                                        overview = overview,
                                        allDevicesCount = allDevices.size,
                                        onlineDevicesCount = onlineCount,
                                        intruderCount = intruderCount,
                                        devices = filteredDevices,
                                        isScanning = isScanning,
                                        scanProgress = scanProgress,
                                        scanStatusMessage = scanStatusMessage,
                                        searchQuery = searchQuery,
                                        sortOption = sortOption,
                                        filterOption = filterOption,
                                        categoryFilter = categoryFilter,
                                        onRefreshScan = { viewModel.refreshNetworkAndScan() },
                                        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                                        onSortOptionChange = { viewModel.updateSortOption(it) },
                                        onFilterOptionChange = { viewModel.updateFilterOption(it) },
                                        onCategoryFilterChange = { viewModel.updateCategoryFilter(it) },
                                        onDeviceClick = { viewModel.openDeviceDetails(it) },
                                        onQuickRenameDevice = { dev, newName ->
                                            viewModel.saveDeviceCustomization(
                                                macAddress = dev.macAddress,
                                                newName = newName,
                                                newCategory = dev.category,
                                                newRoom = dev.locationRoom,
                                                newNotes = dev.notes,
                                                isTrusted = dev.isTrusted,
                                                alertsEnabled = dev.alertsEnabled
                                            )
                                        },
                                        onNavigateToSecurity = { viewModel.selectTab(MainNavTab.SECURITY) }
                                    )

                                    MainNavTab.SPEED -> SpeedTestScreen(
                                        overview = overview,
                                        liveState = speedTestState,
                                        history = speedHistory,
                                        onStartSpeedTest = { viewModel.startInternetSpeedTest() }
                                    )

                                    MainNavTab.SECURITY -> SecurityScreen(
                                        overview = overview,
                                        auditReport = securityReport,
                                        devices = allDevices,
                                        securityEvents = securityEvents,
                                        autoIntruderAlertEnabled = autoIntruderAlert,
                                        onStartSecurityAudit = { viewModel.startSecurityAudit() },
                                        onToggleAutoIntruderAlert = { viewModel.toggleAutoIntruderAlert(it) },
                                        onTrustDevice = { viewModel.trustIntruderDevice(it) },
                                        onBlockDevice = { viewModel.blockIntruderDevice(it) },
                                        onInspectDevice = { viewModel.openDeviceDetails(it) }
                                    )

                                    MainNavTab.TOOLS -> ToolsScreen(
                                        overview = overview,
                                        devices = allDevices,
                                        activeTool = activeTool,
                                        targetHost = toolTargetHost,
                                        isToolRunning = isToolRunning,
                                        pingPackets = pingPackets,
                                        pingSummary = pingSummary,
                                        tracerouteHops = tracerouteHops,
                                        portScanResults = portScanResults,
                                        portScanProgress = portScanProgress,
                                        dnsLookupResult = dnsLookupResult,
                                        wolResult = wolToolResult,
                                        onSelectTool = { tool, target -> viewModel.selectDiagnosticTool(tool, target) },
                                        onTargetHostChange = { viewModel.updateToolTargetHost(it) },
                                        onExecuteTool = { viewModel.executeSelectedDiagnosticTool() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun MainNavTab.icon(selected: Boolean): ImageVector = when (this) {
    MainNavTab.DEVICES -> if (selected) Icons.Filled.Devices else Icons.Outlined.Devices
    MainNavTab.SPEED -> if (selected) Icons.Filled.Speed else Icons.Outlined.Speed
    MainNavTab.SECURITY -> if (selected) Icons.Filled.Security else Icons.Outlined.Security
    MainNavTab.TOOLS -> if (selected) Icons.Filled.Build else Icons.Outlined.Build
}
