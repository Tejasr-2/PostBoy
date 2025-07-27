package com.webcamapp.mobile.power

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PowerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    
    private var batteryReceiver: BroadcastReceiver? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private val _batteryLevel = MutableStateFlow(0)
    val batteryLevel: StateFlow<Int> = _batteryLevel

    private val _isCharging = MutableStateFlow(false)
    val isCharging: StateFlow<Boolean> = _isCharging

    private val _batteryTemperature = MutableStateFlow(0f)
    val batteryTemperature: StateFlow<Float> = _batteryTemperature

    private val _isOverheating = MutableStateFlow(false)
    val isOverheating: StateFlow<Boolean> = _isOverheating

    private val _powerMode = MutableStateFlow(PowerMode.NORMAL)
    val powerMode: StateFlow<PowerMode> = _powerMode

    companion object {
        private const val TAG = "PowerManager"
        private const val OVERHEATING_THRESHOLD_CELSIUS = 45f
        private const val LOW_BATTERY_THRESHOLD = 15
        private const val CRITICAL_BATTERY_THRESHOLD = 5
    }

    init {
        registerBatteryReceiver()
        updateBatteryInfo()
    }

    private fun registerBatteryReceiver() {
        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_BATTERY_CHANGED -> {
                        updateBatteryInfo()
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
        }
        context.registerReceiver(batteryReceiver, filter)
    }

    private fun updateBatteryInfo() {
        val level = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val status = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)
        val temperature = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_TEMPERATURE) / 10f

        _batteryLevel.value = level
        _isCharging.value = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
        _batteryTemperature.value = temperature

        // Check for overheating
        val overheating = temperature > OVERHEATING_THRESHOLD_CELSIUS
        _isOverheating.value = overheating

        // Update power mode based on conditions
        updatePowerMode(level, overheating)

        Log.d(TAG, "Battery: ${level}%, Charging: ${_isCharging.value}, Temp: ${temperature}°C, Overheating: $overheating")
    }

    private fun updatePowerMode(batteryLevel: Int, overheating: Boolean) {
        val newPowerMode = when {
            overheating -> PowerMode.OVERHEATING
            batteryLevel <= CRITICAL_BATTERY_THRESHOLD -> PowerMode.CRITICAL_BATTERY
            batteryLevel <= LOW_BATTERY_THRESHOLD -> PowerMode.LOW_BATTERY
            else -> PowerMode.NORMAL
        }

        if (newPowerMode != _powerMode.value) {
            _powerMode.value = newPowerMode
            Log.d(TAG, "Power mode changed to: ${newPowerMode.name}")
        }
    }

    fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) {
            return
        }

        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "WebcamApp:CameraWakeLock"
        ).apply {
            acquire(10 * 60 * 1000L) // 10 minutes timeout
        }
        Log.d(TAG, "Wake lock acquired")
    }

    fun releaseWakeLock() {
        wakeLock?.let { lock ->
            if (lock.isHeld) {
                lock.release()
                Log.d(TAG, "Wake lock released")
            }
        }
        wakeLock = null
    }

    fun shouldReduceQuality(): Boolean {
        return _powerMode.value == PowerMode.LOW_BATTERY || 
               _powerMode.value == PowerMode.CRITICAL_BATTERY
    }

    fun shouldStopCamera(): Boolean {
        return _powerMode.value == PowerMode.OVERHEATING || 
               _powerMode.value == PowerMode.CRITICAL_BATTERY
    }

    fun getRecommendedFrameRate(): Int {
        return when (_powerMode.value) {
            PowerMode.NORMAL -> 30
            PowerMode.LOW_BATTERY -> 15
            PowerMode.CRITICAL_BATTERY -> 10
            PowerMode.OVERHEATING -> 5
        }
    }

    fun getRecommendedResolution(): com.webcamapp.mobile.data.model.VideoResolution {
        return when (_powerMode.value) {
            PowerMode.NORMAL -> com.webcamapp.mobile.data.model.VideoResolution.HD_720P
            PowerMode.LOW_BATTERY -> com.webcamapp.mobile.data.model.VideoResolution.SD_480P
            PowerMode.CRITICAL_BATTERY -> com.webcamapp.mobile.data.model.VideoResolution.SD_360P
            PowerMode.OVERHEATING -> com.webcamapp.mobile.data.model.VideoResolution.SD_360P
        }
    }

    fun isScreenDimmingRecommended(): Boolean {
        return _powerMode.value == PowerMode.LOW_BATTERY || 
               _powerMode.value == PowerMode.CRITICAL_BATTERY
    }

    fun getBatteryHealth(): BatteryHealth {
        return when {
            _isOverheating.value -> BatteryHealth.OVERHEATING
            _batteryLevel.value <= CRITICAL_BATTERY_THRESHOLD -> BatteryHealth.CRITICAL
            _batteryLevel.value <= LOW_BATTERY_THRESHOLD -> BatteryHealth.LOW
            _batteryLevel.value <= 30 -> BatteryHealth.MEDIUM
            else -> BatteryHealth.GOOD
        }
    }

    fun cleanup() {
        batteryReceiver?.let { receiver ->
            try {
                context.unregisterReceiver(receiver)
            } catch (e: Exception) {
                Log.w(TAG, "Error unregistering battery receiver", e)
            }
        }
        batteryReceiver = null
        releaseWakeLock()
    }
}

enum class PowerMode {
    NORMAL,
    LOW_BATTERY,
    CRITICAL_BATTERY,
    OVERHEATING
}

enum class BatteryHealth {
    GOOD,
    MEDIUM,
    LOW,
    CRITICAL,
    OVERHEATING
}