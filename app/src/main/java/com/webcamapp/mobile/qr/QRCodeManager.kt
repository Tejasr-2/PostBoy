package com.webcamapp.mobile.qr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.webcamapp.mobile.data.model.Device
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QRCodeManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "QRCodeManager"
        private const val QR_CODE_SIZE = 512
        private const val QR_CODE_MARGIN = 2
    }

    suspend fun generateDeviceQRCode(device: Device): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val qrData = createDeviceQRData(device)
            return@withContext generateQRCode(qrData, QR_CODE_SIZE)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating QR code for device: ${device.id}", e)
            return@withContext null
        }
    }

    suspend fun generatePairingQRCode(deviceId: String, deviceName: String, pairingCode: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val qrData = createPairingQRData(deviceId, deviceName, pairingCode)
            return@withContext generateQRCode(qrData, QR_CODE_SIZE)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating pairing QR code for device: $deviceId", e)
            return@withContext null
        }
    }

    private fun createDeviceQRData(device: Device): String {
        val jsonObject = JSONObject().apply {
            put("type", "device_info")
            put("deviceId", device.id)
            put("deviceName", device.name)
            put("deviceType", device.deviceType.name)
            put("pairingCode", device.pairingCode)
            put("isOnline", device.isOnline)
            put("batteryLevel", device.batteryLevel)
            put("storageRemaining", device.storageRemaining)
            put("lastSeen", device.lastSeen)
        }
        return jsonObject.toString()
    }

    private fun createPairingQRData(deviceId: String, deviceName: String, pairingCode: String): String {
        val jsonObject = JSONObject().apply {
            put("type", "pairing")
            put("deviceId", deviceId)
            put("deviceName", deviceName)
            put("pairingCode", pairingCode)
            put("timestamp", System.currentTimeMillis())
        }
        return jsonObject.toString()
    }

    private fun generateQRCode(data: String, size: Int): Bitmap? {
        return try {
            val hints = HashMap<EncodeHintType, Any>().apply {
                put(EncodeHintType.MARGIN, QR_CODE_MARGIN)
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
            }

            val bitMatrix = QRCodeWriter().encode(
                data,
                BarcodeFormat.QR_CODE,
                size,
                size,
                hints
            )

            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }

            bitmap
        } catch (e: WriterException) {
            Log.e(TAG, "Error writing QR code", e)
            null
        }
    }

    fun parseQRCodeData(qrData: String): QRCodeData? {
        return try {
            val jsonObject = JSONObject(qrData)
            val type = jsonObject.getString("type")

            when (type) {
                "device_info" -> {
                    QRCodeData.DeviceInfo(
                        deviceId = jsonObject.getString("deviceId"),
                        deviceName = jsonObject.getString("deviceName"),
                        deviceType = jsonObject.getString("deviceType"),
                        pairingCode = jsonObject.getString("pairingCode"),
                        isOnline = jsonObject.getBoolean("isOnline"),
                        batteryLevel = jsonObject.getInt("batteryLevel"),
                        storageRemaining = jsonObject.getLong("storageRemaining"),
                        lastSeen = jsonObject.getLong("lastSeen")
                    )
                }
                "pairing" -> {
                    QRCodeData.Pairing(
                        deviceId = jsonObject.getString("deviceId"),
                        deviceName = jsonObject.getString("deviceName"),
                        pairingCode = jsonObject.getString("pairingCode"),
                        timestamp = jsonObject.getLong("timestamp")
                    )
                }
                else -> {
                    Log.w(TAG, "Unknown QR code type: $type")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing QR code data", e)
            null
        }
    }

    fun validateQRCodeData(qrData: String): Boolean {
        return try {
            val jsonObject = JSONObject(qrData)
            val type = jsonObject.getString("type")
            
            when (type) {
                "device_info" -> {
                    jsonObject.has("deviceId") && 
                    jsonObject.has("deviceName") && 
                    jsonObject.has("pairingCode")
                }
                "pairing" -> {
                    jsonObject.has("deviceId") && 
                    jsonObject.has("deviceName") && 
                    jsonObject.has("pairingCode") &&
                    jsonObject.has("timestamp")
                }
                else -> false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error validating QR code data", e)
            false
        }
    }

    fun isQRCodeExpired(qrData: String, expirationTimeMs: Long = 5 * 60 * 1000): Boolean {
        return try {
            val jsonObject = JSONObject(qrData)
            val timestamp = jsonObject.getLong("timestamp")
            val currentTime = System.currentTimeMillis()
            (currentTime - timestamp) > expirationTimeMs
        } catch (e: Exception) {
            Log.e(TAG, "Error checking QR code expiration", e)
            true // Consider expired if we can't parse
        }
    }
}

sealed class QRCodeData {
    data class DeviceInfo(
        val deviceId: String,
        val deviceName: String,
        val deviceType: String,
        val pairingCode: String,
        val isOnline: Boolean,
        val batteryLevel: Int,
        val storageRemaining: Long,
        val lastSeen: Long
    ) : QRCodeData()

    data class Pairing(
        val deviceId: String,
        val deviceName: String,
        val pairingCode: String,
        val timestamp: Long
    ) : QRCodeData()
}