package com.webcamapp.mobile.data.local.dao

import androidx.room.*
import com.webcamapp.mobile.data.local.entity.MotionEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MotionEventDao {
    @Query("SELECT * FROM motion_events WHERE id = :eventId")
    suspend fun getMotionEventById(eventId: String): MotionEventEntity?

    @Query("SELECT * FROM motion_events WHERE deviceId = :deviceId ORDER BY timestamp DESC")
    fun getMotionEventsByDeviceId(deviceId: String): Flow<List<MotionEventEntity>>

    @Query("SELECT * FROM motion_events WHERE deviceId = :deviceId AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getMotionEventsByDeviceIdAndTimeRange(deviceId: String, startTime: Long, endTime: Long): Flow<List<MotionEventEntity>>

    @Query("SELECT * FROM motion_events WHERE isUploaded = 0")
    fun getUnuploadedMotionEvents(): Flow<List<MotionEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMotionEvent(motionEvent: MotionEventEntity)

    @Update
    suspend fun updateMotionEvent(motionEvent: MotionEventEntity)

    @Delete
    suspend fun deleteMotionEvent(motionEvent: MotionEventEntity)

    @Query("DELETE FROM motion_events WHERE deviceId = :deviceId")
    suspend fun deleteMotionEventsByDeviceId(deviceId: String)

    @Query("DELETE FROM motion_events WHERE timestamp < :timestamp")
    suspend fun deleteOldMotionEvents(timestamp: Long)

    @Query("SELECT COUNT(*) FROM motion_events WHERE deviceId = :deviceId")
    suspend fun getMotionEventCountByDeviceId(deviceId: String): Int
}