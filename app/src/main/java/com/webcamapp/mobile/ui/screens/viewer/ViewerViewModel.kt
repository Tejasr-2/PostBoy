package com.webcamapp.mobile.ui.screens.viewer

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.webcamapp.mobile.data.model.Device
import com.webcamapp.mobile.pairing.DevicePairingManager
import com.webcamapp.mobile.pairing.PairingResult
import com.webcamapp.mobile.pairing.PairingState
import com.webcamapp.mobile.streaming.StreamingService
import com.webcamapp.mobile.webrtc.ConnectionState
import com.webcamapp.mobile.webrtc.WebRTCManager
import com.webcamapp.mobile.recording.RecordingManager
import com.webcamapp.mobile.data.model.Recording
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val devicePairingManager: DevicePairingManager,
    private val webRTCManager: WebRTCManager,
    private val recordingManager: RecordingManager
) : ViewModel() {

    // State flows from managers
    val pairedDevices: StateFlow<List<Device>> = devicePairingManager.pairedDevices
    val pairingState: StateFlow<PairingState> = devicePairingManager.pairingState
    val connectionState: StateFlow<ConnectionState> = webRTCManager.connectionState
    val isStreaming: StateFlow<Boolean> = webRTCManager.isStreaming
    val currentDevice: StateFlow<Device?> = devicePairingManager.currentPairingDevice

    // Local state
    private val _signalingServerUrl = MutableStateFlow("ws://localhost:8080")
    val signalingServerUrl: StateFlow<String> = _signalingServerUrl

    private val _viewerDeviceId = MutableStateFlow("viewer_${System.currentTimeMillis()}")
    val viewerDeviceId: StateFlow<String> = _viewerDeviceId

    init {
        // Refresh paired devices on initialization
        viewModelScope.launch {
            devicePairingManager.refreshPairedDevices()
        }
    }

    fun pairWithDevice(qrData: String) {
        viewModelScope.launch {
            val result = devicePairingManager.pairWithDevice(qrData)
            when (result) {
                is PairingResult.Success -> {
                    // Device paired successfully
                    // Optionally start streaming immediately
                    // startStreaming(result.device)
                }
                is PairingResult.Failed -> {
                    // Handle pairing failure
                    // Could show error message to user
                }
            }
        }
    }

    fun connectToDevice(device: Device) {
        viewModelScope.launch {
            try {
                // Start streaming service
                val intent = Intent(context, StreamingService::class.java).apply {
                    action = StreamingService.ACTION_START_STREAMING
                    putExtra(StreamingService.EXTRA_DEVICE_ID, device.id)
                    putExtra(StreamingService.EXTRA_SIGNALING_SERVER, _signalingServerUrl.value)
                    putExtra(StreamingService.EXTRA_DEVICE_ID_PARAM, _viewerDeviceId.value)
                }
                context.startService(intent)

                // Create WebRTC peer connection
                val peerConnection = webRTCManager.createPeerConnection(device.id, true)
                if (peerConnection != null) {
                    // Create and send offer
                    webRTCManager.createOffer(device.id,
                        onSuccess = { sdp ->
                            // This would be sent through the signaling manager
                            // For now, we'll handle it in the streaming service
                        },
                        onError = { error ->
                            // Handle offer creation error
                        }
                    )
                }
            } catch (e: Exception) {
                // Handle connection error
            }
        }
    }

    fun disconnectFromDevice(deviceId: String) {
        viewModelScope.launch {
            try {
                // Stop streaming service
                val intent = Intent(context, StreamingService::class.java).apply {
                    action = StreamingService.ACTION_STOP_STREAMING
                }
                context.stopService(intent)

                // Disconnect WebRTC
                webRTCManager.disconnect(deviceId)
            } catch (e: Exception) {
                // Handle disconnection error
            }
        }
    }

    fun unpairDevice(deviceId: String) {
        viewModelScope.launch {
            try {
                // First disconnect if connected
                if (webRTCManager.currentDeviceId.value == deviceId) {
                    disconnectFromDevice(deviceId)
                }

                // Then unpair
                devicePairingManager.unpairDevice(deviceId)
            } catch (e: Exception) {
                // Handle unpairing error
            }
        }
    }

    fun resetPairingState() {
        devicePairingManager.resetPairingState()
    }

    fun refreshPairedDevices() {
        viewModelScope.launch {
            devicePairingManager.refreshPairedDevices()
        }
    }

    fun updateSignalingServer(url: String) {
        _signalingServerUrl.value = url
    }

    fun updateViewerDeviceId(id: String) {
        _viewerDeviceId.value = id
    }

    fun getDeviceById(deviceId: String): Device? {
        return pairedDevices.value.find { it.id == deviceId }
    }

    fun updateDeviceStatus(deviceId: String, isOnline: Boolean, batteryLevel: Int, storageRemaining: Long) {
        viewModelScope.launch {
            devicePairingManager.updateDeviceStatus(deviceId, isOnline, batteryLevel, storageRemaining)
        }
    }

    fun startStreaming(device: Device) {
        connectToDevice(device)
    }

    fun stopStreaming() {
        viewModelScope.launch {
            try {
                val intent = Intent(context, StreamingService::class.java).apply {
                    action = StreamingService.ACTION_STOP_STREAMING
                }
                context.stopService(intent)
                webRTCManager.disconnectAll()
            } catch (e: Exception) {
                // Handle stop streaming error
            }
        }
    }

    fun getConnectionStatusText(): String {
        return when (connectionState.value) {
            ConnectionState.CONNECTED -> "Connected"
            ConnectionState.CONNECTING -> "Connecting..."
            ConnectionState.FAILED -> "Connection Failed"
            ConnectionState.DISCONNECTED -> "Disconnected"
        }
    }

    fun getStreamingStatusText(): String {
        return if (isStreaming.value) "Live streaming active" else "Not streaming"
    }

    fun isDeviceConnected(deviceId: String): Boolean {
        return currentDevice.value?.id == deviceId && isStreaming.value
    }

    fun getCurrentStreamingDevice(): Device? {
        return if (isStreaming.value) currentDevice.value else null
    }

    fun getRecordingsGroupedByDay(device: Device): Map<String, List<Recording>> {
        return recordingManager.getRecordingsForDeviceGroupedByDay(device.id)
    }

    fun setLiveStreamQuality(quality: String) {
        // Switch the live stream quality (HD/SD) via WebRTCManager or signaling
        webRTCManager.setStreamQuality(quality)
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up resources
        viewModelScope.launch {
            webRTCManager.release()
        }
    }
}