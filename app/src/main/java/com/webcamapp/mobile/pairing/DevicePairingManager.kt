package com.webcamapp.mobile.pairing

import android.content.Context
import android.util.Log
import com.webcamapp.mobile.data.local.dao.DeviceDao
import com.webcamapp.mobile.data.model.Device
import com.webcamapp.mobile.data.model.DeviceType
import com.webcamapp.mobile.qr.QRCodeData
import com.webcamapp.mobile.qr.QRCodeManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DevicePairingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val deviceDao: DeviceDao,
    private val qrCodeManager: QRCodeManager
) {
    private val _pairedDevices = MutableStateFlow<List<Device>>(emptyList())
    val pairedDevices: StateFlow<List<Device>> = _pairedDevices

    private val _pairingState = MutableStateFlow(PairingState.IDLE)
    val pairingState: StateFlow<PairingState> = _pairingState

    private val _currentPairingDevice = MutableStateFlow<Device?>(null)
    val currentPairingDevice: StateFlow<Device?> = _currentPairingDevice

    companion object {
        private const val TAG = "DevicePairingManager"
        private const val PAIRING_CODE_LENGTH = 6
    }

    init {
        loadPairedDevices()
    }

    private suspend fun loadPairedDevices() {
        try {
            val devices = deviceDao.getDevicesByType(DeviceType.CAMERA)
            _pairedDevices.value = devices
            Log.d(TAG, "Loaded ${devices.size} paired devices")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading paired devices", e)
        }
    }

    suspend fun generatePairingCode(): String {
        val pairingCode = generateRandomCode(PAIRING_CODE_LENGTH)
        Log.d(TAG, "Generated pairing code: $pairingCode")
        return pairingCode
    }

    suspend fun pairWithDevice(qrData: String): PairingResult {
        _pairingState.value = PairingState.PAIRING

        return try {
            // Parse QR code data
            val qrCodeData = qrCodeManager.parseQRCodeData(qrData)
            if (qrCodeData == null) {
                _pairingState.value = PairingState.FAILED
                return PairingResult.Failed("Invalid QR code data")
            }

            // Validate QR code
            if (!qrCodeManager.validateQRCodeData(qrData)) {
                _pairingState.value = PairingState.FAILED
                return PairingResult.Failed("Invalid QR code format")
            }

            // Check if QR code is expired
            if (qrCodeManager.isQRCodeExpired(qrData)) {
                _pairingState.value = PairingState.FAILED
                return PairingResult.Failed("QR code has expired")
            }

            when (qrCodeData) {
                is QRCodeData.DeviceInfo -> {
                    pairWithDeviceInfo(qrCodeData)
                }
                is QRCodeData.Pairing -> {
                    pairWithPairingData(qrCodeData)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error pairing with device", e)
            _pairingState.value = PairingState.FAILED
            PairingResult.Failed("Pairing failed: ${e.message}")
        }
    }

    private suspend fun pairWithDeviceInfo(deviceInfo: QRCodeData.DeviceInfo): PairingResult {
        try {
            // Check if device is already paired
            val existingDevice = deviceDao.getDeviceById(deviceInfo.deviceId)
            if (existingDevice != null) {
                _pairingState.value = PairingState.FAILED
                return PairingResult.Failed("Device is already paired")
            }

            // Create new device
            val device = Device(
                id = deviceInfo.deviceId,
                name = deviceInfo.deviceName,
                userId = "", // Will be set by the user repository
                deviceType = DeviceType.CAMERA,
                pairingCode = deviceInfo.pairingCode,
                isOnline = deviceInfo.isOnline,
                batteryLevel = deviceInfo.batteryLevel,
                storageRemaining = deviceInfo.storageRemaining,
                lastSeen = deviceInfo.lastSeen,
                settings = null, // Will be set later
                createdAt = System.currentTimeMillis()
            )

            // Save device to database
            deviceDao.insertDevice(device)

            // Update paired devices list
            loadPairedDevices()

            _currentPairingDevice.value = device
            _pairingState.value = PairingState.SUCCESS

            Log.d(TAG, "Successfully paired with device: ${device.name}")
            return PairingResult.Success(device)

        } catch (e: Exception) {
            Log.e(TAG, "Error pairing with device info", e)
            _pairingState.value = PairingState.FAILED
            return PairingResult.Failed("Failed to save device: ${e.message}")
        }
    }

    private suspend fun pairWithPairingData(pairingData: QRCodeData.Pairing): PairingResult {
        try {
            // Check if device is already paired
            val existingDevice = deviceDao.getDeviceById(pairingData.deviceId)
            if (existingDevice != null) {
                _pairingState.value = PairingState.FAILED
                return PairingResult.Failed("Device is already paired")
            }

            // Create new device with pairing data
            val device = Device(
                id = pairingData.deviceId,
                name = pairingData.deviceName,
                userId = "", // Will be set by the user repository
                deviceType = DeviceType.CAMERA,
                pairingCode = pairingData.pairingCode,
                isOnline = true, // Assume online when pairing
                batteryLevel = 100, // Default value
                storageRemaining = 0L, // Will be updated later
                lastSeen = System.currentTimeMillis(),
                settings = null, // Will be set later
                createdAt = System.currentTimeMillis()
            )

            // Save device to database
            deviceDao.insertDevice(device)

            // Update paired devices list
            loadPairedDevices()

            _currentPairingDevice.value = device
            _pairingState.value = PairingState.SUCCESS

            Log.d(TAG, "Successfully paired with device: ${device.name}")
            return PairingResult.Success(device)

        } catch (e: Exception) {
            Log.e(TAG, "Error pairing with pairing data", e)
            _pairingState.value = PairingState.FAILED
            return PairingResult.Failed("Failed to save device: ${e.message}")
        }
    }

    suspend fun unpairDevice(deviceId: String): Boolean {
        return try {
            val device = deviceDao.getDeviceById(deviceId)
            if (device != null) {
                deviceDao.deleteDevice(deviceId)
                loadPairedDevices()
                
                if (_currentPairingDevice.value?.id == deviceId) {
                    _currentPairingDevice.value = null
                }
                
                Log.d(TAG, "Successfully unpaired device: ${device.name}")
                true
            } else {
                Log.w(TAG, "Device not found for unpairing: $deviceId")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error unpairing device", e)
            false
        }
    }

    suspend fun updateDeviceStatus(deviceId: String, isOnline: Boolean, batteryLevel: Int, storageRemaining: Long) {
        try {
            val device = deviceDao.getDeviceById(deviceId)
            if (device != null) {
                val updatedDevice = device.copy(
                    isOnline = isOnline,
                    batteryLevel = batteryLevel,
                    storageRemaining = storageRemaining,
                    lastSeen = System.currentTimeMillis()
                )
                deviceDao.updateDevice(updatedDevice)
                loadPairedDevices()
                
                Log.d(TAG, "Updated device status: $deviceId - Online: $isOnline, Battery: $batteryLevel%")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating device status", e)
        }
    }

    suspend fun getDeviceById(deviceId: String): Device? {
        return try {
            deviceDao.getDeviceById(deviceId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting device by ID", e)
            null
        }
    }

    suspend fun refreshPairedDevices() {
        loadPairedDevices()
    }

    fun resetPairingState() {
        _pairingState.value = PairingState.IDLE
        _currentPairingDevice.value = null
    }

    private fun generateRandomCode(length: Int): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }
}

enum class PairingState {
    IDLE,
    PAIRING,
    SUCCESS,
    FAILED
}

sealed class PairingResult {
    data class Success(val device: Device) : PairingResult()
    data class Failed(val error: String) : PairingResult()
}