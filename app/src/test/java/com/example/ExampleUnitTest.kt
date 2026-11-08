package com.example

import com.example.data.CameraRepository
import com.example.model.CameraFeed
import com.example.model.CameraProtocol
import com.example.security.CryptoManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun cryptoManager_encryptsAndDecryptsCorrectly() {
        val secret = "rtsp://admin:secure_password_123@192.168.1.50:554/live"
        val encrypted = CryptoManager.encrypt(secret)
        val decrypted = CryptoManager.decrypt(encrypted)
        assertEquals(secret, decrypted)
    }

    @Test
    fun cameraRepository_initializesAndAddsCamera() {
        val repo = CameraRepository()
        val initialCount = repo.cameras.value.size
        assertEquals(0, initialCount)

        val newCamera = CameraFeed(
            id = "test_cam_99",
            name = "كاميرا تجريبية",
            locationName = "المكتب",
            protocol = CameraProtocol.ONVIF,
            streamUrl = "http://192.168.1.99:8000/stream"
        )
        repo.addCamera(newCamera)
        assertEquals(initialCount + 1, repo.cameras.value.size)
    }
}
