package com.webcamapp.mobile.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.webcamapp.mobile.R
import com.webcamapp.mobile.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WebcamFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        private const val MOTION_CHANNEL_ID = "motion_notifications"
        private const val DEVICE_CHANNEL_ID = "device_notifications"
        private const val MOTION_NOTIFICATION_ID = 2001
        private const val DEVICE_NOTIFICATION_ID = 2002
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Handle data payload
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }

        // Handle notification payload
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            handleNotificationMessage(it)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
        
        // TODO: Send this token to your server
        sendRegistrationToServer(token)
    }

    private fun handleDataMessage(data: Map<String, String>) {
        when (data["type"]) {
            "motion_detected" -> {
                val deviceId = data["device_id"] ?: return
                val timestamp = data["timestamp"] ?: return
                val confidence = data["confidence"] ?: "0.0"
                
                showMotionNotification(deviceId, timestamp, confidence.toFloatOrNull() ?: 0f)
            }
            "device_offline" -> {
                val deviceId = data["device_id"] ?: return
                val deviceName = data["device_name"] ?: "Unknown Device"
                
                showDeviceOfflineNotification(deviceId, deviceName)
            }
            "device_online" -> {
                val deviceId = data["device_id"] ?: return
                val deviceName = data["device_name"] ?: "Unknown Device"
                
                showDeviceOnlineNotification(deviceId, deviceName)
            }
            "low_battery" -> {
                val deviceId = data["device_id"] ?: return
                val batteryLevel = data["battery_level"] ?: "0"
                
                showLowBatteryNotification(deviceId, batteryLevel.toIntOrNull() ?: 0)
            }
            "storage_full" -> {
                val deviceId = data["device_id"] ?: return
                
                showStorageFullNotification(deviceId)
            }
        }
    }

    private fun handleNotificationMessage(notification: com.google.firebase.messaging.RemoteMessage.Notification) {
        // Handle simple notification messages
        val title = notification.title ?: "WebcamApp"
        val body = notification.body ?: ""
        
        showGeneralNotification(title, body)
    }

    private fun showMotionNotification(deviceId: String, timestamp: String, confidence: Float) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("device_id", deviceId)
            putExtra("action", "view_motion")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, MOTION_CHANNEL_ID)
            .setContentTitle("Motion Detected")
            .setContentText("Motion detected on camera device")
            .setSmallIcon(R.drawable.ic_motion)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(MOTION_NOTIFICATION_ID, notification)
    }

    private fun showDeviceOfflineNotification(deviceId: String, deviceName: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("device_id", deviceId)
            putExtra("action", "device_status")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, DEVICE_CHANNEL_ID)
            .setContentTitle("Device Offline")
            .setContentText("$deviceName is offline")
            .setSmallIcon(R.drawable.ic_device_offline)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(DEVICE_NOTIFICATION_ID, notification)
    }

    private fun showDeviceOnlineNotification(deviceId: String, deviceName: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("device_id", deviceId)
            putExtra("action", "device_status")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, DEVICE_CHANNEL_ID)
            .setContentTitle("Device Online")
            .setContentText("$deviceName is back online")
            .setSmallIcon(R.drawable.ic_device_online)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(DEVICE_NOTIFICATION_ID + 1, notification)
    }

    private fun showLowBatteryNotification(deviceId: String, batteryLevel: Int) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("device_id", deviceId)
            putExtra("action", "device_status")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, DEVICE_CHANNEL_ID)
            .setContentTitle("Low Battery")
            .setContentText("Camera device battery is at $batteryLevel%")
            .setSmallIcon(R.drawable.ic_battery_low)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(DEVICE_NOTIFICATION_ID + 2, notification)
    }

    private fun showStorageFullNotification(deviceId: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("device_id", deviceId)
            putExtra("action", "storage_management")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, DEVICE_CHANNEL_ID)
            .setContentTitle("Storage Full")
            .setContentText("Camera device storage is full")
            .setSmallIcon(R.drawable.ic_storage_full)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(DEVICE_NOTIFICATION_ID + 3, notification)
    }

    private fun showGeneralNotification(title: String, body: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, DEVICE_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(DEVICE_NOTIFICATION_ID + 4, notification)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val motionChannel = NotificationChannel(
                MOTION_CHANNEL_ID,
                "Motion Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for motion detection events"
                enableVibration(true)
                enableLights(true)
            }

            val deviceChannel = NotificationChannel(
                DEVICE_CHANNEL_ID,
                "Device Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for device status changes"
                enableVibration(false)
                enableLights(false)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(motionChannel)
            notificationManager.createNotificationChannel(deviceChannel)
        }
    }

    private fun sendRegistrationToServer(token: String) {
        // TODO: Implement token registration with your server
        Log.d(TAG, "Sending FCM registration token to server: $token")
    }
}