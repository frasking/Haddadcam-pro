package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.CameraFeed
import com.example.model.CameraProtocol
import com.example.model.EventSeverity
import com.example.model.EventType
import com.example.model.MediaRecording
import com.example.model.SecurityEvent
import com.example.model.StreamQuality
import com.example.security.CryptoManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CameraRepository(context: Context? = null) {

    private val prefs: SharedPreferences? = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _cameras = MutableStateFlow<List<CameraFeed>>(loadPersistedCameras())
    val cameras: StateFlow<List<CameraFeed>> = _cameras.asStateFlow()

    private val _events = MutableStateFlow<List<SecurityEvent>>(emptyList())
    val events: StateFlow<List<SecurityEvent>> = _events.asStateFlow()

    private val _recordings = MutableStateFlow<List<MediaRecording>>(emptyList())
    val recordings: StateFlow<List<MediaRecording>> = _recordings.asStateFlow()

    private val _selectedCameraId = MutableStateFlow<String?>(_cameras.value.firstOrNull()?.id)
    val selectedCameraId: StateFlow<String?> = _selectedCameraId.asStateFlow()

    private fun loadPersistedCameras(): List<CameraFeed> {
        val jsonString = prefs?.getString(KEY_SAVED_CAMERAS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(jsonString)
            val list = mutableListOf<CameraFeed>()
            for (i in 0 until arr.length()) {
                list.add(parseCameraFeed(arr.getJSONObject(i)))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun persistCameras(list: List<CameraFeed>) {
        if (prefs == null) return
        try {
            val arr = JSONArray()
            list.forEach { arr.put(serializeCameraFeed(it)) }
            prefs.edit().putString(KEY_SAVED_CAMERAS, arr.toString()).apply()
        } catch (e: Exception) {
        }
    }

    fun selectCamera(id: String?) {
        _selectedCameraId.value = id
    }

    fun addCamera(feed: CameraFeed) {
        val updated = _cameras.value + feed
        _cameras.value = updated
        persistCameras(updated)
        if (_selectedCameraId.value == null) {
            _selectedCameraId.value = feed.id
        }
    }

    fun updateCamera(feed: CameraFeed) {
        val updated = _cameras.value.map { if (it.id == feed.id) feed else it }
        _cameras.value = updated
        persistCameras(updated)
    }

    fun deleteCamera(id: String) {
        val updated = _cameras.value.filterNot { it.id == id }
        _cameras.value = updated
        persistCameras(updated)
        if (_selectedCameraId.value == id) {
            _selectedCameraId.value = updated.firstOrNull()?.id
        }
    }

    fun toggleRecording(id: String): Boolean {
        var isNowRecording = false
        _cameras.value = _cameras.value.map {
            if (it.id == id) {
                isNowRecording = !it.isRecording
                it.copy(isRecording = isNowRecording)
            } else it
        }

        val camera = _cameras.value.find { it.id == id }
        if (camera != null) {
            if (isNowRecording) {
                addEvent(
                    SecurityEvent(
                        id = UUID.randomUUID().toString(),
                        cameraId = camera.id,
                        cameraName = camera.name,
                        timestamp = System.currentTimeMillis(),
                        type = EventType.MOTION,
                        severity = EventSeverity.LOW,
                        description = "بدء تسجيل مرئي يدوي للكاميرا"
                    )
                )
            } else {
                val now = System.currentTimeMillis()
                val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH)
                val fileName = "REC_${camera.name.replace(" ", "_")}_${dateFormat.format(Date(now))}.mp4"
                _recordings.value = listOf(
                    MediaRecording(
                        id = UUID.randomUUID().toString(),
                        cameraId = camera.id,
                        cameraName = camera.name,
                        timestamp = now,
                        isVideo = true,
                        durationSeconds = 45,
                        fileName = fileName,
                        fileSizeFormatted = "14.2 MB",
                        filePath = "/storage/surveillance/$fileName"
                    )
                ) + _recordings.value
            }
        }
        return isNowRecording
    }

    fun captureSnapshot(id: String): MediaRecording? {
        val camera = _cameras.value.find { it.id == id } ?: return null
        val now = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH)
        val fileName = "SNAP_${camera.name.replace(" ", "_")}_${dateFormat.format(Date(now))}.jpg"
        val item = MediaRecording(
            id = UUID.randomUUID().toString(),
            cameraId = camera.id,
            cameraName = camera.name,
            timestamp = now,
            isVideo = false,
            durationSeconds = 0,
            fileName = fileName,
            fileSizeFormatted = "2.8 MB",
            filePath = "/storage/surveillance/$fileName"
        )
        _recordings.value = listOf(item) + _recordings.value

        addEvent(
            SecurityEvent(
                id = UUID.randomUUID().toString(),
                cameraId = camera.id,
                cameraName = camera.name,
                timestamp = now,
                type = EventType.SECURITY_SHIELD,
                severity = EventSeverity.LOW,
                description = "تم التقاط لقطة فورية عالية الدقة مشفرة"
            )
        )
        return item
    }

    fun toggleNightVision(id: String) {
        _cameras.value = _cameras.value.map {
            if (it.id == id) it.copy(nightVisionEnabled = !it.nightVisionEnabled) else it
        }
    }

    fun toggleSiren(id: String): Boolean {
        var isRinging = false
        _cameras.value = _cameras.value.map {
            if (it.id == id) {
                isRinging = !it.isSirenRinging
                it.copy(isSirenRinging = isRinging)
            } else it
        }
        if (isRinging) {
            val camera = _cameras.value.find { it.id == id }
            if (camera != null) {
                addEvent(
                    SecurityEvent(
                        id = UUID.randomUUID().toString(),
                        cameraId = camera.id,
                        cameraName = camera.name,
                        timestamp = System.currentTimeMillis(),
                        type = EventType.TAMPER,
                        severity = EventSeverity.CRITICAL,
                        description = "تم إطلاق صافرة الإنذار الميدانية يدويًا!"
                    )
                )
            }
        }
        return isRinging
    }

    fun toggleAudioListening(id: String) {
        _cameras.value = _cameras.value.map {
            if (it.id == id) it.copy(isAudioListening = !it.isAudioListening) else it
        }
    }

    fun updatePtz(id: String, panDelta: Float, tiltDelta: Float) {
        _cameras.value = _cameras.value.map {
            if (it.id == id) {
                val newPan = (it.currentPan + panDelta).coerceIn(-180f, 180f)
                val newTilt = (it.currentTilt + tiltDelta).coerceIn(-90f, 90f)
                it.copy(currentPan = newPan, currentTilt = newTilt)
            } else it
        }
    }

    fun updateZoom(id: String, zoom: Float) {
        _cameras.value = _cameras.value.map {
            if (it.id == id) it.copy(currentZoom = zoom.coerceIn(1.0f, 5.0f)) else it
        }
    }

    fun togglePatrol(id: String) {
        _cameras.value = _cameras.value.map {
            if (it.id == id) it.copy(isPatrolling = !it.isPatrolling) else it
        }
    }

    fun toggleMotionDetection(id: String) {
        _cameras.value = _cameras.value.map {
            if (it.id == id) it.copy(motionDetectionEnabled = !it.motionDetectionEnabled) else it
        }
    }

    fun updateMotionSensitivity(id: String, sensitivity: Int) {
        _cameras.value = _cameras.value.map {
            if (it.id == id) it.copy(motionSensitivity = sensitivity.coerceIn(1, 100)) else it
        }
    }

    fun toggleMotionZone(id: String, row: Int, col: Int) {
        val key = "$row,$col"
        _cameras.value = _cameras.value.map {
            if (it.id == id) {
                val newZones = if (it.activeZones.contains(key)) {
                    it.activeZones - key
                } else {
                    it.activeZones + key
                }
                it.copy(activeZones = newZones)
            } else it
        }
    }

    fun addEvent(event: SecurityEvent) {
        _events.value = listOf(event) + _events.value.take(99)
    }

    fun clearEvents() {
        _events.value = emptyList()
    }

    fun deleteRecording(id: String) {
        _recordings.value = _recordings.value.filterNot { it.id == id }
    }

    fun exportEncryptedConfig(): String {
        val raw = _cameras.value.joinToString(";;;") {
            "${it.id}|${it.name}|${it.protocol.name}|${it.streamUrl}|${it.ipAddress}|${it.port}|${it.isEncrypted}"
        }
        return CryptoManager.encrypt(raw)
    }

    companion object {
        private const val PREFS_NAME = "haddad_cam_storage"
        private const val KEY_SAVED_CAMERAS = "saved_cameras_v1"

        @Volatile
        private var instance: CameraRepository? = null

        fun getInstance(context: Context): CameraRepository {
            return instance ?: synchronized(this) {
                instance ?: CameraRepository(context.applicationContext).also { instance = it }
            }
        }

        private fun serializeCameraFeed(feed: CameraFeed): JSONObject {
            val obj = JSONObject()
            obj.put("id", feed.id)
            obj.put("name", feed.name)
            obj.put("locationName", feed.locationName)
            obj.put("protocol", feed.protocol.name)
            obj.put("streamUrl", feed.streamUrl)
            obj.put("snapshotUrl", feed.snapshotUrl)
            obj.put("ipAddress", feed.ipAddress)
            obj.put("port", feed.port)
            obj.put("username", feed.username)
            obj.put("password", feed.password)
            obj.put("isEncrypted", feed.isEncrypted)
            obj.put("quality", feed.quality.name)
            obj.put("fps", feed.fps)
            obj.put("isOnline", feed.isOnline)
            obj.put("hasAudio", feed.hasAudio)
            obj.put("hasPtz", feed.hasPtz)
            obj.put("nightVisionEnabled", feed.nightVisionEnabled)
            obj.put("motionDetectionEnabled", feed.motionDetectionEnabled)
            obj.put("motionSensitivity", feed.motionSensitivity)
            val zonesArr = JSONArray()
            feed.activeZones.forEach { zonesArr.put(it) }
            obj.put("activeZones", zonesArr)
            obj.put("group", feed.group)
            obj.put("simulatedScene", feed.simulatedScene)
            return obj
        }

        private fun parseCameraFeed(obj: JSONObject): CameraFeed {
            val zones = mutableSetOf<String>()
            val zonesArr = obj.optJSONArray("activeZones")
            if (zonesArr != null) {
                for (i in 0 until zonesArr.length()) {
                    zones.add(zonesArr.getString(i))
                }
            }
            val protocolName = obj.optString("protocol", CameraProtocol.ONVIF.name)
            val protocol = try {
                CameraProtocol.valueOf(protocolName)
            } catch (e: Exception) {
                CameraProtocol.ONVIF
            }
            val qualityName = obj.optString("quality", StreamQuality.FHD_1080P.name)
            val quality = try {
                StreamQuality.valueOf(qualityName)
            } catch (e: Exception) {
                StreamQuality.FHD_1080P
            }

            return CameraFeed(
                id = obj.optString("id", UUID.randomUUID().toString()),
                name = obj.optString("name", "كاميرا مراقبة"),
                locationName = obj.optString("locationName", ""),
                protocol = protocol,
                streamUrl = obj.optString("streamUrl", ""),
                snapshotUrl = obj.optString("snapshotUrl", ""),
                ipAddress = obj.optString("ipAddress", "192.168.1.100"),
                port = obj.optInt("port", protocol.defaultPort),
                username = obj.optString("username", "admin"),
                password = obj.optString("password", ""),
                isEncrypted = obj.optBoolean("isEncrypted", true),
                quality = quality,
                fps = obj.optInt("fps", 30),
                isOnline = obj.optBoolean("isOnline", true),
                hasAudio = obj.optBoolean("hasAudio", true),
                hasPtz = obj.optBoolean("hasPtz", true),
                nightVisionEnabled = obj.optBoolean("nightVisionEnabled", false),
                motionDetectionEnabled = obj.optBoolean("motionDetectionEnabled", true),
                motionSensitivity = obj.optInt("motionSensitivity", 75),
                activeZones = zones,
                group = obj.optString("group", "الرئيسية"),
                simulatedScene = obj.optString("simulatedScene", "gate")
            )
        }
    }
}
