package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CameraFeed
import com.example.ui.components.MotionZoneGrid
import com.example.ui.components.PtzController
import com.example.ui.components.TalkbackController
import com.example.ui.player.StreamPlayerView
import com.example.ui.theme.AlertRed
import com.example.ui.theme.SurveillanceBgDark
import com.example.ui.theme.SurveillanceSurfaceDark
import com.example.ui.theme.SyrianGreenLight
import com.example.ui.theme.SyrianGreenPrimary
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.SurveillanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraDetailScreen(
    viewModel: SurveillanceViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(ScreenDestination.DASHBOARD)
    }

    val cameras by viewModel.cameras.collectAsState()
    val selectedId by viewModel.selectedCameraId.collectAsState()
    val camera = cameras.find { it.id == selectedId } ?: cameras.firstOrNull()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("التحكم PTZ", "كشف الحركة", "الاتصال الصوتي", "التشفير والمواصفات")

    if (camera == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SurveillanceBgDark),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "لم يتم تحديد كاميرا", color = Color.White)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = camera.name,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (camera.isEncrypted) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Encrypted",
                                    tint = SyrianGreenLight,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Text(
                            text = "${camera.locationName} • ${camera.protocol.displayName} • ${camera.ipAddress}",
                            color = Color(0xFFA1B3A9),
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(ScreenDestination.DASHBOARD) },
                        modifier = Modifier.testTag("detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Edit Camera
                    IconButton(
                        onClick = { viewModel.openAddCamera(camera) },
                        modifier = Modifier.testTag("detail_edit_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Camera",
                            tint = SyrianGreenLight
                        )
                    }
                    // Delete Camera
                    IconButton(
                        onClick = { viewModel.deleteCamera(camera.id) },
                        modifier = Modifier.testTag("detail_delete_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Camera",
                            tint = AlertRed
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurveillanceSurfaceDark
                )
            )
        },
        containerColor = SurveillanceBgDark
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Main Focused Stream Player
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
                    .padding(8.dp)
            ) {
                StreamPlayerView(
                    camera = camera,
                    isFocused = true,
                    showControlsOverlay = true,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Tactical Action Buttons Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Snapshot
                TacticalActionButton(
                    icon = Icons.Default.CameraAlt,
                    label = "لقطة",
                    isActive = false,
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.captureSnapshot(camera.id)
                }

                // Record
                TacticalActionButton(
                    icon = Icons.Default.FiberManualRecord,
                    label = if (camera.isRecording) "إيقاف" else "تسجيل",
                    isActive = camera.isRecording,
                    activeColor = AlertRed,
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.toggleRecording(camera.id)
                }

                // Night Vision IR
                TacticalActionButton(
                    icon = Icons.Default.Nightlight,
                    label = "رؤية ليلية",
                    isActive = camera.nightVisionEnabled,
                    activeColor = SyrianGreenLight,
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.toggleNightVision(camera.id)
                }

                // Siren Alarm
                TacticalActionButton(
                    icon = Icons.Default.Warning,
                    label = "إنذار",
                    isActive = camera.isSirenRinging,
                    activeColor = AlertRed,
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.triggerSiren(camera.id)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tab Navigation for Controls
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color(0xFF101713),
                contentColor = SyrianGreenLight,
                edgePadding = 12.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = SyrianGreenLight
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) SyrianGreenLight else Color(0xFF8DA396)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Content
            Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                when (selectedTabIndex) {
                    0 -> {
                        // PTZ Controls
                        PtzController(
                            camera = camera,
                            onPanTilt = { pan, tilt -> viewModel.updatePtz(camera.id, pan, tilt) },
                            onZoomChange = { zoom -> viewModel.updateZoom(camera.id, zoom) },
                            onTogglePatrol = { viewModel.togglePatrol(camera.id) }
                        )
                    }
                    1 -> {
                        // Motion Zone Grid & Sensitivity
                        MotionZoneGrid(
                            camera = camera,
                            onToggleZone = { r, c -> viewModel.toggleMotionZone(camera.id, r, c) },
                            onToggleMotionDetection = { viewModel.toggleMotionDetection(camera.id) },
                            onSensitivityChange = { s -> viewModel.updateMotionSensitivity(camera.id, s) }
                        )
                    }
                    2 -> {
                        // Two-Way Audio Talkback Intercom
                        TalkbackController(
                            camera = camera,
                            onToggleListen = { viewModel.toggleAudioListening(camera.id) }
                        )
                    }
                    3 -> {
                        // Camera Stream Specs & AES-256 Encryption
                        CameraSpecsCard(camera = camera)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TacticalActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color = SyrianGreenPrimary,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) activeColor else Color(0xFF141F19),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.height(44.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CameraSpecsCard(camera: CameraFeed) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131A16)),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF233029))),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "بيانات الكاميرا والتشفير المخصص",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            listOf(
                "بروتوكول البث" to camera.protocol.displayName,
                "عنوان الشبكة (IP)" to "${camera.ipAddress}:${camera.port}",
                "دقة الشاشة" to "${camera.quality.resolution} (${camera.quality.label})",
                "معدل الإطارات (FPS)" to "${camera.fps} FPS",
                "نظام التشفير" to if (camera.isEncrypted) "AES-256-GCM عسكري محلي" else "غير مشفر",
                "رابط البث الحي" to camera.streamUrl,
                "اسم المستخدم" to camera.username,
                "المجموعة الأمنية" to camera.group
            ).forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = label, color = Color(0xFF8BA094), fontSize = 11.sp)
                    Text(
                        text = value,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
