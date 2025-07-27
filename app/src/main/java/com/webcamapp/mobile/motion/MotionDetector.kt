package com.webcamapp.mobile.motion

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MotionDetector @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var previousFrame: Bitmap? = null
    private var motionDetectionJob: Job? = null
    private val isRunning = AtomicBoolean(false)
    
    private val _motionDetected = MutableStateFlow(false)
    val motionDetected: StateFlow<Boolean> = _motionDetected
    
    private val _sensitivity = MutableStateFlow(50) // 0-100
    val sensitivity: StateFlow<Int> = _sensitivity
    
    private val _detectionZones = MutableStateFlow<List<Rect>>(emptyList())
    val detectionZones: StateFlow<List<Rect>> = _detectionZones
    
    private var onMotionDetectedCallback: ((com.webcamapp.mobile.data.model.MotionEvent) -> Unit)? = null

    companion object {
        private const val TAG = "MotionDetector"
        private const val DEFAULT_THRESHOLD = 0.1f
        private const val MIN_MOTION_DURATION_MS = 500L
        private const val FRAME_PROCESSING_DELAY_MS = 100L
    }

    fun startMotionDetection(
        onMotionDetected: (com.webcamapp.mobile.data.model.MotionEvent) -> Unit
    ) {
        if (isRunning.get()) {
            Log.w(TAG, "Motion detection is already running")
            return
        }

        onMotionDetectedCallback = onMotionDetected
        isRunning.set(true)
        
        motionDetectionJob = CoroutineScope(Dispatchers.Default + SupervisorJob()).launch {
            Log.d(TAG, "Motion detection started")
            
            while (isRunning.get()) {
                delay(FRAME_PROCESSING_DELAY_MS)
                // Frame processing will be triggered by processFrame method
            }
        }
    }

    fun stopMotionDetection() {
        if (!isRunning.get()) {
            return
        }

        isRunning.set(false)
        motionDetectionJob?.cancel()
        motionDetectionJob = null
        onMotionDetectedCallback = null
        previousFrame = null
        _motionDetected.value = false
        
        Log.d(TAG, "Motion detection stopped")
    }

    fun processFrame(currentFrame: Bitmap): Boolean {
        if (!isRunning.get()) {
            return false
        }

        return try {
            val previous = previousFrame
            if (previous == null) {
                previousFrame = currentFrame.copy(currentFrame.config, false)
                return false
            }

            val motionScore = calculateMotionScore(previous, currentFrame)
            val threshold = getThresholdFromSensitivity(_sensitivity.value)
            
            val hasMotion = motionScore > threshold
            
            if (hasMotion && !_motionDetected.value) {
                _motionDetected.value = true
                handleMotionDetected(motionScore)
            } else if (!hasMotion && _motionDetected.value) {
                _motionDetected.value = false
            }

            // Update previous frame
            previousFrame = currentFrame.copy(currentFrame.config, false)
            
            hasMotion
        } catch (e: Exception) {
            Log.e(TAG, "Error processing frame", e)
            false
        }
    }

    private fun calculateMotionScore(frame1: Bitmap, frame2: Bitmap): Float {
        if (frame1.width != frame2.width || frame1.height != frame2.height) {
            return 0f
        }

        val width = frame1.width
        val height = frame1.height
        val pixels1 = IntArray(width * height)
        val pixels2 = IntArray(width * height)
        
        frame1.getPixels(pixels1, 0, width, 0, 0, width, height)
        frame2.getPixels(pixels2, 0, width, 0, 0, width, height)

        var totalDifference = 0L
        var validPixels = 0

        // Apply detection zones if specified
        val zones = _detectionZones.value
        val shouldCheckZones = zones.isNotEmpty()

        for (y in 0 until height) {
            for (x in 0 until width) {
                // Check if pixel is in detection zone
                if (shouldCheckZones && !isPixelInZones(x, y, zones)) {
                    continue
                }

                val index = y * width + x
                val pixel1 = pixels1[index]
                val pixel2 = pixels2[index]

                // Calculate difference in RGB values
                val diffR = kotlin.math.abs(((pixel1 shr 16) and 0xFF) - ((pixel2 shr 16) and 0xFF))
                val diffG = kotlin.math.abs(((pixel1 shr 8) and 0xFF) - ((pixel2 shr 8) and 0xFF))
                val diffB = kotlin.math.abs((pixel1 and 0xFF) - (pixel2 and 0xFF))

                totalDifference += diffR + diffG + diffB
                validPixels++
            }
        }

        return if (validPixels > 0) {
            (totalDifference.toFloat() / (validPixels * 3 * 255)) // Normalize to 0-1
        } else {
            0f
        }
    }

    private fun isPixelInZones(x: Int, y: Int, zones: List<Rect>): Boolean {
        return zones.any { zone ->
            x >= zone.left && x <= zone.right && y >= zone.top && y <= zone.bottom
        }
    }

    private fun getThresholdFromSensitivity(sensitivity: Int): Float {
        // Convert sensitivity (0-100) to threshold (0.01-0.5)
        // Higher sensitivity = lower threshold = more sensitive
        return 0.5f - (sensitivity / 100f) * 0.49f
    }

    private fun handleMotionDetected(motionScore: Float) {
        val motionEvent = com.webcamapp.mobile.data.model.MotionEvent(
            id = java.util.UUID.randomUUID().toString(),
            deviceId = "", // Will be set by the service
            timestamp = System.currentTimeMillis(),
            confidence = motionScore,
            duration = 0L // Will be updated when motion stops
        )

        onMotionDetectedCallback?.invoke(motionEvent)
        Log.d(TAG, "Motion detected with score: $motionScore")
    }

    fun setSensitivity(sensitivity: Int) {
        val clampedSensitivity = sensitivity.coerceIn(0, 100)
        _sensitivity.value = clampedSensitivity
        Log.d(TAG, "Motion detection sensitivity set to: $clampedSensitivity")
    }

    fun setDetectionZones(zones: List<Rect>) {
        _detectionZones.value = zones
        Log.d(TAG, "Detection zones updated: ${zones.size} zones")
    }

    fun addDetectionZone(zone: Rect) {
        val currentZones = _detectionZones.value.toMutableList()
        currentZones.add(zone)
        _detectionZones.value = currentZones
    }

    fun removeDetectionZone(zone: Rect) {
        val currentZones = _detectionZones.value.toMutableList()
        currentZones.remove(zone)
        _detectionZones.value = currentZones
    }

    fun clearDetectionZones() {
        _detectionZones.value = emptyList()
    }

    fun isRunning(): Boolean = isRunning.get()

    fun getCurrentSensitivity(): Int = _sensitivity.value

    fun getDetectionZones(): List<Rect> = _detectionZones.value
}