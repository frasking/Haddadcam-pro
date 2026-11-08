package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CameraFeed
import com.example.ui.theme.SyrianGreenLight
import com.example.ui.theme.SyrianGreenPrimary
import com.example.ui.theme.WarningAmber

@Composable
fun TalkbackController(
    camera: CameraFeed,
    onToggleListen: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isTalking by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "mic_wave")
    val waveScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave_scale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF131A16), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF233029), RoundedCornerShape(12.dp))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "الاتصال الصوتي ثنائي الاتجاه (Intercom)",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "استمع للصوت المحيط أو تحدث عبر مكبر الكاميرا",
                    color = Color(0xFF8BA094),
                    fontSize = 11.sp
                )
            }

            // Audio Listen Toggle
            Button(
                onClick = onToggleListen,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (camera.isAudioListening) SyrianGreenPrimary else Color(0xFF1C2720),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .height(34.dp)
                    .testTag("audio_listen_toggle")
            ) {
                Icon(
                    imageVector = if (camera.isAudioListening) Icons.Default.VolumeUp else Icons.Default.Hearing,
                    contentDescription = "Listen",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (camera.isAudioListening) "الاستماع نشط" else "تشغيل الاستماع",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Decibel Meter
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "مستوى الضجيج (42 dB):",
                color = Color(0xFFA5B8AD),
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.width(10.dp))
            LinearProgressIndicator(
                progress = { 0.42f },
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = SyrianGreenLight,
                trackColor = Color(0xFF223028)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Push-to-Talk Big Button
        Box(
            modifier = Modifier
                .size(100.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isTalking = true
                            tryAwaitRelease()
                            isTalking = false
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Pulsing wave when talking
            if (isTalking) {
                Box(
                    modifier = Modifier
                        .size(90.dp * waveScale)
                        .background(SyrianGreenLight.copy(alpha = 0.25f), CircleShape)
                )
            }

            Box(
                modifier = Modifier
                    .size(76.dp)
                    .background(
                        if (isTalking) SyrianGreenPrimary else Color(0xFF1A2620),
                        CircleShape
                    )
                    .border(
                        2.dp,
                        if (isTalking) Color.White else SyrianGreenLight,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Talk",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = if (isTalking) "جاري الإرسال" else "اضغط وتكلم",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "اضغط واستمر بالضغط للتحدث عبر مكبر صوت الكاميرا",
            color = Color(0xFF7A8E82),
            fontSize = 10.sp
        )
    }
}
