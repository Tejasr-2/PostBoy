package com.webcamapp.mobile.playback

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.webcamapp.mobile.data.model.Recording
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoPlaybackManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var mediaPlayer: MediaPlayer? = null
    private var currentRecording: Recording? = null

    private val _playbackState = MutableStateFlow(PlaybackState.STOPPED)
    val playbackState: StateFlow<PlaybackState> = _playbackState

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed

    private val _motionEvents = MutableStateFlow<List<MotionEventMarker>>(emptyList())
    val motionEvents: StateFlow<List<MotionEventMarker>> = _motionEvents

    private val _playbackAnalytics = MutableStateFlow(PlaybackAnalytics())
    val playbackAnalytics: StateFlow<PlaybackAnalytics> = _playbackAnalytics

    companion object {
        private const val TAG = "VideoPlaybackManager"
    }

    fun loadVideo(recording: Recording) {
        try {
            currentRecording = recording
            val videoFile = File(recording.filePath)
            
            if (!videoFile.exists()) {
                Log.e(TAG, "Video file not found: ${recording.filePath}")
                return
            }

            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, Uri.fromFile(videoFile))
                prepare()
                
                setOnPreparedListener {
                    _duration.value = duration.toLong()
                    _playbackState.value = PlaybackState.READY
                    Log.d(TAG, "Video loaded: ${recording.filePath}")
                }
                
                setOnCompletionListener {
                    _playbackState.value = PlaybackState.COMPLETED
                    _currentPosition.value = 0L
                }
                
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                    _playbackState.value = PlaybackState.ERROR
                    true
                }
            }

            // Load motion events for this recording
            loadMotionEvents(recording)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error loading video", e)
            _playbackState.value = PlaybackState.ERROR
        }
    }

    fun play() {
        mediaPlayer?.let { player ->
            if (!player.isPlaying) {
                player.start()
                _playbackState.value = PlaybackState.PLAYING
                startPositionTracking()
                Log.d(TAG, "Playback started")
            }
        }
    }

    fun pause() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                _playbackState.value = PlaybackState.PAUSED
                Log.d(TAG, "Playback paused")
            }
        }
    }

    fun stop() {
        mediaPlayer?.let { player ->
            player.stop()
            player.seekTo(0)
            _playbackState.value = PlaybackState.STOPPED
            _currentPosition.value = 0L
            Log.d(TAG, "Playback stopped")
        }
    }

    fun seekTo(position: Long) {
        mediaPlayer?.let { player ->
            player.seekTo(position.toInt())
            _currentPosition.value = position
            Log.d(TAG, "Seeked to position: $position")
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        if (speed in 0.25f..4.0f) {
            _playbackSpeed.value = speed
            mediaPlayer?.let { player ->
                player.playbackParams = player.playbackParams.setSpeed(speed)
            }
            Log.d(TAG, "Playback speed set to: $speed")
        }
    }

    fun jumpToMotionEvent(event: MotionEventMarker) {
        seekTo(event.timestamp)
        Log.d(TAG, "Jumped to motion event at: ${event.timestamp}")
    }

    fun nextMotionEvent(): MotionEventMarker? {
        val events = _motionEvents.value
        val currentPos = _currentPosition.value
        
        return events.find { it.timestamp > currentPos }
    }

    fun previousMotionEvent(): MotionEventMarker? {
        val events = _motionEvents.value
        val currentPos = _currentPosition.value
        
        return events.findLast { it.timestamp < currentPos }
    }

    fun getMotionEventsInRange(startTime: Long, endTime: Long): List<MotionEventMarker> {
        return _motionEvents.value.filter { 
            it.timestamp in startTime..endTime 
        }
    }

    private fun loadMotionEvents(recording: Recording) {
        // In a real implementation, this would load motion events from the database
        // For now, we'll create some sample events
        val events = listOf(
            MotionEventMarker(
                id = "1",
                timestamp = 5000L,
                confidence = 0.8f,
                description = "Motion detected"
            ),
            MotionEventMarker(
                id = "2",
                timestamp = 15000L,
                confidence = 0.9f,
                description = "Strong motion"
            ),
            MotionEventMarker(
                id = "3",
                timestamp = 25000L,
                confidence = 0.7f,
                description = "Light motion"
            )
        )
        _motionEvents.value = events
        Log.d(TAG, "Loaded ${events.size} motion events")
    }

    private fun startPositionTracking() {
        // Start a coroutine to track playback position
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            while (_playbackState.value == PlaybackState.PLAYING) {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        _currentPosition.value = player.currentPosition.toLong()
                        updatePlaybackAnalytics()
                    }
                }
                kotlinx.coroutines.delay(100) // Update every 100ms
            }
        }
    }

    private fun updatePlaybackAnalytics() {
        val currentAnalytics = _playbackAnalytics.value
        val updatedAnalytics = currentAnalytics.copy(
            totalPlayTime = currentAnalytics.totalPlayTime + 100L,
            videosPlayed = if (_currentPosition.value == 0L) currentAnalytics.videosPlayed + 1 else currentAnalytics.videosPlayed
        )
        _playbackAnalytics.value = updatedAnalytics
    }

    fun getPlaybackInfo(): PlaybackInfo {
        return PlaybackInfo(
            recording = currentRecording,
            currentPosition = _currentPosition.value,
            duration = _duration.value,
            playbackSpeed = _playbackSpeed.value,
            motionEventsCount = _motionEvents.value.size,
            isPlaying = _playbackState.value == PlaybackState.PLAYING
        )
    }

    fun exportPlaybackAnalytics(): PlaybackAnalyticsReport {
        val analytics = _playbackAnalytics.value
        return PlaybackAnalyticsReport(
            totalPlayTime = analytics.totalPlayTime,
            videosPlayed = analytics.videosPlayed,
            averagePlaybackSpeed = analytics.averagePlaybackSpeed,
            motionEventsViewed = analytics.motionEventsViewed,
            exportTime = System.currentTimeMillis()
        )
    }

    fun resetAnalytics() {
        _playbackAnalytics.value = PlaybackAnalytics()
        Log.d(TAG, "Reset playback analytics")
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        _playbackState.value = PlaybackState.STOPPED
        Log.d(TAG, "Playback manager released")
    }
}

enum class PlaybackState {
    STOPPED,
    READY,
    PLAYING,
    PAUSED,
    COMPLETED,
    ERROR
}

data class MotionEventMarker(
    val id: String,
    val timestamp: Long,
    val confidence: Float,
    val description: String
)

data class PlaybackInfo(
    val recording: Recording?,
    val currentPosition: Long,
    val duration: Long,
    val playbackSpeed: Float,
    val motionEventsCount: Int,
    val isPlaying: Boolean
)

data class PlaybackAnalytics(
    val totalPlayTime: Long = 0L,
    val videosPlayed: Int = 0,
    val averagePlaybackSpeed: Float = 1.0f,
    val motionEventsViewed: Int = 0
)

data class PlaybackAnalyticsReport(
    val totalPlayTime: Long,
    val videosPlayed: Int,
    val averagePlaybackSpeed: Float,
    val motionEventsViewed: Int,
    val exportTime: Long
)