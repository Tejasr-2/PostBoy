package com.webcamapp.mobile.ui.screens.settings

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.webcamapp.mobile.advanced.*
import com.webcamapp.mobile.optimization.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(name = "settings")
private val DATE_FORMAT_KEY = stringPreferencesKey("date_format")

@HiltViewModel
class AdvancedSettingsViewModel @Inject constructor(
    private val advancedCameraManager: AdvancedCameraManager,
    private val performanceOptimizer: PerformanceOptimizer,
    application: Application
) : ViewModel() {
    private val appContext = application.applicationContext
    private val _dateFormat = MutableStateFlow("yyyy-MM-dd HH:mm:ss")
    val dateFormat: StateFlow<String> = _dateFormat

    init {
        viewModelScope.launch {
            val prefs = appContext.dataStore.data.first()
            _dateFormat.value = prefs[DATE_FORMAT_KEY] ?: "yyyy-MM-dd HH:mm:ss"
        }
    }

    fun setDateFormat(format: String) {
        viewModelScope.launch {
            appContext.dataStore.edit { prefs ->
                prefs[DATE_FORMAT_KEY] = format
            }
            _dateFormat.value = format
        }
    }

    // State flows from managers
    val privacyZones: StateFlow<List<PrivacyZone>> = advancedCameraManager.privacyZones
    val aiMotionDetection: StateFlow<Boolean> = advancedCameraManager.aiMotionDetection
    val advancedRecording: StateFlow<AdvancedRecordingConfig> = advancedCameraManager.advancedRecording
    val performanceMode: StateFlow<PerformanceMode> = performanceOptimizer.performanceMode
    val optimizationSettings: StateFlow<OptimizationSettings> = performanceOptimizer.optimizationSettings

    // Computed analytics
    val analytics: StateFlow<AnalyticsReport> = advancedCameraManager.cameraAnalytics.map { analytics ->
        AnalyticsReport(
            totalMotionEvents = analytics.motionEvents,
            totalRecordings = analytics.recordingsStarted,
            totalRecordingTime = analytics.totalRecordingTime,
            privacyZoneEvents = analytics.privacyZoneEvents,
            aiMotionEvents = analytics.aiMotionEvents,
            averageAIConfidence = analytics.aiConfidence,
            uptime = System.currentTimeMillis() - analytics.startTime,
            activePrivacyZones = privacyZones.value.count { it.isActive }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyticsReport(0, 0, 0L, 0, 0, 0.0f, 0L, 0)
    )

    // Privacy Zone Management
    fun addPrivacyZone(zone: PrivacyZone) {
        viewModelScope.launch {
            advancedCameraManager.addPrivacyZone(zone)
        }
    }

    fun removePrivacyZone(zoneId: String) {
        viewModelScope.launch {
            advancedCameraManager.removePrivacyZone(zoneId)
        }
    }

    fun updatePrivacyZone(zone: PrivacyZone) {
        viewModelScope.launch {
            advancedCameraManager.updatePrivacyZone(zone)
        }
    }

    // AI Motion Detection
    fun enableAIMotionDetection(enabled: Boolean) {
        viewModelScope.launch {
            advancedCameraManager.enableAIMotionDetection(enabled)
        }
    }

    // Advanced Recording Configuration
    fun updateAdvancedRecordingConfig(config: AdvancedRecordingConfig) {
        viewModelScope.launch {
            advancedCameraManager.updateAdvancedRecordingConfig(config)
        }
    }

    fun enableScheduledRecording(schedule: RecordingSchedule) {
        viewModelScope.launch {
            advancedCameraManager.enableScheduledRecording(schedule)
        }
    }

    fun disableScheduledRecording() {
        viewModelScope.launch {
            advancedCameraManager.disableScheduledRecording()
        }
    }

    // Performance Optimization
    fun setPerformanceMode(mode: PerformanceMode) {
        viewModelScope.launch {
            performanceOptimizer.setManualPerformanceMode(mode)
        }
    }

    fun getOptimizationRecommendation(): OptimizationRecommendation {
        return performanceOptimizer.getOptimizationRecommendation()
    }

    fun getPerformanceReport(): PerformanceReport {
        return performanceOptimizer.getPerformanceReport()
    }

    // Analytics
    fun resetAnalytics() {
        viewModelScope.launch {
            advancedCameraManager.resetAnalytics()
            performanceOptimizer.resetMetrics()
        }
    }

    fun getAnalyticsReport(): AnalyticsReport {
        return advancedCameraManager.getAnalyticsReport()
    }

    // Utility Functions
    fun optimizeForBattery() {
        viewModelScope.launch {
            advancedCameraManager.optimizeForBattery()
        }
    }

    fun optimizeForQuality() {
        viewModelScope.launch {
            advancedCameraManager.optimizeForQuality()
        }
    }

    fun optimizeForStorage() {
        viewModelScope.launch {
            advancedCameraManager.optimizeForStorage()
        }
    }

    // Privacy Zone Helpers
    fun createPrivacyZone(
        name: String,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        blurType: BlurType = BlurType.BLACK
    ): PrivacyZone {
        return PrivacyZone(
            id = java.util.UUID.randomUUID().toString(),
            name = name,
            x = x,
            y = y,
            width = width,
            height = height,
            blurType = blurType
        )
    }

    fun getActivePrivacyZones(): List<PrivacyZone> {
        return privacyZones.value.filter { it.isActive }
    }

    fun getInactivePrivacyZones(): List<PrivacyZone> {
        return privacyZones.value.filter { !it.isActive }
    }

    // Recording Schedule Helpers
    fun createDailySchedule(
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        daysOfWeek: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7)
    ): RecordingSchedule {
        val startTime = (startHour * 60 + startMinute) * 60 * 1000L
        val endTime = (endHour * 60 + endMinute) * 60 * 1000L
        
        return RecordingSchedule(
            startTime = startTime,
            endTime = endTime,
            daysOfWeek = daysOfWeek
        )
    }

    fun createWorkdaySchedule(
        startHour: Int = 9,
        startMinute: Int = 0,
        endHour: Int = 17,
        endMinute: Int = 0
    ): RecordingSchedule {
        return createDailySchedule(
            startHour = startHour,
            startMinute = startMinute,
            endHour = endHour,
            endMinute = endMinute,
            daysOfWeek = listOf(2, 3, 4, 5, 6) // Monday to Friday
        )
    }

    fun createNightSchedule(
        startHour: Int = 22,
        startMinute: Int = 0,
        endHour: Int = 6,
        endMinute: Int = 0
    ): RecordingSchedule {
        return createDailySchedule(
            startHour = startHour,
            startMinute = startMinute,
            endHour = endHour,
            endMinute = endMinute
        )
    }

    // Performance Mode Helpers
    fun isHighPerformanceMode(): Boolean {
        return performanceMode.value == PerformanceMode.HIGH_PERFORMANCE
    }

    fun isBatterySaveMode(): Boolean {
        return performanceMode.value == PerformanceMode.BATTERY_SAVE || 
               performanceMode.value == PerformanceMode.ULTRA_SAVE
    }

    fun getCurrentFrameRate(): Int {
        return optimizationSettings.value.frameRate
    }

    fun getCurrentResolution(): String {
        return optimizationSettings.value.resolution.name
    }

    fun getCurrentQuality(): String {
        return optimizationSettings.value.quality.name
    }

    // Analytics Helpers
    fun getFormattedUptime(): String {
        val uptime = analytics.value.uptime
        val hours = uptime / (1000 * 60 * 60)
        val minutes = (uptime % (1000 * 60 * 60)) / (1000 * 60)
        return "${hours}h ${minutes}m"
    }

    fun getFormattedRecordingTime(): String {
        val totalMinutes = analytics.value.totalRecordingTime / (1000 * 60)
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return "${hours}h ${minutes}m"
    }

    fun getMotionEventRate(): Float {
        val uptimeHours = analytics.value.uptime / (1000 * 60 * 60)
        return if (uptimeHours > 0) {
            analytics.value.totalMotionEvents.toFloat() / uptimeHours
        } else {
            0.0f
        }
    }

    fun getRecordingEventRate(): Float {
        val uptimeHours = analytics.value.uptime / (1000 * 60 * 60)
        return if (uptimeHours > 0) {
            analytics.value.totalRecordings.toFloat() / uptimeHours
        } else {
            0.0f
        }
    }

    // Settings Validation
    fun validatePrivacyZone(zone: PrivacyZone): Boolean {
        return zone.name.isNotBlank() &&
               zone.x >= 0.0f && zone.x <= 1.0f &&
               zone.y >= 0.0f && zone.y <= 1.0f &&
               zone.width > 0.0f && zone.width <= 1.0f &&
               zone.height > 0.0f && zone.height <= 1.0f &&
               (zone.x + zone.width) <= 1.0f &&
               (zone.y + zone.height) <= 1.0f
    }

    fun validateRecordingSchedule(schedule: RecordingSchedule): Boolean {
        return schedule.startTime >= 0 &&
               schedule.endTime >= 0 &&
               schedule.startTime < schedule.endTime &&
               schedule.daysOfWeek.isNotEmpty() &&
               schedule.daysOfWeek.all { it in 1..7 }
    }

    // Export Functions
    fun exportSettings(): String {
        return buildString {
            appendLine("=== WebcamApp Advanced Settings ===")
            appendLine("Performance Mode: ${performanceMode.value}")
            appendLine("AI Motion Detection: ${aiMotionDetection.value}")
            appendLine("Privacy Zones: ${privacyZones.value.size}")
            appendLine("Active Privacy Zones: ${getActivePrivacyZones().size}")
            appendLine("Frame Rate: ${getCurrentFrameRate()} FPS")
            appendLine("Resolution: ${getCurrentResolution()}")
            appendLine("Quality: ${getCurrentQuality()}")
            appendLine("=== Analytics ===")
            appendLine("Uptime: ${getFormattedUptime()}")
            appendLine("Motion Events: ${analytics.value.totalMotionEvents}")
            appendLine("Recordings: ${analytics.value.totalRecordings}")
            appendLine("Recording Time: ${getFormattedRecordingTime()}")
            appendLine("Motion Event Rate: ${String.format("%.2f", getMotionEventRate())} events/hour")
            appendLine("Recording Event Rate: ${String.format("%.2f", getRecordingEventRate())} recordings/hour")
        }
    }
}