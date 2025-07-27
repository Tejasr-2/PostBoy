package com.webcamapp.mobile.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.webcamapp.mobile.R
import com.webcamapp.mobile.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class CameraService : Service() {

    @Inject
    lateinit var cameraManager: CameraManager

    @Inject
    lateinit var motionDetector: MotionDetector

    @Inject
    lateinit var recordingManager: RecordingManager

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isRecording = false
    private var isMotionDetectionEnabled = true

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "camera_service_channel"
        private const val CHANNEL_NAME = "Camera Service"

        const val ACTION_START_CAMERA = "com.webcamapp.mobile.START_CAMERA"
        const val ACTION_STOP_CAMERA = "com.webcamapp.mobile.STOP_CAMERA"
        const val ACTION_TOGGLE_RECORDING = "com.webcamapp.mobile.TOGGLE_RECORDING"
        const val ACTION_TOGGLE_MOTION_DETECTION = "com.webcamapp.mobile.TOGGLE_MOTION_DETECTION"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_CAMERA -> startCamera()
            ACTION_STOP_CAMERA -> stopCamera()
            ACTION_TOGGLE_RECORDING -> toggleRecording()
            ACTION_TOGGLE_MOTION_DETECTION -> toggleMotionDetection()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startCamera() {
        val notification = createNotification("Camera Active", "Camera is running in background")
        startForeground(NOTIFICATION_ID, notification)

        serviceScope.launch {
            try {
                cameraManager.startCamera()
                if (isMotionDetectionEnabled) {
                    startMotionDetection()
                }
            } catch (e: Exception) {
                // Handle camera start error
            }
        }
    }

    private fun stopCamera() {
        serviceScope.launch {
            try {
                cameraManager.stopCamera()
                motionDetector.stopMotionDetection()
                if (isRecording) {
                    recordingManager.stopRecording()
                    isRecording = false
                }
            } catch (e: Exception) {
                // Handle camera stop error
            } finally {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    private fun toggleRecording() {
        serviceScope.launch {
            if (isRecording) {
                recordingManager.stopRecording()
                isRecording = false
                updateNotification("Camera Active", "Recording stopped")
            } else {
                recordingManager.startRecording()
                isRecording = true
                updateNotification("Camera Active", "Recording in progress")
            }
        }
    }

    private fun toggleMotionDetection() {
        isMotionDetectionEnabled = !isMotionDetectionEnabled
        if (isMotionDetectionEnabled) {
            startMotionDetection()
        } else {
            motionDetector.stopMotionDetection()
        }
    }

    private fun startMotionDetection() {
        serviceScope.launch {
            motionDetector.startMotionDetection { motionEvent ->
                // Handle motion detected
                handleMotionDetected(motionEvent)
            }
        }
    }

    private fun handleMotionDetected(motionEvent: com.webcamapp.mobile.data.model.MotionEvent) {
        serviceScope.launch {
            // Start motion-triggered recording
            if (!isRecording) {
                recordingManager.startMotionRecording(motionEvent)
            }
            
            // Update notification
            updateNotification("Motion Detected", "Recording motion event")
            
            // Send notification to user
            sendMotionNotification(motionEvent)
        }
    }

    private fun createNotification(title: String, content: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_camera)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(title: String, content: String) {
        val notification = createNotification(title, content)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Camera service notifications"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendMotionNotification(motionEvent: com.webcamapp.mobile.data.model.MotionEvent) {
        // TODO: Implement push notification to viewer devices
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}