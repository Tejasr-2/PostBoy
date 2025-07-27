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
        private const val MOTION_NOTIFICATION_ID = 2000
        private const val CHANNEL_ID = "camera_service_channel"
        private const val MOTION_CHANNEL_ID = "motion_notification_channel"
        private const val CHANNEL_NAME = "Camera Service"
        private const val MOTION_CHANNEL_NAME = "Motion Notifications"

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
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Camera service channel
            val cameraChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Camera service notifications"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(cameraChannel)
            
            // Motion notification channel
            val motionChannel = NotificationChannel(
                MOTION_CHANNEL_ID,
                MOTION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Motion detection notifications"
                setShowBadge(true)
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(motionChannel)
        }
    }

    private fun sendMotionNotification(motionEvent: com.webcamapp.mobile.data.model.MotionEvent) {
        try {
            // Create motion notification for viewer devices
            val motionNotification = createMotionNotification(motionEvent)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Send notification with unique ID for motion events
            val motionNotificationId = MOTION_NOTIFICATION_ID + motionEvent.id.hashCode()
            notificationManager.notify(motionNotificationId, motionNotification)
            
            // Log motion event for analytics
            Log.d(TAG, "Motion notification sent: ${motionEvent.id}")
            
            // In a real implementation, this would also send to paired viewer devices
            // via Firebase Cloud Messaging or WebRTC signaling
            sendMotionToViewerDevices(motionEvent)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error sending motion notification", e)
        }
    }

    private fun createMotionNotification(motionEvent: com.webcamapp.mobile.data.model.MotionEvent): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("motion_event_id", motionEvent.id)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, motionEvent.id.hashCode(), intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, MOTION_CHANNEL_ID)
            .setContentTitle("Motion Detected")
            .setContentText("Motion detected at ${formatTimestamp(motionEvent.timestamp)}")
            .setSmallIcon(R.drawable.ic_motion)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()
    }

    private fun sendMotionToViewerDevices(motionEvent: com.webcamapp.mobile.data.model.MotionEvent) {
        // This would integrate with the WebRTC signaling or Firebase messaging
        // to notify paired viewer devices about the motion event
        serviceScope.launch {
            try {
                // For now, we'll log the intent to send to viewer devices
                Log.d(TAG, "Would send motion event to viewer devices: ${motionEvent.id}")
                
                // In a full implementation, this would:
                // 1. Get list of paired viewer devices
                // 2. Send motion event via WebRTC signaling or FCM
                // 3. Include motion event details and thumbnail
                // 4. Handle delivery confirmations
                
            } catch (e: Exception) {
                Log.e(TAG, "Error sending motion to viewer devices", e)
            }
        }
    }

    private fun formatTimestamp(timestamp: Long): String {
        val dateFormat = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        return dateFormat.format(java.util.Date(timestamp))
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}