package com.webcamapp.mobile.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MotionEvent(
    val id: String = "",
    val deviceId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val duration: Long = 0,
    val confidence: Float = 0.0f,
    val videoFilePath: String? = null,
    val thumbnailPath: String? = null,
    val isUploaded: Boolean = false
) : Parcelable

@Parcelize
data class Recording(
    val id: String = "",
    val deviceId: String = "",
    val filePath: String = "",
    val fileName: String = "",
    val fileSize: Long = 0,
    val duration: Long = 0,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = System.currentTimeMillis(),
    val recordingType: RecordingType = RecordingType.MOTION_TRIGGERED,
    val isUploaded: Boolean = false
) : Parcelable

enum class RecordingType {
    CONTINUOUS,
    MOTION_TRIGGERED,
    MANUAL
}