package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.NetworkPing
import androidx.compose.material.icons.filled.PauseCircleFilled
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SignalWifi4Bar
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.network.NetworkOverviewInfo
import com.example.ui.components.AnimatedRadarScannerCircle
import com.example.ui.components.BadgeChip
import com.example.ui.components.MonoMetricTag
import com.example.ui.components.StatusPulseDot
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyCardElevated
import com.example.ui.theme.NavySurface
import com.example.ui.theme.PurpleAccent
import com.example.ui.viewmodel.MainNavTab
import kotlin.math.roundToInt

private enum class TutorialStep(
    val stepIndex: Int,
    val arabicTitle: String,
    val badgeTitle: String,
    val subtitle: String
) {
    NETWORK_CONCEPTS(
        0,
        "أساسيات ومفاهيم الشبكة",
        "1. المفاهيم الأساسية",
        "تعرّف بشكل تفاعلي على معنى SSID وعنوان IP وعنوان MAC وبوابة الراوتر"
    ),
    DEVICE_SCANNING(
        1,
        "اكتشاف الأجهزة وإدارتها",
        "2. رادار الأجهزة",
        "كيف يكتشف التطبيق كل جهاز متصل، وكيف يمكنك إيقاف الإنترنت أو إيقاظ الأجهزة عن بُعد"
    ),
    SPEED_AND_PING(
        2,
        "اختبار السرعة وزمن الاستجابة",
        "3. السرعة والـ Ping",
        "جرّب محاكي السرعة والـ Ping لتفهم تأثيرهما على الألعاب ومكالمات الفيديو والبث 4K"
    ),
    SECURITY_AND_INTRUDERS(
        3,
        "الأمان وكشف المتسللين",
        "4. درع الحماية",
        "تعلّم كيف تحمي شبكتك من الأجهزة الغريبة والمنافذ الخطرة المفتوحة"
    ),
    TOOLS_AND_LAUNCH(
        4,
        "أدوات التشخيص والانطلاق",
        "5. الأدوات والبدء",
        "استكشف أدوات Ping و Traceroute و Port Scanner وابدأ استخدام التطبيق فوراً"
    )
}

private data class NetworkConceptDetail(
    val key: String,
    val title: String,
    val shortCode: String,
    val liveExample: String,
    val technicalExplanation: String,
    val realWorldAnalogy: String,
    val whyItMatters: String,
    val accentColor: Color,
    val icon: ImageVector
)

@Composable
fun OnboardingTutorialScreen(
    overview: NetworkOverviewInfo,
    onFinishTutorial: () -> Unit,
    onJumpToFeature: (MainNavTab, Boolean) -> Unit
) {
    var currentStepIndex by remember { mutableIntStateOf(0) }
    val steps = TutorialStep.entries
    val activeStep = steps[currentStepIndex]

    BackHandler {
        if (currentStepIndex > 0) {
            currentStepIndex--
        } else {
            onFinishTutorial()
        }
    }

    val progress by animateFloatAsState(
        targetValue = (currentStepIndex + 1).toFloat() / steps.size.toFloat(),
        animationSpec = tween(300),
        label = "onboarding_progress"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
            .testTag("onboarding_tutorial_screen")
    ) {
        // Top Header with Progress & Skip Button
        Surface(
            color = NavySurface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ElectricCyan.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "الدليل التفاعلي لتحليل الشبكات",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "الخطوة ${currentStepIndex + 1} من ${steps.size} • ${activeStep.arabicTitle}",
                                style = MaterialTheme.typography.bodySmall,
                                color = ElectricCyan
                            )
                        }
                    }

                    TextButton(
                        onClick = onFinishTutorial,
                        modifier = Modifier.testTag("onboarding_skip_button")
                    ) {
                        Text(
                            text = "تخطي للدخول",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق الدليل",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Step Progress Bar
                LinearProgressIndicator(
                    progress = { progress },
                    color = ElectricCyan,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )

                // Interactive Step Pills Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    itemsIndexed(steps) { idx, step ->
                        val isSelected = idx == currentStepIndex
                        val isCompleted = idx < currentStepIndex
                        Surface(
                            onClick = { currentStepIndex = idx },
                            shape = RoundedCornerShape(10.dp),
                            color = when {
                                isSelected -> ElectricCyan.copy(alpha = 0.22f)
                                isCompleted -> EmeraldOnline.copy(alpha = 0.14f)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            },
                            modifier = Modifier
                                .border(
                                    width = 1.dp,
                                    color = when {
                                        isSelected -> ElectricCyan
                                        isCompleted -> EmeraldOnline.copy(alpha = 0.5f)
                                        else -> Color.Transparent
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .testTag("onboarding_step_pill_$idx")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (isCompleted) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = EmeraldOnline,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Text(
                                    text = step.badgeTitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isSelected -> ElectricCyan
                                        isCompleted -> EmeraldOnline
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Main Interactive Content Area
        Box(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState = activeStep,
                label = "onboarding_step_content"
            ) { step ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Hero Banner Card for the current step
                    item {
                        TutorialStepHeroBanner(step = step)
                    }

                    when (step) {
                        TutorialStep.NETWORK_CONCEPTS -> {
                            item {
                                InteractiveConceptsStep(overview = overview)
                            }
                        }

                        TutorialStep.DEVICE_SCANNING -> {
                            item {
                                InteractiveDeviceScannerStep(
                                    overview = overview,
                                    onTryRealScanNow = {
                                        onJumpToFeature(MainNavTab.DEVICES, true)
                                    }
                                )
                            }
                        }

                        TutorialStep.SPEED_AND_PING -> {
                            item {
                                InteractiveSpeedAndPingSimulatorStep(
                                    onTryRealSpeedTest = {
                                        onJumpToFeature(MainNavTab.SPEED, false)
                                    }
                                )
                            }
                        }

                        TutorialStep.SECURITY_AND_INTRUDERS -> {
                            item {
                                InteractiveSecurityChallengeStep(
                                    onTryRealSecurityAudit = {
                                        onJumpToFeature(MainNavTab.SECURITY, false)
                                    }
                                )
                            }
                        }

                        TutorialStep.TOOLS_AND_LAUNCH -> {
                            item {
                                InteractiveToolsAndLaunchStep(
                                    onJumpToFeature = onJumpToFeature,
                                    onFinishTutorial = onFinishTutorial
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        // Bottom Navigation Bar (Previous / Next / Start Using App)
        Surface(
            color = NavySurface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentStepIndex > 0) {
                    OutlinedButton(
                        onClick = { currentStepIndex-- },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("onboarding_prev_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("السابق", color = ElectricCyan, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Spacer(modifier = Modifier.width(8.dp))
                }

                // Step Dots Indicator
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    steps.forEachIndexed { index, _ ->
                        val active = index == currentStepIndex
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(if (active) 24.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (active) ElectricCyan
                                    else MaterialTheme.colorScheme.outline
                                )
                        )
                    }
                }

                if (currentStepIndex < steps.lastIndex) {
                    Button(
                        onClick = { currentStepIndex++ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = NavyBackground
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("onboarding_next_button")
                    ) {
                        Text("التالي", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Button(
                        onClick = onFinishTutorial,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldOnline,
                            contentColor = NavyBackground
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("onboarding_finish_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ابدأ استخدام التطبيق", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TutorialStepHeroBanner(step: TutorialStep) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(ElectricCyan.copy(alpha = 0.65f), EmeraldOnline.copy(alpha = 0.35f))
                ),
                shape = RoundedCornerShape(22.dp)
            )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.img_onboarding_network_1793457295607),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = 0.28f,
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(22.dp))
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                NavyBackground.copy(alpha = 0.45f),
                                NavyBackground.copy(alpha = 0.92f)
                            )
                        )
                    )
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BadgeChip(
                    text = step.badgeTitle,
                    color = ElectricCyan
                )
                Text(
                    text = step.arabicTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = step.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Step 1: Interactive Network Concepts Explorer (SSID, IP Address, MAC Address, Gateway)
 */
@Composable
private fun InteractiveConceptsStep(overview: NetworkOverviewInfo) {
    val concepts = remember(overview) {
        listOf(
            NetworkConceptDetail(
                key = "SSID",
                title = "اسم الشبكة اللاسلكية (SSID)",
                shortCode = "Service Set Identifier",
                liveExample = overview.ssid,
                technicalExplanation = "هو الاسم العام الذي يبثه جهاز الراوتر لتتمكن الهواتف والحواسيب من العثور على شبكة الواي فاي والاتصال بها.",
                realWorldAnalogy = "يشبه \"اسم البرج السكني أو المبنى\" الذي تراه على اللافتة الخارجية لتعرف أنك في المكان الصحيح.",
                whyItMatters = "يساعدك في التأكد من اتصالك بشبكتك الحقيقية (وليس شبكة وهمية تحمل اسماً مشابهاً) ومعرفة التردد 2.4GHz أو 5GHz.",
                accentColor = ElectricCyan,
                icon = Icons.Default.SignalWifi4Bar
            ),
            NetworkConceptDetail(
                key = "IP",
                title = "عنوان الشبكة المنطقي (IP Address)",
                shortCode = "Internet Protocol v4",
                liveExample = overview.localIp,
                technicalExplanation = "رقم مكون من 4 مقاطع يوزعه الراوتر تلقائياً (عبر خدمة DHCP) لكل جهاز متصل لكي يتمكن من إرسال واستقبال البيانات داخل الشبكة.",
                realWorldAnalogy = "يشبه \"رقم الشقة أو المكتب داخل المبنى\"؛ يمكن أن يتغير إذا انتقلت أو أعدت الاتصال بالشبكة.",
                whyItMatters = "من خلال عنوان IP يمكنك عمل فحص استجابة (Ping)، أو فحص المنافذ المفتوحة، أو الوصول لصفحة إعدادات الجهاز.",
                accentColor = EmeraldOnline,
                icon = Icons.Default.Hub
            ),
            NetworkConceptDetail(
                key = "MAC",
                title = "العنوان الفيزيائي الفريد (MAC Address)",
                shortCode = "Media Access Control",
                liveExample = overview.bssid,
                technicalExplanation = "بصمة سداسية عشرية مكونة من 6 أزواج (مثل A4:2B:B0:E8:4C:01) محفورة في شريحة الشبكة من المصنع ولا تتكرر عالمياً.",
                realWorldAnalogy = "يشبه \"رقم الهوية الوطنية أو الرقم التسلسلي لجواز السفر\"؛ يبقى ثابتاً للجهاز حتى لو غيّر عنوان IP الخاص به.",
                whyItMatters = "يكشف لك التطبيق اسم الشركة المصنّعة للجهاز (Apple, Samsung, TP-Link) من أول 3 مقاطع في عنوان MAC، ويُستخدم لحظر المتسللين أو إيقاظ الحاسوب عن بُعد (Wake-on-LAN).",
                accentColor = AmberWarning,
                icon = Icons.Default.Fingerprint
            ),
            NetworkConceptDetail(
                key = "GATEWAY",
                title = "بوابة الراوتر والـ DNS (Gateway IP)",
                shortCode = "Default Router Gateway",
                liveExample = overview.gatewayIp,
                technicalExplanation = "هو عنوان IP الخاص بالراوتر الرئيسي الذي يربط أجهزتك المنزلية بشبكة الإنترنت العالمية ويترجم أسماء المواقع عبر DNS.",
                realWorldAnalogy = "يشبه \"البوابة الرئيسية ومكتب الاستقبال في المبنى\" الذي تمر عبره جميع الرسائل والطرود الخارجة والواردة.",
                whyItMatters = "فحص أمان البوابة (${overview.gatewayIp}) يضمن عدم وجود ثغرات في الراوتر أو تلاعب بخوادم DNS.",
                accentColor = PurpleAccent,
                icon = Icons.Default.Router
            )
        )
    }

    var selectedConceptIndex by remember { mutableIntStateOf(0) }
    val selectedConcept = concepts[selectedConceptIndex]

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Interactive Instruction Banner
        Surface(
            color = ElectricCyan.copy(alpha = 0.12f),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ElectricCyan.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.TouchApp,
                    contentDescription = null,
                    tint = ElectricCyan
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "اضغط على أي مصطلح في البطاقة التفاعلية أدناه لاستكشاف معناه وقيمته الحية في شبكتك:",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Interactive Selector Grid (2x2)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                concepts.take(2).forEachIndexed { idx, item ->
                    ConceptInteractiveTile(
                        concept = item,
                        isSelected = selectedConceptIndex == idx,
                        onClick = { selectedConceptIndex = idx },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                concepts.drop(2).forEachIndexed { idx, item ->
                    val realIdx = idx + 2
                    ConceptInteractiveTile(
                        concept = item,
                        isSelected = selectedConceptIndex == realIdx,
                        onClick = { selectedConceptIndex = realIdx },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Detailed Interactive Concept Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, selectedConcept.accentColor.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                .testTag("concept_detail_card_${selectedConcept.key.lowercase()}")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(selectedConcept.accentColor.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = selectedConcept.icon,
                                contentDescription = null,
                                tint = selectedConcept.accentColor,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = selectedConcept.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = selectedConcept.shortCode,
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = JetBrainsMonoFontFamily,
                                color = selectedConcept.accentColor
                            )
                        }
                    }
                }

                MonoMetricTag(
                    label = "القيمة الحية في شبكتك الآن (${selectedConcept.key})",
                    value = selectedConcept.liveExample,
                    accentColor = selectedConcept.accentColor,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = selectedConcept.technicalExplanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Analogy Box
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = AmberWarning,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "تشبيه مبسّط:",
                                style = MaterialTheme.typography.labelLarge,
                                color = AmberWarning,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = selectedConcept.realWorldAnalogy,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Why it matters
                Text(
                    text = "فائدته في التطبيق: ${selectedConcept.whyItMatters}",
                    style = MaterialTheme.typography.bodySmall,
                    color = selectedConcept.accentColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ConceptInteractiveTile(
    concept: NetworkConceptDetail,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) concept.accentColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface,
        modifier = modifier
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) concept.accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("concept_tile_${concept.key.lowercase()}")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = concept.icon,
                    contentDescription = null,
                    tint = concept.accentColor,
                    modifier = Modifier.size(22.dp)
                )
                BadgeChip(text = concept.key, color = concept.accentColor)
            }
            Text(
                text = concept.title.substringBefore(" ("),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = concept.liveExample,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = JetBrainsMonoFontFamily,
                color = concept.accentColor,
                maxLines = 1
            )
        }
    }
}

/**
 * Step 2: Interactive Device Discovery & Management Demo
 */
@Composable
private fun InteractiveDeviceScannerStep(
    overview: NetworkOverviewInfo,
    onTryRealScanNow: () -> Unit
) {
    var radarDemoActive by remember { mutableStateOf(true) }
    var demoLaptopPaused by remember { mutableStateOf(false) }
    var demoWolSent by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "كيف يعمل رادار اكتشاف الأجهزة؟",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "يرسل التطبيق نبضات سريعة لجميع العناوين في نطاق شبكتك (${overview.localIp.substringBeforeLast(".")}.1 إلى .254) ويقرأ جدول ARP ليكتشف كل جهاز متصل في ثوانٍ.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    AnimatedRadarScannerCircle(
                        isScanning = radarDemoActive,
                        onlineCount = if (demoLaptopPaused) 2 else 3
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                Text(
                    text = "جرّب التحكم التفاعلي في الجهاز أدناه (محاكاة فورية):",
                    style = MaterialTheme.typography.titleSmall,
                    color = ElectricCyan,
                    fontWeight = FontWeight.Bold
                )

                // Interactive Sandbox Device Card
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (demoLaptopPaused) AmberWarning else EmeraldOnline.copy(alpha = 0.5f),
                            RoundedCornerShape(16.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "MacBook Pro 16 - غرفة المكتب",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "IP: ${overview.localIp.substringBeforeLast(".")}.14  •  MAC: 3C:22:FB:91:40:12",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    color = ElectricCyan
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                StatusPulseDot(
                                    isOnline = !demoLaptopPaused,
                                    isPaused = demoLaptopPaused
                                )
                                BadgeChip(
                                    text = if (demoLaptopPaused) "موقوف مؤقتاً (Paused)" else "متصل (4ms)",
                                    color = if (demoLaptopPaused) AmberWarning else EmeraldOnline
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { demoLaptopPaused = !demoLaptopPaused },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (demoLaptopPaused) EmeraldOnline else AmberWarning,
                                    contentColor = NavyBackground
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("tutorial_demo_pause_button")
                            ) {
                                Icon(
                                    imageVector = if (demoLaptopPaused) Icons.Default.PlayCircleFilled else Icons.Default.PauseCircleFilled,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (demoLaptopPaused) "استئناف الإنترنت" else "جرّب الإيقاف المؤقت",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = {
                                    demoWolSent = true
                                    demoLaptopPaused = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElectricCyan,
                                    contentColor = NavyBackground
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("tutorial_demo_wol_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PowerSettingsNew,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("جرّب Wake on LAN", fontWeight = FontWeight.Bold)
                            }
                        }

                        AnimatedVisibility(visible = demoWolSent) {
                            Text(
                                text = "✓ تم إرسال الحزمة السحرية (Magic Packet FF:FF:FF:FF:FF:FF + 16x MAC) عبر منفذ UDP 9 لإيقاظ الحاسوب عن بُعد!",
                                style = MaterialTheme.typography.bodySmall,
                                color = EmeraldOnline,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = onTryRealScanNow,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Radar,
                        contentDescription = null,
                        tint = ElectricCyan
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("انتقل لشاشة الأجهزة وافحص شبكتك الآن", color = ElectricCyan, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Step 3: Interactive Speed, Ping & Jitter Simulator
 */
@Composable
private fun InteractiveSpeedAndPingSimulatorStep(
    onTryRealSpeedTest: () -> Unit
) {
    var simulatedDownloadMbps by remember { mutableFloatStateOf(160f) }
    var simulatedPingMs by remember { mutableFloatStateOf(16f) }

    val gamingScore = (100 - ((simulatedPingMs.roundToInt() - 10).coerceAtLeast(0) / 2)).coerceIn(35, 99)
    val streamingScore = when {
        simulatedDownloadMbps >= 100f -> 98
        simulatedDownloadMbps >= 50f -> 92
        simulatedDownloadMbps >= 25f -> 82
        else -> 55
    }
    val videoCallScore = ((gamingScore + streamingScore) / 2).coerceIn(40, 99)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
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
                    text = "محاكي السرعة وزمن الاستجابة (Ping vs Speed)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "هل تعلم أن الألعاب الأونلاين تعتمد على انخفاض زمن الاستجابة (Ping ms) أكثر من سرعة التحميل، بينما يعتمد البث 4K على سعة التحميل (Mbps)؟ حرّك المؤشرين لترى الفرق بنفسك:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Slider 1: Download Speed
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("سرعة التحميل (Download Speed):", style = MaterialTheme.typography.titleSmall)
                        Text(
                            text = "${simulatedDownloadMbps.roundToInt()} Mbps",
                            style = MaterialTheme.typography.labelLarge,
                            fontFamily = JetBrainsMonoFontFamily,
                            color = ElectricCyan
                        )
                    }
                    Slider(
                        value = simulatedDownloadMbps,
                        onValueChange = { simulatedDownloadMbps = it },
                        valueRange = 10f..300f,
                        colors = SliderDefaults.colors(
                            thumbColor = ElectricCyan,
                            activeTrackColor = ElectricCyan
                        ),
                        modifier = Modifier.testTag("tutorial_download_slider")
                    )
                }

                // Slider 2: Ping Latency
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("زمن الاستجابة (Ping Latency):", style = MaterialTheme.typography.titleSmall)
                        Text(
                            text = "${simulatedPingMs.roundToInt()} ms",
                            style = MaterialTheme.typography.labelLarge,
                            fontFamily = JetBrainsMonoFontFamily,
                            color = if (simulatedPingMs <= 35f) EmeraldOnline else if (simulatedPingMs <= 80f) AmberWarning else CoralDanger
                        )
                    }
                    Slider(
                        value = simulatedPingMs,
                        onValueChange = { simulatedPingMs = it },
                        valueRange = 5f..180f,
                        colors = SliderDefaults.colors(
                            thumbColor = AmberWarning,
                            activeTrackColor = AmberWarning
                        ),
                        modifier = Modifier.testTag("tutorial_ping_slider")
                    )
                }

                // Live Resulting Application Ratings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SimulatedAppQualityCard(
                        icon = Icons.Default.SportsEsports,
                        title = "الألعاب",
                        score = gamingScore,
                        modifier = Modifier.weight(1f)
                    )
                    SimulatedAppQualityCard(
                        icon = Icons.Default.Videocam,
                        title = "مكالمات الفيديو",
                        score = videoCallScore,
                        modifier = Modifier.weight(1f)
                    )
                    SimulatedAppQualityCard(
                        icon = Icons.Default.LiveTv,
                        title = "بث 4K UHD",
                        score = streamingScore,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedButton(
                    onClick = onTryRealSpeedTest,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = EmeraldOnline
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("اختبر سرعة شبكتك الحقيقية الآن", color = EmeraldOnline, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SimulatedAppQualityCard(
    icon: ImageVector,
    title: String,
    score: Int,
    modifier: Modifier = Modifier
) {
    val color = when {
        score >= 85 -> EmeraldOnline
        score >= 68 -> AmberWarning
        else -> CoralDanger
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
        Text(text = title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        Text(
            text = "$score%",
            style = MaterialTheme.typography.titleMedium,
            fontFamily = JetBrainsMonoFontFamily,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

/**
 * Step 4: Interactive Security & Intruder Challenge
 */
@Composable
private fun InteractiveSecurityChallengeStep(
    onTryRealSecurityAudit: () -> Unit
) {
    var intruderBlocked by remember { mutableStateOf(false) }
    val simulatedScore = if (intruderBlocked) 98 else 76

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "اختبار عملي: اكتشف المتسلل والثغرات",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "يفحص تبويب الأمان تشفير الواي فاي (WPA3/WPA2) والمنافذ الخطرة مثل Telnet (23) ويكشف أي جهاز غريب ينضم لشبكتك.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    BadgeChip(
                        text = "درجة الأمان: $simulatedScore%",
                        color = if (intruderBlocked) EmeraldOnline else AmberWarning
                    )
                }

                // Interactive Intruder Scenario Card
                Surface(
                    color = if (intruderBlocked) EmeraldOnline.copy(alpha = 0.12f) else CoralDanger.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (intruderBlocked) EmeraldOnline.copy(alpha = 0.5f) else CoralDanger.copy(alpha = 0.5f),
                            RoundedCornerShape(16.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (intruderBlocked) Icons.Default.GppGood else Icons.Default.GppBad,
                                contentDescription = null,
                                tint = if (intruderBlocked) EmeraldOnline else CoralDanger,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (intruderBlocked) {
                                        "تم تأمين الشبكة وحظر الجهاز الدخيل بنجاح!"
                                    } else {
                                        "تنبيه: جهاز غير معروف (Espressif IoT) فتح المنفذ 23 (Telnet)"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (intruderBlocked) EmeraldOnline else CoralDanger
                                )
                                Text(
                                    text = "IP: 192.168.1.179  •  MAC: 5C:CF:7F:D1:08:77",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = JetBrainsMonoFontFamily
                                )
                            }
                        }

                        Button(
                            onClick = { intruderBlocked = !intruderBlocked },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (intruderBlocked) ElectricCyan else CoralDanger,
                                contentColor = if (intruderBlocked) NavyBackground else Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("tutorial_demo_block_intruder_button")
                        ) {
                            Icon(
                                imageVector = if (intruderBlocked) Icons.Default.VerifiedUser else Icons.Default.Block,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (intruderBlocked) "تم الحظر (اضغط لإعادة التجربة)" else "جرّب حظر المتسلل ورفع درجة الأمان",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = onTryRealSecurityAudit,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = ElectricCyan
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("انتقل إلى الفحص الأمني الكامل للشبكة", color = ElectricCyan, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Step 5: Diagnostic Tools Overview & Direct Feature Launchpad
 */
@Composable
private fun InteractiveToolsAndLaunchStep(
    onJumpToFeature: (MainNavTab, Boolean) -> Unit,
    onFinishTutorial: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
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
                    text = "أدوات التشخيص الاحترافية في متناول يدك",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                ToolSummaryRow(
                    icon = Icons.Default.NetworkPing,
                    title = "أداة Ping",
                    desc = "قياس سرعة استجابة أي موقع أو جهاز وكشف نسبة فقدان الحزم (Packet Loss).",
                    color = EmeraldOnline
                )
                ToolSummaryRow(
                    icon = Icons.Default.AltRoute,
                    title = "أداة Traceroute",
                    desc = "تتبع القفزات (Hops) والموجهات التي تمر بها بياناتك من الراوتر حتى الخادم الهدف.",
                    color = ElectricCyan
                )
                ToolSummaryRow(
                    icon = Icons.Default.Radar,
                    title = "فاحص المنافذ (Port Scanner)",
                    desc = "فحص منافذ TCP المفتوحة (مثل 80 HTTP و 443 HTTPS و 22 SSH و 554 RTSP).",
                    color = PurpleAccent
                )
                ToolSummaryRow(
                    icon = Icons.Default.Dns,
                    title = "أداة DNS Lookup & Wake on LAN",
                    desc = "تحليل عناوين النطاقات IPv4/IPv6 وتشغيل الحواسيب عن بُعد بحزمة Magic Packet.",
                    color = AmberWarning
                )
            }
        }

        // Quick Launchpad Grid
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ElectricCyan.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "اختر وجهتك للبدء فوراً:",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickLaunchButton(
                        icon = Icons.Default.Devices,
                        label = "فحص الأجهزة",
                        color = ElectricCyan,
                        onClick = { onJumpToFeature(MainNavTab.DEVICES, true) },
                        modifier = Modifier.weight(1f)
                    )
                    QuickLaunchButton(
                        icon = Icons.Default.Speed,
                        label = "اختبار السرعة",
                        color = EmeraldOnline,
                        onClick = { onJumpToFeature(MainNavTab.SPEED, false) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickLaunchButton(
                        icon = Icons.Default.Security,
                        label = "الفحص الأمني",
                        color = AmberWarning,
                        onClick = { onJumpToFeature(MainNavTab.SECURITY, false) },
                        modifier = Modifier.weight(1f)
                    )
                    QuickLaunchButton(
                        icon = Icons.Default.Build,
                        label = "أدوات الشبكة",
                        color = PurpleAccent,
                        onClick = { onJumpToFeature(MainNavTab.TOOLS, false) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolSummaryRow(
    icon: ImageVector,
    title: String,
    desc: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(color.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickLaunchButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.16f),
        modifier = modifier.border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, style = MaterialTheme.typography.labelLarge, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
