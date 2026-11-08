package com.example.ui.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import com.example.model.CameraFeed
import com.example.model.CameraProtocol
import com.example.ui.theme.AlertRed
import com.example.ui.theme.SyrianGreenLight
import com.example.ui.theme.SyrianGreenPrimary
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@OptIn(UnstableApi::class)
@Composable
fun StreamPlayerView(
    camera: CameraFeed,
    modifier: Modifier = Modifier,
    isFocused: Boolean = false,
    showControlsOverlay: Boolean = true,
    onClick: (() -> Unit)? = null,
    onDoubleTap: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var isLiveVideoPlaying by remember { mutableStateOf(false) }

    // Digital Pan & Zoom gestures for surveillance examination
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // Live clock ticker
    var currentTimeString by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        while (true) {
            currentTimeString = sdf.format(Date())
            delay(1000)
        }
    }

    // Scanning radar line animation
    val infiniteTransition = rememberInfiniteTransition(label = "surveillance_scan")
    val scanProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan_line"
    )

    // Motion box simulation wobble
    val motionWobble by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "motion_wobble"
    )

    // ExoPlayer initialization if streamUrl is HTTP/HTTPS playable
    val isHttpPlayable = (camera.streamUrl.startsWith("http://") || camera.streamUrl.startsWith("https://")) &&
            !camera.streamUrl.endsWith(".mjpg") &&
            !camera.streamUrl.contains(".local")
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }

    DisposableEffect(camera.id, camera.streamUrl) {
        if (isHttpPlayable) {
            try {
                val httpDataSourceFactory = DefaultHttpDataSource.Factory()
                    .setUserAgent("Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                    .setAllowCrossProtocolRedirects(true)
                    .setConnectTimeoutMs(6000)
                    .setReadTimeoutMs(6000)
                val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)
                val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

                val player = ExoPlayer.Builder(context)
                    .setMediaSourceFactory(mediaSourceFactory)
                    .build().apply {
                        val mediaItem = MediaItem.fromUri(camera.streamUrl)
                        setMediaItem(mediaItem)
                        repeatMode = Player.REPEAT_MODE_ALL
                        playWhenReady = true
                        volume = if (camera.isAudioListening) 1f else 0f
                        addListener(object : Player.Listener {
                            override fun onPlaybackStateChanged(playbackState: Int) {
                                isLiveVideoPlaying = playbackState == Player.STATE_READY
                            }

                            override fun onPlayerError(error: PlaybackException) {
                                // Gracefully fall back to synthetic surveillance canvas on 403 or network failure
                                isLiveVideoPlaying = false
                            }
                        })
                        prepare()
                    }
                exoPlayer = player
            } catch (e: Exception) {
                isLiveVideoPlaying = false
            }
        }

        onDispose {
            try {
                exoPlayer?.stop()
                exoPlayer?.release()
            } catch (e: Exception) {
            }
            exoPlayer = null
        }
    }

    LaunchedEffect(camera.isAudioListening) {
        exoPlayer?.volume = if (camera.isAudioListening) 1f else 0f
    }

    Box(
        modifier = modifier
            .background(Color(0xFF070B09))
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) SyrianGreenLight else Color(0xFF1E2823),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 4f)
                    val maxOffset = (scale - 1f) * 200f
                    offsetX = (offsetX + pan.x).coerceIn(-maxOffset, maxOffset)
                    offsetY = (offsetY + pan.y).coerceIn(-maxOffset, maxOffset)
                }
            }
    ) {
        // Base Content Layer: ExoPlayer Video or Surveillance Canvas Generator
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offsetX
                    translationY = offsetY
                }
        ) {
            if (exoPlayer != null && isLiveVideoPlaying) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            useController = false
                            this.player = exoPlayer
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Tactical Simulated High-FPS Surveillance Canvas
                SyntheticSurveillanceCanvas(
                    camera = camera,
                    motionWobble = motionWobble,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // IR Night Vision Filter Layer
        if (camera.nightVisionEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x3500FF66))
            )
        }

        // Scanline & Phosphor Grid Overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            val h = size.height
            val w = size.width
            val lineY = scanProgress * h

            // Subtle scan radar beam
            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        SyrianGreenLight.copy(alpha = 0.35f),
                        Color.Transparent
                    ),
                    startY = lineY - 24f,
                    endY = lineY + 24f
                ),
                start = Offset(0f, lineY),
                end = Offset(w, lineY),
                strokeWidth = 3f
            )

            // Tactical Corner Crosshairs
            val crosshairLen = 16.dp.toPx()
            val cornerColor = if (camera.nightVisionEnabled) SyrianGreenLight else Color(0x66FFFFFF)
            val strokeW = 1.5.dp.toPx()

            // Top Left
            drawLine(cornerColor, Offset(12f, 12f), Offset(12f + crosshairLen, 12f), strokeW)
            drawLine(cornerColor, Offset(12f, 12f), Offset(12f, 12f + crosshairLen), strokeW)
            // Top Right
            drawLine(cornerColor, Offset(w - 12f, 12f), Offset(w - 12f - crosshairLen, 12f), strokeW)
            drawLine(cornerColor, Offset(w - 12f, 12f), Offset(w - 12f, 12f + crosshairLen), strokeW)
            // Bottom Left
            drawLine(cornerColor, Offset(12f, h - 12f), Offset(12f + crosshairLen, h - 12f), strokeW)
            drawLine(cornerColor, Offset(12f, h - 12f), Offset(12f, h - 12f - crosshairLen), strokeW)
            // Bottom Right
            drawLine(cornerColor, Offset(w - 12f, h - 12f), Offset(w - 12f - crosshairLen, h - 12f), strokeW)
            drawLine(cornerColor, Offset(w - 12f, h - 12f), Offset(w - 12f, h - 12f - crosshairLen), strokeW)

            // Center target crosshair if PTZ or focused
            if (isFocused) {
                val cx = w / 2
                val cy = h / 2
                drawCircle(
                    color = SyrianGreenLight.copy(alpha = 0.5f),
                    radius = 24.dp.toPx(),
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
                )
                drawLine(SyrianGreenLight.copy(alpha = 0.6f), Offset(cx - 30f, cy), Offset(cx + 30f, cy), 1f)
                drawLine(SyrianGreenLight.copy(alpha = 0.6f), Offset(cx, cy - 30f), Offset(cx, cy + 30f), 1f)
            }
        }

        // Surveillance HUD Telemetry Header & Status
        if (showControlsOverlay) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Telemetry Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Live badge, Recording dot, and Encryption shield
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Live badge
                        Box(
                            modifier = Modifier
                                .background(Color(0xCC000000), RoundedCornerShape(4.dp))
                                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(if (camera.isOnline) SyrianGreenLight else AlertRed, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (camera.isOnline) "LIVE" else "LOST",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        if (camera.isRecording) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(AlertRed.copy(alpha = 0.9f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(Color.White, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "REC",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }

                        if (camera.isEncrypted) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color(0xBB0A2518), RoundedCornerShape(4.dp))
                                    .border(0.8.dp, SyrianGreenLight, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "AES-256 Encrypted",
                                        tint = SyrianGreenLight,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "AES-256",
                                        color = SyrianGreenLight,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                    // Right: Protocol & Night Vision Chip
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (camera.nightVisionEnabled) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xCC003D1A), RoundedCornerShape(4.dp))
                                    .border(1.dp, SyrianGreenLight, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "IR NIGHT",
                                    color = SyrianGreenLight,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        Box(
                            modifier = Modifier
                                .background(Color(0xCC000000), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = camera.protocol.displayName,
                                color = Color(0xFFD1D5DB),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Bottom Telemetry Bar (Camera Name, Timestamp, Resolution & PTZ)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xDD000000))
                            ),
                            RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = camera.name,
                            color = Color.White,
                            fontSize = if (isFocused) 13.sp else 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = if (isFocused) currentTimeString else currentTimeString.takeLast(8),
                            color = SyrianGreenLight,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (isFocused && camera.locationName.isNotBlank()) {
                        Text(
                            text = camera.locationName,
                            color = Color(0xFF9EABA2),
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (camera.hasPtz && isFocused) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "PAN: ${camera.currentPan.toInt()}° | TILT: ${camera.currentTilt.toInt()}° | ZOOM: ${"%.1f".format(camera.currentZoom)}X",
                                color = SyrianGreenLight,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            if (camera.isPatrolling) {
                                Text(
                                    text = "PATROL: ACTIVE",
                                    color = WarningAmber,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * High-performance tactical surveillance simulation canvas when no external video URL is streaming.
 */
@Composable
private fun SyntheticSurveillanceCanvas(
    camera: CameraFeed,
    motionWobble: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Dark Surveillance Room / Street Gradient
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF16231C),
                    Color(0xFF0F1713),
                    Color(0xFF080C0A)
                ),
                center = Offset(w * 0.5f, h * 0.4f),
                radius = w * 0.8f
            )
        )

        // Architectural Ground / Perspective Lines
        val horizonY = h * 0.55f
        drawLine(
            color = Color(0xFF1E2D24),
            start = Offset(0f, horizonY),
            end = Offset(w, horizonY),
            strokeWidth = 2f
        )
        // Perspective Grid Lines
        for (i in 0..6) {
            val startX = w * (i / 6f)
            val endX = w * 0.5f + (i - 3) * (w * 0.45f)
            drawLine(
                color = Color(0xFF18241D),
                start = Offset(w * 0.5f, horizonY),
                end = Offset(endX, h),
                strokeWidth = 1.2f
            )
        }

        // Render distinct scenery according to camera location
        when (camera.simulatedScene) {
            "gate" -> {
                // Gate posts
                val gateW = w * 0.35f
                val gateLeft = w * 0.325f
                drawRect(Color(0xFF26382D), Offset(gateLeft, horizonY - h * 0.25f), Size(12.dp.toPx(), h * 0.25f))
                drawRect(Color(0xFF26382D), Offset(gateLeft + gateW, horizonY - h * 0.25f), Size(12.dp.toPx(), h * 0.25f))
                drawLine(
                    Color(0xFF2E4336),
                    Offset(gateLeft, horizonY - h * 0.2f),
                    Offset(gateLeft + gateW, horizonY - h * 0.2f),
                    4f
                )
            }
            "perimeter" -> {
                // Chainlink fence patterns
                for (fenceX in 0 until (w.toInt()) step 40) {
                    drawLine(
                        Color(0xFF203025),
                        Offset(fenceX.toFloat(), horizonY - 60f),
                        Offset(fenceX + 20f, horizonY),
                        1f
                    )
                }
            }
            "server" -> {
                // Server racks with blinking LED lights
                for (rack in 0..2) {
                    val rx = w * 0.2f + rack * (w * 0.25f)
                    drawRect(Color(0xFF1B2620), Offset(rx, horizonY - h * 0.35f), Size(w * 0.18f, h * 0.35f))
                    // Server LED arrays
                    for (led in 0..4) {
                        val ly = horizonY - h * 0.3f + led * (h * 0.06f)
                        drawCircle(SyrianGreenLight, 2.5f, Offset(rx + 12f, ly))
                        drawCircle(Color(0xFF0EA5E9), 2f, Offset(rx + 24f, ly))
                    }
                }
            }
        }

        // Dynamic Motion Tracking Box
        if (camera.motionDetectionEnabled && camera.isOnline) {
            val boxLeft = w * 0.42f + motionWobble * 2
            val boxTop = horizonY - h * 0.18f + (motionWobble * 0.5f)
            val boxW = w * 0.22f
            val boxH = h * 0.24f

            val motionColor = if (camera.nightVisionEnabled) SyrianGreenLight else AlertRed
            drawRect(
                color = motionColor.copy(alpha = 0.85f),
                topLeft = Offset(boxLeft, boxTop),
                size = Size(boxW, boxH),
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 6f))
                )
            )

            // Tracking bracket markers
            val bLen = 14f
            drawLine(motionColor, Offset(boxLeft, boxTop), Offset(boxLeft + bLen, boxTop), 3f)
            drawLine(motionColor, Offset(boxLeft, boxTop), Offset(boxLeft, boxTop + bLen), 3f)
            drawLine(motionColor, Offset(boxLeft + boxW, boxTop + boxH), Offset(boxLeft + boxW - bLen, boxTop + boxH), 3f)
            drawLine(motionColor, Offset(boxLeft + boxW, boxTop + boxH), Offset(boxLeft + boxW, boxTop + boxH - bLen), 3f)
        }
    }
}
