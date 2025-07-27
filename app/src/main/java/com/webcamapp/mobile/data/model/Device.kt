package com.webcamapp.mobile.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Device(
    val id: String = "",
    val name: String = "",
    val userId: String = "",
    val deviceType: DeviceType = DeviceType.CAMERA,
    val pairingCode: String = "",
    val isOnline: Boolean = false,
    val batteryLevel: Int = 0,
    val storageRemaining: Long = 0,
    val lastSeen: Long = System.currentTimeMillis(),
    val settings: DeviceSettings = DeviceSettings(),
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable

enum class DeviceType {
    CAMERA,
    VIEWER
}

@Parcelize
data class DeviceSettings(
    val cameraSelection: CameraSelection = CameraSelection.REAR,
    val resolution: VideoResolution = VideoResolution.HD_720P,
    val frameRate: Int = 30,
    val motionDetectionEnabled: Boolean = true,
    val motionSensitivity: Int = 50,
    val continuousRecording: Boolean = false,
    val motionRecordingDuration: Int = 30,
    val screenDimming: Boolean = true,
    val autoStart: Boolean = false,
    val scheduledStartTime: String? = null,
    val scheduledEndTime: String? = null
) : Parcelable

enum class CameraSelection {
    FRONT,
    REAR
}

enum class VideoResolution(val width: Int, val height: Int, val label: String) {
    SD_360P(640, 360, "360p"),
    SD_480P(854, 480, "480p"),
    HD_720P(1280, 720, "720p"),
    HD_1080P(1920, 1080, "1080p")
}