package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AlertSoundGenerator
import com.example.data.CameraRepository
import com.example.model.CameraFeed
import com.example.model.DiscoveredDevice
import com.example.model.MediaRecording
import com.example.model.SecurityEvent
import com.example.network.LanScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenDestination {
    DASHBOARD,
    CAMERA_DETAIL,
    ADD_EDIT_CAMERA,
    EVENT_LOGS,
    MEDIA_GALLERY,
    SECURITY_SHIELD,
    ABOUT_DESIGNER
}

class SurveillanceViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository: CameraRepository = CameraRepository.getInstance(application)

    val cameras: StateFlow<List<CameraFeed>> = repository.cameras
    val events: StateFlow<List<SecurityEvent>> = repository.events
    val recordings: StateFlow<List<MediaRecording>> = repository.recordings
    val selectedCameraId: StateFlow<String?> = repository.selectedCameraId

    private val _currentScreen = MutableStateFlow(ScreenDestination.DASHBOARD)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    // Multi-Grid layout mode: 1 = 1x1, 2 = 2x2, 3 = 3x3
    private val _gridColumns = MutableStateFlow(2)
    val gridColumns: StateFlow<Int> = _gridColumns.asStateFlow()

    private val _selectedGroup = MutableStateFlow("الكل")
    val selectedGroup: StateFlow<String> = _selectedGroup.asStateFlow()

    // LAN Scanner state
    private val _isScanningLan = MutableStateFlow(false)
    val isScanningLan: StateFlow<Boolean> = _isScanningLan.asStateFlow()

    private val _scanProgress = MutableStateFlow(0)
    val scanProgress: StateFlow<Int> = _scanProgress.asStateFlow()

    private val _scanStatusMessage = MutableStateFlow("")
    val scanStatusMessage: StateFlow<String> = _scanStatusMessage.asStateFlow()

    private val _discoveredCameras = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    val discoveredCameras: StateFlow<List<DiscoveredDevice>> = _discoveredCameras.asStateFlow()

    // Feedback message
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Camera being edited
    private val _cameraToEdit = MutableStateFlow<CameraFeed?>(null)
    val cameraToEdit: StateFlow<CameraFeed?> = _cameraToEdit.asStateFlow()

    fun navigateTo(destination: ScreenDestination) {
        _currentScreen.value = destination
    }

    fun selectCamera(id: String) {
        repository.selectCamera(id)
        _currentScreen.value = ScreenDestination.CAMERA_DETAIL
    }

    fun setGridColumns(cols: Int) {
        _gridColumns.value = cols.coerceIn(1, 3)
    }

    fun setSelectedGroup(group: String) {
        _selectedGroup.value = group
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun showMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    fun toggleRecording(cameraId: String) {
        val isNowRecording = repository.toggleRecording(cameraId)
        showMessage(if (isNowRecording) "تم بدء التسجيل المرئي للكاميرا" else "تم حفظ التسجيل في المعرض المشفر")
    }

    fun captureSnapshot(cameraId: String) {
        val snap = repository.captureSnapshot(cameraId)
        if (snap != null) {
            showMessage("تم التقاط لقطة فورية وحفظها في المعرض المشفر")
        }
    }

    fun toggleNightVision(cameraId: String) {
        repository.toggleNightVision(cameraId)
    }

    fun triggerSiren(cameraId: String) {
        val isRinging = repository.toggleSiren(cameraId)
        if (isRinging) {
            AlertSoundGenerator.startSiren(durationMs = 4000) {
                repository.toggleSiren(cameraId)
            }
            showMessage("⚠️ تم إطلاق صافرة الإنذار الصوتية الميدانية!")
        } else {
            AlertSoundGenerator.stopSiren()
            showMessage("تم إيقاف الإنذار")
        }
    }

    fun toggleAudioListening(cameraId: String) {
        repository.toggleAudioListening(cameraId)
    }

    fun updatePtz(cameraId: String, panDelta: Float, tiltDelta: Float) {
        repository.updatePtz(cameraId, panDelta, tiltDelta)
    }

    fun updateZoom(cameraId: String, zoom: Float) {
        repository.updateZoom(cameraId, zoom)
    }

    fun togglePatrol(cameraId: String) {
        repository.togglePatrol(cameraId)
    }

    fun toggleMotionDetection(cameraId: String) {
        repository.toggleMotionDetection(cameraId)
    }

    fun updateMotionSensitivity(cameraId: String, sensitivity: Int) {
        repository.updateMotionSensitivity(cameraId, sensitivity)
    }

    fun toggleMotionZone(cameraId: String, row: Int, col: Int) {
        repository.toggleMotionZone(cameraId, row, col)
    }

    fun deleteCamera(cameraId: String) {
        repository.deleteCamera(cameraId)
        showMessage("تم حذف الكاميرا من قائمة المراقبة")
        if (_currentScreen.value == ScreenDestination.CAMERA_DETAIL) {
            _currentScreen.value = ScreenDestination.DASHBOARD
        }
    }

    fun openAddCamera(camera: CameraFeed? = null) {
        _cameraToEdit.value = camera
        _currentScreen.value = ScreenDestination.ADD_EDIT_CAMERA
    }

    fun saveCamera(camera: CameraFeed) {
        val existing = cameras.value.find { it.id == camera.id }
        if (existing != null) {
            repository.updateCamera(camera)
            showMessage("تم تحديث إعدادات الكاميرا بنجاح")
        } else {
            repository.addCamera(camera)
            showMessage("تمت إضافة الكاميرا الجديدة إلى لوحة المراقبة")
        }
        _cameraToEdit.value = null
        _currentScreen.value = ScreenDestination.DASHBOARD
    }

    fun getDetectedSubnet(): String {
        return LanScanner.getLocalSubnetInfo()?.second ?: "192.168.1"
    }

    fun startLanScan(customSubnet: String? = null) {
        viewModelScope.launch {
            _isScanningLan.value = true
            _scanProgress.value = 0
            _discoveredCameras.value = emptyList()

            val detected = LanScanner.getLocalSubnetInfo()
            val subnetToScan = customSubnet?.ifBlank { null } ?: detected?.second ?: "192.168.1"

            val context = getApplication<android.app.Application>()
            val found = LanScanner.scanLocalSubnetReal(context, subnetToScan) { progress, status ->
                _scanProgress.value = progress
                _scanStatusMessage.value = status
            }

            _discoveredCameras.value = found
            _isScanningLan.value = false
            if (found.isEmpty()) {
                showMessage("اكتمل الفحص: لم يتم العثور على كاميرات في النطاق ($subnetToScan). تأكد من اتصال الكاميرات بالراوتر.")
            } else {
                showMessage("تم العثور على ${found.size} كاميرا حقيقية في الشبكة!")
            }
        }
    }

    fun clearEvents() {
        repository.clearEvents()
        showMessage("تم مسح سجل الأحداث الأمنية")
    }

    fun deleteRecording(id: String) {
        repository.deleteRecording(id)
        showMessage("تم حذف الملف من المعرض")
    }

    fun exportEncryptedBackup(): String {
        return repository.exportEncryptedConfig()
    }
}
