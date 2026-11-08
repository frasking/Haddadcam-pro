package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.ui.components.SyrianFlagBanner
import com.example.ui.components.SyrianRevolutionFlag
import com.example.ui.player.StreamPlayerView
import com.example.ui.theme.AlertRed
import com.example.ui.theme.SurveillanceBgDark
import com.example.ui.theme.SurveillanceBorder
import com.example.ui.theme.SurveillanceSurfaceDark
import com.example.ui.theme.SyrianGreenLight
import com.example.ui.theme.SyrianGreenPrimary
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.SurveillanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: SurveillanceViewModel,
    modifier: Modifier = Modifier
) {
    val cameras by viewModel.cameras.collectAsState()
    val events by viewModel.events.collectAsState()
    val gridColumns by viewModel.gridColumns.collectAsState()
    val selectedGroup by viewModel.selectedGroup.collectAsState()

    val groups = listOf("الكل") + cameras.map { it.group }.distinct()
    val filteredCameras = if (selectedGroup == "الكل") {
        cameras
    } else {
        cameras.filter { it.group == selectedGroup }
    }

    val onlineCount = cameras.count { it.isOnline }
    val recCount = cameras.count { it.isRecording }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SyrianRevolutionFlag(
                            modifier = Modifier
                                .size(width = 38.dp, height = 24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "كام حداد برو",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(SyrianGreenLight.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "المخصص",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SyrianGreenLight
                                    )
                                }
                            }
                            Text(
                                text = if (cameras.isEmpty()) "تصميم فراس باسم حداد | جاهز لإضافة كاميراتك" else "تصميم فراس باسم حداد | $onlineCount متصلة",
                                fontSize = 10.sp,
                                color = Color(0xFFA0B3A8)
                            )
                        }
                    }
                },
                actions = {
                    // Security Shield Screen Shortcut
                    IconButton(
                        onClick = { viewModel.navigateTo(ScreenDestination.SECURITY_SHIELD) },
                        modifier = Modifier.testTag("shield_shortcut_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Security Shield",
                            tint = Color(0xFF0EA5E9)
                        )
                    }

                    // Event Log Shortcut with badge
                    IconButton(
                        onClick = { viewModel.navigateTo(ScreenDestination.EVENT_LOGS) },
                        modifier = Modifier.testTag("events_shortcut_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (events.isNotEmpty()) {
                                    Badge(
                                        containerColor = AlertRed,
                                        contentColor = Color.White
                                    ) {
                                        Text(text = "${events.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Security Events",
                                tint = SyrianGreenLight
                            )
                        }
                    }

                    // Grid View Column Switcher (1x1, 2x2, 3x3)
                    IconButton(
                        onClick = {
                            val nextCols = if (gridColumns == 1) 2 else if (gridColumns == 2) 3 else 1
                            viewModel.setGridColumns(nextCols)
                        },
                        modifier = Modifier.testTag("grid_toggle_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = "Toggle Grid Columns",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${gridColumns}x",
                                color = SyrianGreenLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurveillanceSurfaceDark
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddCamera() },
                containerColor = SyrianGreenPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("add_camera_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Camera",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        containerColor = SurveillanceBgDark
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Group Filter Strip
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(groups) { group ->
                    val isSelected = selectedGroup == group
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setSelectedGroup(group) },
                        label = {
                            Text(
                                text = group,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SyrianGreenPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF16201B),
                            labelColor = Color(0xFFADBDB3)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = Color(0xFF283A30),
                            selectedBorderColor = SyrianGreenLight
                        )
                    )
                }
            }

            // Surveillance Multi-Grid Stream Canvas
            if (filteredCameras.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .background(Color(0xFF101914), RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFF22352B), RoundedCornerShape(16.dp))
                            .padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(SyrianGreenLight.copy(alpha = 0.15f), CircleShape)
                                .border(1.5.dp, SyrianGreenLight, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = SyrianGreenLight,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = if (cameras.isEmpty()) "لا توجد أي كاميرات مضافة حالياً" else "لا توجد كاميرات في هذه المجموعة",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        androidx.compose.material3.Button(
                            onClick = { viewModel.openAddCamera() },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = SyrianGreenPrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "إضافة كاميرا جديدة الآن (+)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(gridColumns),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("cameras_grid")
                ) {
                    items(filteredCameras, key = { it.id }) { camera ->
                        CameraGridTile(
                            camera = camera,
                            onTileClick = { viewModel.selectCamera(camera.id) },
                            onSnapshot = { viewModel.captureSnapshot(camera.id) },
                            onToggleRecord = { viewModel.toggleRecording(camera.id) },
                            onToggleAudio = { viewModel.toggleAudioListening(camera.id) },
                            onToggleNight = { viewModel.toggleNightVision(camera.id) },
                            onSiren = { viewModel.triggerSiren(camera.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CameraGridTile(
    camera: CameraFeed,
    onTileClick: () -> Unit,
    onSnapshot: () -> Unit,
    onToggleRecord: () -> Unit,
    onToggleAudio: () -> Unit,
    onToggleNight: () -> Unit,
    onSiren: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101713)),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF223028))),
        modifier = modifier
            .fillMaxWidth()
            .testTag("camera_tile_${camera.id}")
    ) {
        Column {
            // Live Stream Player View
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
            ) {
                StreamPlayerView(
                    camera = camera,
                    showControlsOverlay = true,
                    onClick = onTileClick,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Quick Tactical Action Bar under each camera tile
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0C120F))
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Snapshot Button
                IconButton(
                    onClick = onSnapshot,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Capture Snapshot",
                        tint = Color(0xFFBACBC1),
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Record Button
                IconButton(
                    onClick = onToggleRecord,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FiberManualRecord,
                        contentDescription = "Record",
                        tint = if (camera.isRecording) AlertRed else Color(0xFFBACBC1),
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Night Vision IR Toggle
                IconButton(
                    onClick = onToggleNight,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Nightlight,
                        contentDescription = "Night Vision",
                        tint = if (camera.nightVisionEnabled) SyrianGreenLight else Color(0xFFBACBC1),
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Audio Listen Toggle
                IconButton(
                    onClick = onToggleAudio,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Listen Audio",
                        tint = if (camera.isAudioListening) SyrianGreenLight else Color(0xFFBACBC1),
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Siren Alarm Alert Button
                IconButton(
                    onClick = onSiren,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Siren",
                        tint = if (camera.isSirenRinging) AlertRed else Color(0xFFBACBC1),
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Focus Fullscreen Button
                IconButton(
                    onClick = onTileClick,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "Fullscreen",
                        tint = SyrianGreenLight,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
