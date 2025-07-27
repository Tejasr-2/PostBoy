package com.webcamapp.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.webcamapp.mobile.data.model.MotionEvent

@Entity(tableName = "motion_events")
data class MotionEventEntity(
    @PrimaryKey
    val id: String,
    val deviceId: String,
    val timestamp: Long,
    val duration: Long,
    val confidence: Float,
    val videoFilePath: String?,
    val thumbnailPath: String?,
    val isUploaded: Boolean
) {
    fun toMotionEvent(): MotionEvent = MotionEvent(
        id = id,
        deviceId = deviceId,
        timestamp = timestamp,
        duration = duration,
        confidence = confidence,
        videoFilePath = videoFilePath,
        thumbnailPath = thumbnailPath,
        isUploaded = isUploaded
    )

    companion object {
        fun fromMotionEvent(motionEvent: MotionEvent): MotionEventEntity = MotionEventEntity(
            id = motionEvent.id,
            deviceId = motionEvent.deviceId,
            timestamp = motionEvent.timestamp,
            duration = motionEvent.duration,
            confidence = motionEvent.confidence,
            videoFilePath = motionEvent.videoFilePath,
            thumbnailPath = motionEvent.thumbnailPath,
            isUploaded = motionEvent.isUploaded
        )
    }
}