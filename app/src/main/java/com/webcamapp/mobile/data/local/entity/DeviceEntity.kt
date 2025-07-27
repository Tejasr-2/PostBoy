package com.webcamapp.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.webcamapp.mobile.data.model.*

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val userId: String,
    val deviceType: DeviceType,
    val pairingCode: String,
    val isOnline: Boolean,
    val batteryLevel: Int,
    val storageRemaining: Long,
    val lastSeen: Long,
    val settings: DeviceSettings,
    val createdAt: Long
) {
    fun toDevice(): Device = Device(
        id = id,
        name = name,
        userId = userId,
        deviceType = deviceType,
        pairingCode = pairingCode,
        isOnline = isOnline,
        batteryLevel = batteryLevel,
        storageRemaining = storageRemaining,
        lastSeen = lastSeen,
        settings = settings,
        createdAt = createdAt
    )

    companion object {
        fun fromDevice(device: Device): DeviceEntity = DeviceEntity(
            id = device.id,
            name = device.name,
            userId = device.userId,
            deviceType = device.deviceType,
            pairingCode = device.pairingCode,
            isOnline = device.isOnline,
            batteryLevel = device.batteryLevel,
            storageRemaining = device.storageRemaining,
            lastSeen = device.lastSeen,
            settings = device.settings,
            createdAt = device.createdAt
        )
    }
}