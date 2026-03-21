package com.template.project.feature.media.domain

interface ImagePicker {
    suspend fun pickImage(): ByteArray?
}

interface CameraCapture {
    suspend fun takePhoto(): ByteArray?
}
