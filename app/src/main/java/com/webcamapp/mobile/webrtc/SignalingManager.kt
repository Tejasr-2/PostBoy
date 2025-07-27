package com.webcamapp.mobile.webrtc

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.java_websocket.framing.Framedata
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignalingManager @Inject constructor() {
    private var webSocketClient: WebSocketClient? = null
    private var reconnectJob: Job? = null
    private var heartbeatJob: Job? = null
    
    private val _connectionState = MutableStateFlow(SignalingConnectionState.DISCONNECTED)
    val connectionState: StateFlow<SignalingConnectionState> = _connectionState
    
    private val _connectedDevices = MutableStateFlow<List<String>>(emptyList())
    val connectedDevices: StateFlow<List<String>> = _connectedDevices

    private var deviceId: String? = null
    private var signalingServerUrl: String? = null
    private var onMessageCallback: ((SignalingMessage) -> Unit)? = null

    companion object {
        private const val TAG = "SignalingManager"
        private const val RECONNECT_DELAY_MS = 5000L
        private const val HEARTBEAT_INTERVAL_MS = 30000L
    }

    fun connect(serverUrl: String, deviceId: String, onMessage: (SignalingMessage) -> Unit) {
        this.signalingServerUrl = serverUrl
        this.deviceId = deviceId
        this.onMessageCallback = onMessage
        
        disconnect()
        establishConnection()
    }

    private fun establishConnection() {
        try {
            val serverUri = URI(signalingServerUrl)
            
            webSocketClient = object : WebSocketClient(serverUri) {
                override fun onOpen(handshakedata: ServerHandshake?) {
                    Log.d(TAG, "WebSocket connection opened")
                    _connectionState.value = SignalingConnectionState.CONNECTED
                    
                    // Send device registration
                    sendDeviceRegistration()
                    
                    // Start heartbeat
                    startHeartbeat()
                }

                override fun onMessage(message: String?) {
                    message?.let { handleMessage(it) }
                }

                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    Log.d(TAG, "WebSocket connection closed: $code - $reason")
                    _connectionState.value = SignalingConnectionState.DISCONNECTED
                    stopHeartbeat()
                    
                    // Attempt to reconnect
                    scheduleReconnect()
                }

                override fun onError(ex: Exception?) {
                    Log.e(TAG, "WebSocket error", ex)
                    _connectionState.value = SignalingConnectionState.ERROR
                }
            }

            webSocketClient?.connect()
        } catch (e: Exception) {
            Log.e(TAG, "Error establishing WebSocket connection", e)
            _connectionState.value = SignalingConnectionState.ERROR
            scheduleReconnect()
        }
    }

    private fun handleMessage(message: String) {
        try {
            val signalingMessage = parseSignalingMessage(message)
            onMessageCallback?.invoke(signalingMessage)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing signaling message", e)
        }
    }

    private fun parseSignalingMessage(message: String): SignalingMessage {
        // Simple JSON parsing for signaling messages
        return when {
            message.contains("\"type\":\"offer\"") -> {
                SignalingMessage.Offer(
                    fromDeviceId = extractField(message, "fromDeviceId"),
                    toDeviceId = extractField(message, "toDeviceId"),
                    sdp = extractField(message, "sdp")
                )
            }
            message.contains("\"type\":\"answer\"") -> {
                SignalingMessage.Answer(
                    fromDeviceId = extractField(message, "fromDeviceId"),
                    toDeviceId = extractField(message, "toDeviceId"),
                    sdp = extractField(message, "sdp")
                )
            }
            message.contains("\"type\":\"ice-candidate\"") -> {
                SignalingMessage.IceCandidate(
                    fromDeviceId = extractField(message, "fromDeviceId"),
                    toDeviceId = extractField(message, "toDeviceId"),
                    candidate = extractField(message, "candidate"),
                    sdpMLineIndex = extractField(message, "sdpMLineIndex").toIntOrNull() ?: 0,
                    sdpMid = extractField(message, "sdpMid")
                )
            }
            message.contains("\"type\":\"device-list\"") -> {
                val devices = extractArrayField(message, "devices")
                SignalingMessage.DeviceList(devices)
            }
            message.contains("\"type\":\"device-online\"") -> {
                SignalingMessage.DeviceOnline(
                    deviceId = extractField(message, "deviceId"),
                    deviceName = extractField(message, "deviceName")
                )
            }
            message.contains("\"type\":\"device-offline\"") -> {
                SignalingMessage.DeviceOffline(
                    deviceId = extractField(message, "deviceId")
                )
            }
            message.contains("\"type\":\"ping\"") -> {
                SignalingMessage.Ping(
                    fromDeviceId = extractField(message, "fromDeviceId")
                )
            }
            message.contains("\"type\":\"pong\"") -> {
                SignalingMessage.Pong(
                    fromDeviceId = extractField(message, "fromDeviceId")
                )
            }
            else -> {
                SignalingMessage.Unknown(message)
            }
        }
    }

    private fun extractField(json: String, fieldName: String): String {
        val pattern = "\"$fieldName\":\"([^\"]*)\""
        val regex = Regex(pattern)
        return regex.find(json)?.groupValues?.get(1) ?: ""
    }

    private fun extractArrayField(json: String, fieldName: String): List<String> {
        val pattern = "\"$fieldName\":\\[([^\\]]*)\\]"
        val regex = Regex(pattern)
        val match = regex.find(json)?.groupValues?.get(1) ?: ""
        return match.split(",").map { it.trim().removeSurrounding("\"") }.filter { it.isNotEmpty() }
    }

    private fun sendDeviceRegistration() {
        val registrationMessage = """
            {
                "type": "register",
                "deviceId": "$deviceId",
                "deviceType": "viewer"
            }
        """.trimIndent()
        
        sendMessage(registrationMessage)
    }

    fun sendOffer(toDeviceId: String, sdp: SessionDescription) {
        val offerMessage = """
            {
                "type": "offer",
                "fromDeviceId": "$deviceId",
                "toDeviceId": "$toDeviceId",
                "sdp": "${sdp.description}"
            }
        """.trimIndent()
        
        sendMessage(offerMessage)
    }

    fun sendAnswer(toDeviceId: String, sdp: SessionDescription) {
        val answerMessage = """
            {
                "type": "answer",
                "fromDeviceId": "$deviceId",
                "toDeviceId": "$toDeviceId",
                "sdp": "${sdp.description}"
            }
        """.trimIndent()
        
        sendMessage(answerMessage)
    }

    fun sendIceCandidate(toDeviceId: String, candidate: IceCandidate) {
        val iceMessage = """
            {
                "type": "ice-candidate",
                "fromDeviceId": "$deviceId",
                "toDeviceId": "$toDeviceId",
                "candidate": "${candidate.sdp}",
                "sdpMLineIndex": ${candidate.sdpMLineIndex},
                "sdpMid": "${candidate.sdpMid}"
            }
        """.trimIndent()
        
        sendMessage(iceMessage)
    }

    fun requestDeviceList() {
        val requestMessage = """
            {
                "type": "get-devices",
                "fromDeviceId": "$deviceId"
            }
        """.trimIndent()
        
        sendMessage(requestMessage)
    }

    private fun sendMessage(message: String) {
        try {
            webSocketClient?.send(message)
            Log.d(TAG, "Sent message: $message")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message", e)
        }
    }

    private fun startHeartbeat() {
        heartbeatJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive && _connectionState.value == SignalingConnectionState.CONNECTED) {
                delay(HEARTBEAT_INTERVAL_MS)
                sendPing()
            }
        }
    }

    private fun stopHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = null
    }

    private fun sendPing() {
        val pingMessage = """
            {
                "type": "ping",
                "fromDeviceId": "$deviceId"
            }
        """.trimIndent()
        
        sendMessage(pingMessage)
    }

    private fun scheduleReconnect() {
        reconnectJob?.cancel()
        reconnectJob = CoroutineScope(Dispatchers.IO).launch {
            delay(RECONNECT_DELAY_MS)
            if (_connectionState.value == SignalingConnectionState.DISCONNECTED) {
                Log.d(TAG, "Attempting to reconnect...")
                establishConnection()
            }
        }
    }

    fun disconnect() {
        reconnectJob?.cancel()
        stopHeartbeat()
        webSocketClient?.close()
        webSocketClient = null
        _connectionState.value = SignalingConnectionState.DISCONNECTED
        _connectedDevices.value = emptyList()
        Log.d(TAG, "Signaling manager disconnected")
    }

    fun isConnected(): Boolean = _connectionState.value == SignalingConnectionState.CONNECTED
}

enum class SignalingConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

sealed class SignalingMessage {
    data class Offer(
        val fromDeviceId: String,
        val toDeviceId: String,
        val sdp: String
    ) : SignalingMessage()

    data class Answer(
        val fromDeviceId: String,
        val toDeviceId: String,
        val sdp: String
    ) : SignalingMessage()

    data class IceCandidate(
        val fromDeviceId: String,
        val toDeviceId: String,
        val candidate: String,
        val sdpMLineIndex: Int,
        val sdpMid: String
    ) : SignalingMessage()

    data class DeviceList(
        val devices: List<String>
    ) : SignalingMessage()

    data class DeviceOnline(
        val deviceId: String,
        val deviceName: String
    ) : SignalingMessage()

    data class DeviceOffline(
        val deviceId: String
    ) : SignalingMessage()

    data class Ping(
        val fromDeviceId: String
    ) : SignalingMessage()

    data class Pong(
        val fromDeviceId: String
    ) : SignalingMessage()

    data class Unknown(
        val message: String
    ) : SignalingMessage()
}