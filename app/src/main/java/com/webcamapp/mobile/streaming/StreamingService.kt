package com.webcamapp.mobile.streaming

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.webcamapp.mobile.R
import com.webcamapp.mobile.ui.MainActivity
import com.webcamapp.mobile.webrtc.ConnectionState
import com.webcamapp.mobile.webrtc.SignalingManager
import com.webcamapp.mobile.webrtc.WebRTCManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import javax.inject.Inject

@AndroidEntryPoint
class StreamingService : Service() {

    @Inject
    lateinit var webRTCManager: WebRTCManager

    @Inject
    lateinit var signalingManager: SignalingManager

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var currentStreamingDeviceId: String? = null
    private var signalingServerUrl: String? = null
    private var deviceId: String? = null

    companion object {
        private const val TAG = "StreamingService"
        private const val NOTIFICATION_ID = 3001
        private const val CHANNEL_ID = "streaming_service_channel"
        private const val CHANNEL_NAME = "Streaming Service"

        const val ACTION_START_STREAMING = "com.webcamapp.mobile.START_STREAMING"
        const val ACTION_STOP_STREAMING = "com.webcamapp.mobile.STOP_STREAMING"
        const val ACTION_CONNECT_TO_DEVICE = "com.webcamapp.mobile.CONNECT_TO_DEVICE"
        const val ACTION_DISCONNECT_FROM_DEVICE = "com.webcamapp.mobile.DISCONNECT_FROM_DEVICE"

        const val EXTRA_DEVICE_ID = "device_id"
        const val EXTRA_SIGNALING_SERVER = "signaling_server"
        const val EXTRA_DEVICE_ID_PARAM = "device_id_param"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        setupSignalingManager()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_STREAMING -> {
                val deviceId = intent.getStringExtra(EXTRA_DEVICE_ID)
                val signalingServer = intent.getStringExtra(EXTRA_SIGNALING_SERVER)
                val deviceIdParam = intent.getStringExtra(EXTRA_DEVICE_ID_PARAM)
                startStreaming(deviceId, signalingServer, deviceIdParam)
            }
            ACTION_STOP_STREAMING -> stopStreaming()
            ACTION_CONNECT_TO_DEVICE -> {
                val deviceId = intent.getStringExtra(EXTRA_DEVICE_ID)
                connectToDevice(deviceId)
            }
            ACTION_DISCONNECT_FROM_DEVICE -> {
                val deviceId = intent.getStringExtra(EXTRA_DEVICE_ID)
                disconnectFromDevice(deviceId)
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun setupSignalingManager() {
        webRTCManager.setSignalingManager(signalingManager)
        
        // Set up signaling message handler
        signalingManager.connect(
            serverUrl = signalingServerUrl ?: "ws://localhost:8080",
            deviceId = deviceId ?: "viewer_device",
            onMessage = { message ->
                handleSignalingMessage(message)
            }
        )
    }

    private fun startStreaming(deviceId: String?, signalingServer: String?, deviceIdParam: String?) {
        if (deviceId == null || signalingServer == null || deviceIdParam == null) {
            Log.e(TAG, "Missing required parameters for streaming")
            return
        }

        this.currentStreamingDeviceId = deviceId
        this.signalingServerUrl = signalingServer
        this.deviceId = deviceIdParam

        val notification = createNotification("Streaming Active", "Connected to camera device")
        startForeground(NOTIFICATION_ID, notification)

        serviceScope.launch {
            try {
                // Connect to signaling server
                signalingManager.connect(signalingServer, deviceIdParam) { message ->
                    handleSignalingMessage(message)
                }

                // Create peer connection
                val peerConnection = webRTCManager.createPeerConnection(deviceId, true)
                if (peerConnection != null) {
                    // Create and send offer
                    webRTCManager.createOffer(deviceId,
                        onSuccess = { sdp ->
                            signalingManager.sendOffer(deviceId, sdp)
                        },
                        onError = { error ->
                            Log.e(TAG, "Error creating offer: $error")
                        }
                    )
                } else {
                    Log.e(TAG, "Failed to create peer connection")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error starting streaming", e)
            }
        }
    }

    private fun stopStreaming() {
        serviceScope.launch {
            try {
                currentStreamingDeviceId?.let { deviceId ->
                    webRTCManager.disconnect(deviceId)
                }
                signalingManager.disconnect()
                currentStreamingDeviceId = null
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping streaming", e)
            } finally {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    private fun connectToDevice(deviceId: String?) {
        if (deviceId == null) return

        serviceScope.launch {
            try {
                val peerConnection = webRTCManager.createPeerConnection(deviceId, true)
                if (peerConnection != null) {
                    // Create and send offer
                    webRTCManager.createOffer(deviceId,
                        onSuccess = { sdp ->
                            signalingManager.sendOffer(deviceId, sdp)
                        },
                        onError = { error ->
                            Log.e(TAG, "Error creating offer: $error")
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error connecting to device", e)
            }
        }
    }

    private fun disconnectFromDevice(deviceId: String?) {
        if (deviceId == null) return

        serviceScope.launch {
            try {
                webRTCManager.disconnect(deviceId)
            } catch (e: Exception) {
                Log.e(TAG, "Error disconnecting from device", e)
            }
        }
    }

    private fun handleSignalingMessage(message: com.webcamapp.mobile.webrtc.SignalingMessage) {
        serviceScope.launch {
            try {
                when (message) {
                    is com.webcamapp.mobile.webrtc.SignalingMessage.Offer -> {
                        handleOffer(message)
                    }
                    is com.webcamapp.mobile.webrtc.SignalingMessage.Answer -> {
                        handleAnswer(message)
                    }
                    is com.webcamapp.mobile.webrtc.SignalingMessage.IceCandidate -> {
                        handleIceCandidate(message)
                    }
                    is com.webcamapp.mobile.webrtc.SignalingMessage.DeviceList -> {
                        handleDeviceList(message)
                    }
                    is com.webcamapp.mobile.webrtc.SignalingMessage.DeviceOnline -> {
                        handleDeviceOnline(message)
                    }
                    is com.webcamapp.mobile.webrtc.SignalingMessage.DeviceOffline -> {
                        handleDeviceOffline(message)
                    }
                    is com.webcamapp.mobile.webrtc.SignalingMessage.Ping -> {
                        handlePing(message)
                    }
                    is com.webcamapp.mobile.webrtc.SignalingMessage.Pong -> {
                        handlePong(message)
                    }
                    is com.webcamapp.mobile.webrtc.SignalingMessage.Unknown -> {
                        Log.w(TAG, "Unknown signaling message: ${message.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling signaling message", e)
            }
        }
    }

    private suspend fun handleOffer(offer: com.webcamapp.mobile.webrtc.SignalingMessage.Offer) {
        try {
            // Create peer connection if it doesn't exist
            val peerConnection = webRTCManager.createPeerConnection(offer.fromDeviceId, false)
            if (peerConnection != null) {
                // Set remote description
                val sdp = SessionDescription(SessionDescription.Type.OFFER, offer.sdp)
                webRTCManager.setRemoteDescription(offer.fromDeviceId, sdp,
                    onSuccess = {
                        // Create and send answer
                        webRTCManager.createAnswer(offer.fromDeviceId,
                            onSuccess = { answerSdp ->
                                signalingManager.sendAnswer(offer.fromDeviceId, answerSdp)
                            },
                            onError = { error ->
                                Log.e(TAG, "Error creating answer: $error")
                            }
                        )
                    },
                    onError = { error ->
                        Log.e(TAG, "Error setting remote description: $error")
                    }
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling offer", e)
        }
    }

    private suspend fun handleAnswer(answer: com.webcamapp.mobile.webrtc.SignalingMessage.Answer) {
        try {
            val sdp = SessionDescription(SessionDescription.Type.ANSWER, answer.sdp)
            webRTCManager.setRemoteDescription(answer.fromDeviceId, sdp,
                onSuccess = {
                    Log.d(TAG, "Remote description set successfully")
                },
                onError = { error ->
                    Log.e(TAG, "Error setting remote description: $error")
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error handling answer", e)
        }
    }

    private suspend fun handleIceCandidate(iceCandidate: com.webcamapp.mobile.webrtc.SignalingMessage.IceCandidate) {
        try {
            val candidate = IceCandidate(
                iceCandidate.sdpMid,
                iceCandidate.sdpMLineIndex,
                iceCandidate.candidate
            )
            webRTCManager.addIceCandidate(iceCandidate.fromDeviceId, candidate)
        } catch (e: Exception) {
            Log.e(TAG, "Error handling ICE candidate", e)
        }
    }

    private suspend fun handleDeviceList(deviceList: com.webcamapp.mobile.webrtc.SignalingMessage.DeviceList) {
        Log.d(TAG, "Received device list: ${deviceList.devices}")
        // Update UI with available devices
    }

    private suspend fun handleDeviceOnline(deviceOnline: com.webcamapp.mobile.webrtc.SignalingMessage.DeviceOnline) {
        Log.d(TAG, "Device online: ${deviceOnline.deviceName}")
        updateNotification("Device Online", "${deviceOnline.deviceName} is online")
    }

    private suspend fun handleDeviceOffline(deviceOffline: com.webcamapp.mobile.webrtc.SignalingMessage.DeviceOffline) {
        Log.d(TAG, "Device offline: ${deviceOffline.deviceId}")
        updateNotification("Device Offline", "Camera device is offline")
    }

    private suspend fun handlePing(ping: com.webcamapp.mobile.webrtc.SignalingMessage.Ping) {
        // Send pong response
        val pongMessage = """
            {
                "type": "pong",
                "fromDeviceId": "${deviceId}"
            }
        """.trimIndent()
        
        // This would be sent through the signaling manager
        Log.d(TAG, "Received ping from: ${ping.fromDeviceId}")
    }

    private suspend fun handlePong(pong: com.webcamapp.mobile.webrtc.SignalingMessage.Pong) {
        Log.d(TAG, "Received pong from: ${pong.fromDeviceId}")
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
            .setSmallIcon(R.drawable.ic_streaming)
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
                description = "Streaming service notifications"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        webRTCManager.release()
    }
}