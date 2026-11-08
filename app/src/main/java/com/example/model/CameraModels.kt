package com.example.model

enum class CameraProtocol(val displayName: String, val defaultPort: Int) {
    RTSP("RTSP مباشر", 554),
    ONVIF("WiFi ONVIF", 8000),
    IPTV_HLS("IPTV (HLS/m3u8)", 80),
    HTTP_MJPEG("HTTP / MJPEG", 8080),
    WIRED_DVR("سلكية / DVR / NVR", 37777),
    DEVICE_CAM("كاميرا الجهاز المحلية", 0)
}

enum class StreamQuality(val label: String, val resolution: String, val defaultBitrateKbps: Int) {
    UHD_4K("4K Ultra HD", "3840x2160", 8192),
    QHD_2K("2K Quad HD", "2560x1440", 4096),
    FHD_1080P("1080p Full HD", "1920x1080", 2500),
    HD_720P("720p HD", "1280x720", 1200),
    SD_480P("480p Sub-Stream", "854x480", 600)
}

data class MotionZone(
    val row: Int,
    val col: Int,
    val active: Boolean = true
)

data class CameraFeed(
    val id: String,
    val name: String,
    val locationName: String,
    val protocol: CameraProtocol,
    val streamUrl: String,
    val snapshotUrl: String = "",
    val ipAddress: String = "192.168.1.100",
    val port: Int = 554,
    val username: String = "admin",
    val password: String = "••••••••",
    val isEncrypted: Boolean = true,
    val quality: StreamQuality = StreamQuality.FHD_1080P,
    val fps: Int = 30,
    val isOnline: Boolean = true,
    val hasAudio: Boolean = true,
    val hasPtz: Boolean = true,
    val nightVisionEnabled: Boolean = false,
    val motionDetectionEnabled: Boolean = true,
    val motionSensitivity: Int = 75, // 1 - 100
    val activeZones: Set<String> = emptySet(), // format "row,col"
    val isRecording: Boolean = false,
    val isAudioListening: Boolean = false,
    val isSirenRinging: Boolean = false,
    val currentPan: Float = 0f, // -180f to 180f
    val currentTilt: Float = 0f, // -90f to 90f
    val currentZoom: Float = 1.0f, // 1.0f to 5.0f
    val isPatrolling: Boolean = false,
    val group: String = "الرئيسية",
    val simulatedScene: String = "gate" // gate, perimeter, entrance, storage, yard
)

enum class EventSeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}

enum class EventType(val titleAr: String) {
    MOTION("كشف حركة"),
    AUDIO("إنذار صوتي"),
    TAMPER("محاولة حجب / عبث"),
    OFFLINE("انقطاع الإشارة"),
    SECURITY_SHIELD("حظر اختراق")
}

data class SecurityEvent(
    val id: String,
    val cameraId: String,
    val cameraName: String,
    val timestamp: Long,
    val type: EventType,
    val severity: EventSeverity,
    val description: String,
    val snapshotUri: String? = null
)

data class MediaRecording(
    val id: String,
    val cameraId: String,
    val cameraName: String,
    val timestamp: Long,
    val isVideo: Boolean,
    val durationSeconds: Int = 0,
    val fileName: String,
    val fileSizeFormatted: String,
    val filePath: String
)

data class DiscoveredDevice(
    val ip: String,
    val port: Int,
    val protocol: CameraProtocol,
    val brand: String,
    val mac: String,
    val onvifServiceUrl: String
)
