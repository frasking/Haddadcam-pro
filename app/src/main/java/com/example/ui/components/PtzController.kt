package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.unit.sp
import com.example.model.CameraFeed
import com.example.ui.theme.SyrianGreenLight
import com.example.ui.theme.SyrianGreenPrimary
import com.example.ui.theme.WarningAmber

@Composable
fun PtzController(
    camera: CameraFeed,
    onPanTilt: (Float, Float) -> Unit,
    onZoomChange: (Float) -> Unit,
    onTogglePatrol: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF131A16), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF233029), RoundedCornerShape(12.dp))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title Bar & Patrol Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "التحكم في حركة الكاميرا (PTZ)",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "تحريك أفقي / عمودي / تقريب بصري",
                    color = Color(0xFF8BA094),
                    fontSize = 11.sp
                )
            }

            // Patrol Mode Button
            Button(
                onClick = onTogglePatrol,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (camera.isPatrolling) WarningAmber else Color(0xFF1E2B23),
                    contentColor = if (camera.isPatrolling) Color.Black else Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .height(34.dp)
                    .testTag("ptz_patrol_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = "Patrol Tour",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (camera.isPatrolling) "الدورية نشطة" else "جولة دورية",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Center PTZ Joystick and Zoom Slider Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 8-Way Directional Controller D-Pad
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .background(Color(0xFF0C120E), CircleShape)
                    .border(2.dp, Color(0xFF1E2E24), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Up
                PtzArrowButton(
                    icon = Icons.Default.KeyboardArrowUp,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                        .testTag("ptz_up_button")
                ) { onPanTilt(0f, 10f) }

                // Down
                PtzArrowButton(
                    icon = Icons.Default.KeyboardArrowDown,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp)
                        .testTag("ptz_down_button")
                ) { onPanTilt(0f, -10f) }

                // Left
                PtzArrowButton(
                    icon = Icons.Default.KeyboardArrowLeft,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 8.dp)
                        .testTag("ptz_left_button")
                ) { onPanTilt(-15f, 0f) }

                // Right
                PtzArrowButton(
                    icon = Icons.Default.KeyboardArrowRight,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp)
                        .testTag("ptz_right_button")
                ) { onPanTilt(15f, 0f) }

                // Center Reset to Center
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFF1E2D24), CircleShape)
                        .border(1.dp, SyrianGreenLight, CircleShape)
                        .clickable { onPanTilt(-camera.currentPan, -camera.currentTilt) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = "Center",
                        tint = SyrianGreenLight,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Zoom Controller Column
            Column(
                modifier = Modifier
                    .width(130.dp)
                    .background(Color(0xFF0D1410), RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFF223227), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "التقريب (Zoom)",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${"%.1f".format(camera.currentZoom)}X",
                    color = SyrianGreenLight,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { onZoomChange(camera.currentZoom - 0.5f) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF19251E), CircleShape)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Zoom out", tint = Color.White)
                    }

                    IconButton(
                        onClick = { onZoomChange(camera.currentZoom + 0.5f) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(SyrianGreenPrimary, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Zoom in", tint = Color.White)
                    }
                }

                Slider(
                    value = camera.currentZoom,
                    onValueChange = onZoomChange,
                    valueRange = 1.0f..5.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = SyrianGreenLight,
                        activeTrackColor = SyrianGreenPrimary,
                        inactiveTrackColor = Color(0xFF2A3A30)
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Preset Points
        Text(
            text = "المواقع المحفوظة مسبقاً (Presets):",
            color = Color(0xFFA5B8AD),
            fontSize = 11.sp,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "نقطة 1: البوابة" to Pair(0f, 0f),
                "نقطة 2: المدخل" to Pair(45f, -10f),
                "نقطة 3: الشارع" to Pair(-60f, 15f),
                "نقطة 4: السور" to Pair(120f, -5f)
            ).forEach { (label, coords) ->
                FilledTonalButton(
                    onClick = {
                        onPanTilt(coords.first - camera.currentPan, coords.second - camera.currentTilt)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF1A2620),
                        contentColor = SyrianGreenLight
                    )
                ) {
                    Text(text = label.substringBefore(":"), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PtzArrowButton(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(Color(0xFF1A2620))
            .border(1.dp, Color(0xFF2E4035), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SyrianGreenLight,
            modifier = Modifier.size(24.dp)
        )
    }
}
