package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CameraFeed
import com.example.ui.theme.AlertRed
import com.example.ui.theme.SyrianGreenLight
import com.example.ui.theme.SyrianGreenPrimary

@Composable
fun MotionZoneGrid(
    camera: CameraFeed,
    onToggleZone: (Int, Int) -> Unit,
    onToggleMotionDetection: () -> Unit,
    onSensitivityChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF131A16), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF233029), RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        // Toggle Switch & Sensitivity Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "نظام كشف الحركة والمناطق النشطة",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "حدد المربعات المراد مراقبتها لتفادي الإنذارات الكاذبة",
                    color = Color(0xFF8BA094),
                    fontSize = 11.sp
                )
            }

            Switch(
                checked = camera.motionDetectionEnabled,
                onCheckedChange = { onToggleMotionDetection() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = SyrianGreenPrimary,
                    uncheckedThumbColor = Color(0xFF888888),
                    uncheckedTrackColor = Color(0xFF222E27)
                ),
                modifier = Modifier.testTag("motion_detection_switch")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sensitivity Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "حساسية الحركة: ${camera.motionSensitivity}%",
                color = SyrianGreenLight,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (camera.motionSensitivity > 80) "حساسية فائقة (الرياح/الحيوانات)" else "حساسية قياسية",
                color = Color(0xFFA5B8AD),
                fontSize = 10.sp
            )
        }

        Slider(
            value = camera.motionSensitivity.toFloat(),
            onValueChange = { onSensitivityChange(it.toInt()) },
            valueRange = 10f..100f,
            colors = SliderDefaults.colors(
                thumbColor = SyrianGreenLight,
                activeTrackColor = SyrianGreenPrimary,
                inactiveTrackColor = Color(0xFF28362D)
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 6 x 6 Interactive Detection Matrix
        Text(
            text = "شبكة استشعار المناطق (المربعات الحمراء تفعل الإنذار):",
            color = Color(0xFFA5B8AD),
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(6.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF090E0B))
                .border(1.dp, Color(0xFF233228), RoundedCornerShape(8.dp))
                .padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (row in 0 until 5) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (col in 0 until 6) {
                        val isZoneActive = camera.activeZones.contains("$row,$col")
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1.6f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isZoneActive) AlertRed.copy(alpha = 0.45f)
                                    else Color(0xFF141F18)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isZoneActive) AlertRed else Color(0xFF26382D),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable { onToggleZone(row, col) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isZoneActive) "ON" else "",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
