package com.webcamapp.mobile.data.local.dao

import androidx.room.*
import com.webcamapp.mobile.data.local.entity.DeviceEntity
import com.webcamapp.mobile.data.model.DeviceType
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    @Query("SELECT * FROM devices WHERE id = :deviceId")
    suspend fun getDeviceById(deviceId: String): DeviceEntity?

    @Query("SELECT * FROM devices WHERE userId = :userId")
    fun getDevicesByUserId(userId: String): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE deviceType = :deviceType")
    fun getDevicesByType(deviceType: DeviceType): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE pairingCode = :pairingCode")
    suspend fun getDeviceByPairingCode(pairingCode: String): DeviceEntity?

    @Query("SELECT * FROM devices WHERE isOnline = 1")
    fun getOnlineDevices(): Flow<List<DeviceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: DeviceEntity)

    @Update
    suspend fun updateDevice(device: DeviceEntity)

    @Delete
    suspend fun deleteDevice(device: DeviceEntity)

    @Query("DELETE FROM devices WHERE userId = :userId")
    suspend fun deleteDevicesByUserId(userId: String)

    @Query("DELETE FROM devices")
    suspend fun deleteAllDevices()
}