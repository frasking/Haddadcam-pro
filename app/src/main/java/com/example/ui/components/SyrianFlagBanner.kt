package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AlertRed
import com.example.ui.theme.SyrianBlack
import com.example.ui.theme.SyrianGreenLight
import com.example.ui.theme.SyrianGreenPrimary
import com.example.ui.theme.SyrianRedStars
import com.example.ui.theme.SyrianWhite
import kotlin.math.cos
import kotlin.math.sin

/**
 * Procedural Vector Drawing of the Syrian Revolutionary Green Flag:
 * Top stripe: Green (#007A3D)
 * Middle stripe: White with 3 sharp red 5-pointed stars
 * Bottom stripe: Black
 */
@Composable
fun SyrianRevolutionFlag(
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .border(0.8.dp, Color(0xFF33443B), RoundedCornerShape(4.dp))
    ) {
        val w = size.width
        val stripeH = size.height / 3f

        // Top Stripe: Green
        drawRect(color = Color(0xFF007A3D), topLeft = Offset(0f, 0f), size = Size(w, stripeH))

        // Middle Stripe: White
        drawRect(color = Color.White, topLeft = Offset(0f, stripeH), size = Size(w, stripeH))

        // Bottom Stripe: Black
        drawRect(color = Color(0xFF050806), topLeft = Offset(0f, stripeH * 2), size = Size(w, stripeH))

        // Draw 3 Red 5-Pointed Stars evenly centered on the white middle stripe
        val centerY = stripeH * 1.5f
        val starRadius = (stripeH * 0.32f).coerceAtLeast(4f)
        val starCenters = listOf(w * 0.28f, w * 0.50f, w * 0.72f)

        starCenters.forEach { cx ->
            val starPath = Path()
            val points = 5
            val innerRadius = starRadius * 0.45f
            val step = Math.PI / points

            for (i in 0 until (points * 2)) {
                val r = if (i % 2 == 0) starRadius else innerRadius
                val angle = i * step - Math.PI / 2
                val px = (cx + r * cos(angle)).toFloat()
                val py = (centerY + r * sin(angle)).toFloat()
                if (i == 0) starPath.moveTo(px, py) else starPath.lineTo(px, py)
            }
            starPath.close()
            drawPath(starPath, color = Color(0xFFD61820))
        }
    }
}

@Composable
fun SyrianFlagBanner(
    modifier: Modifier = Modifier,
    showDetailedCredits: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0E1612))
            .border(1.dp, Color(0xFF22352B), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Syrian Flag & Branding Titles
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SyrianRevolutionFlag(
                        modifier = Modifier
                            .size(width = 54.dp, height = 36.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "كام حداد برو",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(SyrianGreenLight.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "PRO V2.6",
                                    color = SyrianGreenLight,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                        Text(
                            text = "المخصص والتشفير الآمن - المراقبة الذكية",
                            color = Color(0xFF94A99E),
                            fontSize = 11.sp
                        )
                    }
                }

                // Open Source & Shield Badges
                Box(
                    modifier = Modifier
                        .background(Color(0xFF16251E), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF2B4135), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Open Source",
                            tint = SyrianGreenLight,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "مفتوح المصدر",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (showDetailedCredits) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF090E0B), RoundedCornerShape(8.dp))
                        .border(0.8.dp, Color(0xFF1E2D25), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "المصمم: فراس باسم حداد",
                            color = SyrianGreenLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Zero Telemetry",
                                tint = Color(0xFF0EA5E9),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "تشفير عسكري AES-256 محلي 100%",
                                color = Color(0xFF90A499),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
