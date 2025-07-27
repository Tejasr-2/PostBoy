package com.webcamapp.mobile.data.local

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.webcamapp.mobile.data.local.dao.DeviceDao
import com.webcamapp.mobile.data.local.dao.MotionEventDao
import com.webcamapp.mobile.data.local.dao.UserDao
import com.webcamapp.mobile.data.local.entity.DeviceEntity
import com.webcamapp.mobile.data.local.entity.MotionEventEntity
import com.webcamapp.mobile.data.local.entity.UserEntity
import com.webcamapp.mobile.data.model.*
import kotlinx.coroutines.flow.Flow

@Database(
    entities = [
        UserEntity::class,
        DeviceEntity::class,
        MotionEventEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun deviceDao(): DeviceDao
    abstract fun motionEventDao(): MotionEventDao

    companion object {
        const val DATABASE_NAME = "webcam_app_db"
    }
}

class Converters {
    @TypeConverter
    fun fromUserRole(role: UserRole): String = role.name

    @TypeConverter
    fun toUserRole(role: String): UserRole = UserRole.valueOf(role)

    @TypeConverter
    fun fromDeviceType(type: DeviceType): String = type.name

    @TypeConverter
    fun toDeviceType(type: String): DeviceType = DeviceType.valueOf(type)

    @TypeConverter
    fun fromCameraSelection(selection: CameraSelection): String = selection.name

    @TypeConverter
    fun toCameraSelection(selection: String): CameraSelection = CameraSelection.valueOf(selection)

    @TypeConverter
    fun fromVideoResolution(resolution: VideoResolution): String = resolution.name

    @TypeConverter
    fun toVideoResolution(resolution: String): VideoResolution = VideoResolution.valueOf(resolution)

    @TypeConverter
    fun fromDeviceSettings(settings: DeviceSettings): String {
        return "${settings.cameraSelection.name}|${settings.resolution.name}|${settings.frameRate}|" +
                "${settings.motionDetectionEnabled}|${settings.motionSensitivity}|" +
                "${settings.continuousRecording}|${settings.motionRecordingDuration}|" +
                "${settings.screenDimming}|${settings.autoStart}|" +
                "${settings.scheduledStartTime}|${settings.scheduledEndTime}"
    }

    @TypeConverter
    fun toDeviceSettings(settingsString: String): DeviceSettings {
        val parts = settingsString.split("|")
        return DeviceSettings(
            cameraSelection = toCameraSelection(parts[0]),
            resolution = toVideoResolution(parts[1]),
            frameRate = parts[2].toInt(),
            motionDetectionEnabled = parts[3].toBoolean(),
            motionSensitivity = parts[4].toInt(),
            continuousRecording = parts[5].toBoolean(),
            motionRecordingDuration = parts[6].toInt(),
            screenDimming = parts[7].toBoolean(),
            autoStart = parts[8].toBoolean(),
            scheduledStartTime = if (parts[9] == "null") null else parts[9],
            scheduledEndTime = if (parts[10] == "null") null else parts[10]
        )
    }
}