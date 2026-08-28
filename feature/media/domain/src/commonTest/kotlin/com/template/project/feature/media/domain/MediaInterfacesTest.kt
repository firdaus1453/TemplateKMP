package com.template.project.feature.media.domain

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MediaInterfacesTest {

    private class FakeImagePicker(private val result: ByteArray?) : ImagePicker {
        override suspend fun pickImage(): ByteArray? = result
    }

    private class FakeCameraCapture(private val result: ByteArray?) : CameraCapture {
        override suspend fun takePhoto(): ByteArray? = result
    }

    @Test
    fun imagePickerReturnsBytesWhenAvailable() = runTest {
        val bytes = byteArrayOf(1, 2, 3)
        val picker = FakeImagePicker(bytes)
        val result = picker.pickImage()
        assertNotNull(result)
        assertEquals(3, result.size)
    }

    @Test
    fun imagePickerReturnsNullWhenCancelled() = runTest {
        val picker = FakeImagePicker(null)
        assertNull(picker.pickImage())
    }

    @Test
    fun cameraCaptureReturnsBytesWhenPhotoTaken() = runTest {
        val bytes = byteArrayOf(4, 5, 6, 7)
        val camera = FakeCameraCapture(bytes)
        val result = camera.takePhoto()
        assertNotNull(result)
        assertEquals(4, result.size)
    }

    @Test
    fun cameraCaptureReturnsNullWhenCancelled() = runTest {
        val camera = FakeCameraCapture(null)
        assertNull(camera.takePhoto())
    }
}
