package com.webcamapp.mobile.webrtc

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.webrtc.*
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebRTCManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private val peerConnections = ConcurrentHashMap<String, PeerConnection>()
    private val localVideoTrack = MutableStateFlow<VideoTrack?>(null)
    private val remoteVideoTrack = MutableStateFlow<VideoTrack?>(null)
    
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState
    
    private val _isStreaming = MutableStateFlow(false)
    val isStreaming: StateFlow<Boolean> = _isStreaming
    
    private val _currentDeviceId = MutableStateFlow<String?>(null)
    val currentDeviceId: StateFlow<String?> = _currentDeviceId

    companion object {
        private const val TAG = "WebRTCManager"
        private val ICE_SERVERS = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer()
        )
    }

    init {
        initializePeerConnectionFactory()
    }

    private fun initializePeerConnectionFactory() {
        try {
            // Initialize WebRTC
            PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(context)
                    .setEnableInternalTracer(true)
                    .createInitializationOptions()
            )

            // Create EGL context
            val eglBaseContext = EglBase.create().eglBaseContext

            // Create video encoder/decoder factories
            val videoEncoderFactory = DefaultVideoEncoderFactory(
                eglBaseContext,
                true,
                true
            )
            val videoDecoderFactory = DefaultVideoDecoderFactory(eglBaseContext)

            // Create audio device module
            val audioDeviceModule = JavaAudioDeviceModule.builder(context).createAudioDeviceModule()

            // Create peer connection factory
            peerConnectionFactory = PeerConnectionFactory.builder()
                .setVideoEncoderFactory(videoEncoderFactory)
                .setVideoDecoderFactory(videoDecoderFactory)
                .setAudioDeviceModule(audioDeviceModule)
                .createPeerConnectionFactory()

            Log.d(TAG, "PeerConnectionFactory initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing PeerConnectionFactory", e)
        }
    }

    fun createLocalVideoTrack(source: VideoSource): VideoTrack {
        return peerConnectionFactory?.createVideoTrack("local_video", source)
            ?: throw IllegalStateException("PeerConnectionFactory not initialized")
    }

    fun createLocalAudioTrack(): AudioTrack {
        val audioSource = peerConnectionFactory?.createAudioSource(MediaConstraints())
        return peerConnectionFactory?.createAudioTrack("local_audio", audioSource)
            ?: throw IllegalStateException("PeerConnectionFactory not initialized")
    }

    fun createPeerConnection(deviceId: String, isInitiator: Boolean): PeerConnection? {
        try {
            val rtcConfig = PeerConnection.RTCConfiguration(ICE_SERVERS).apply {
                sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
                enableDtlsSrtp = true
                enableRtpDataChannel = true
            }

            val peerConnectionObserver = object : PeerConnection.Observer {
                override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) {
                    Log.d(TAG, "ICE connection state changed: $state")
                    when (state) {
                        PeerConnection.IceConnectionState.CONNECTED -> {
                            _connectionState.value = ConnectionState.CONNECTED
                            _isStreaming.value = true
                        }
                        PeerConnection.IceConnectionState.DISCONNECTED -> {
                            _connectionState.value = ConnectionState.DISCONNECTED
                            _isStreaming.value = false
                        }
                        PeerConnection.IceConnectionState.FAILED -> {
                            _connectionState.value = ConnectionState.FAILED
                            _isStreaming.value = false
                        }
                        else -> {}
                    }
                }

                override fun onIceCandidate(candidate: IceCandidate) {
                    Log.d(TAG, "New ICE candidate: ${candidate.sdp}")
                    // Send candidate to remote peer via signaling server
                    signalingManager?.sendIceCandidate(deviceId, candidate)
                }

                override fun onAddStream(stream: MediaStream) {
                    Log.d(TAG, "Remote stream added")
                    stream.videoTracks.firstOrNull()?.let { track ->
                        remoteVideoTrack.value = track
                    }
                }

                override fun onRemoveStream(stream: MediaStream) {
                    Log.d(TAG, "Remote stream removed")
                    remoteVideoTrack.value = null
                }

                override fun onDataChannel(channel: DataChannel) {
                    Log.d(TAG, "Data channel created")
                }

                override fun onRenegotiationNeeded() {
                    Log.d(TAG, "Renegotiation needed")
                }

                override fun onSignalingChange(state: PeerConnection.SignalingState) {
                    Log.d(TAG, "Signaling state changed: $state")
                }

                override fun onIceConnectionReceivingChange(receiving: Boolean) {
                    Log.d(TAG, "ICE connection receiving changed: $receiving")
                }

                override fun onIceGatheringChange(state: PeerConnection.IceGatheringState) {
                    Log.d(TAG, "ICE gathering state changed: $state")
                }

                override fun onAddTrack(receiver: RtpReceiver, streams: Array<out MediaStream>) {
                    Log.d(TAG, "Track added")
                }

                override fun onTrack(transceiver: RtpTransceiver) {
                    Log.d(TAG, "Track received")
                    if (transceiver.mediaType == MediaStreamTrack.MediaType.VIDEO) {
                        transceiver.receiver.track()?.let { track ->
                            if (track is VideoTrack) {
                                remoteVideoTrack.value = track
                            }
                        }
                    }
                }

                override fun onRemoveTrack(receiver: RtpReceiver) {
                    Log.d(TAG, "Track removed")
                }

                override fun onSelectedCandidatePairChanged(event: CandidatePairChangeEvent) {
                    Log.d(TAG, "Selected candidate pair changed")
                }
            }

            val peerConnection = peerConnectionFactory?.createPeerConnection(
                rtcConfig,
                peerConnectionObserver
            )

            peerConnection?.let {
                peerConnections[deviceId] = it
                _currentDeviceId.value = deviceId
                _connectionState.value = ConnectionState.CONNECTING
                Log.d(TAG, "Peer connection created for device: $deviceId")
            }

            return peerConnection
        } catch (e: Exception) {
            Log.e(TAG, "Error creating peer connection", e)
            return null
        }
    }

    fun addLocalVideoTrack(deviceId: String, videoTrack: VideoTrack) {
        val peerConnection = peerConnections[deviceId]
        if (peerConnection != null) {
            val stream = peerConnectionFactory?.createLocalMediaStream("local_stream")
            stream?.addTrack(videoTrack)
            peerConnection.addStream(stream)
            localVideoTrack.value = videoTrack
            Log.d(TAG, "Local video track added to peer connection")
        }
    }

    fun addLocalAudioTrack(deviceId: String, audioTrack: AudioTrack) {
        val peerConnection = peerConnections[deviceId]
        if (peerConnection != null) {
            val stream = peerConnectionFactory?.createLocalMediaStream("local_stream")
            stream?.addTrack(audioTrack)
            peerConnection.addStream(stream)
            Log.d(TAG, "Local audio track added to peer connection")
        }
    }

    fun createOffer(deviceId: String, onSuccess: (SessionDescription) -> Unit, onError: (String) -> Unit) {
        val peerConnection = peerConnections[deviceId]
        if (peerConnection != null) {
            val constraints = MediaConstraints().apply {
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            }

            peerConnection.createOffer(object : SdpObserver {
                override fun onCreateSuccess(sdp: SessionDescription) {
                    peerConnection.setLocalDescription(object : SdpObserver {
                        override fun onSetSuccess() {
                            onSuccess(sdp)
                        }

                        override fun onSetFailure(error: String) {
                            onError("Failed to set local description: $error")
                        }

                        override fun onCreateSuccess(sdp: SessionDescription) {}
                        override fun onCreateFailure(error: String) {}
                    }, sdp)
                }

                override fun onSetSuccess() {}
                override fun onSetFailure(error: String) {
                    onError("Failed to create offer: $error")
                }

                override fun onCreateFailure(error: String) {
                    onError("Failed to create offer: $error")
                }
            }, constraints)
        } else {
            onError("Peer connection not found for device: $deviceId")
        }
    }

    fun createAnswer(deviceId: String, onSuccess: (SessionDescription) -> Unit, onError: (String) -> Unit) {
        val peerConnection = peerConnections[deviceId]
        if (peerConnection != null) {
            val constraints = MediaConstraints().apply {
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            }

            peerConnection.createAnswer(object : SdpObserver {
                override fun onCreateSuccess(sdp: SessionDescription) {
                    peerConnection.setLocalDescription(object : SdpObserver {
                        override fun onSetSuccess() {
                            onSuccess(sdp)
                        }

                        override fun onSetFailure(error: String) {
                            onError("Failed to set local description: $error")
                        }

                        override fun onCreateSuccess(sdp: SessionDescription) {}
                        override fun onCreateFailure(error: String) {}
                    }, sdp)
                }

                override fun onSetSuccess() {}
                override fun onSetFailure(error: String) {
                    onError("Failed to create answer: $error")
                }

                override fun onCreateFailure(error: String) {
                    onError("Failed to create answer: $error")
                }
            }, constraints)
        } else {
            onError("Peer connection not found for device: $deviceId")
        }
    }

    fun setRemoteDescription(deviceId: String, sdp: SessionDescription, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val peerConnection = peerConnections[deviceId]
        if (peerConnection != null) {
            peerConnection.setRemoteDescription(object : SdpObserver {
                override fun onSetSuccess() {
                    onSuccess()
                }

                override fun onSetFailure(error: String) {
                    onError("Failed to set remote description: $error")
                }

                override fun onCreateSuccess(sdp: SessionDescription) {}
                override fun onCreateFailure(error: String) {}
            }, sdp)
        } else {
            onError("Peer connection not found for device: $deviceId")
        }
    }

    fun addIceCandidate(deviceId: String, candidate: IceCandidate) {
        val peerConnection = peerConnections[deviceId]
        if (peerConnection != null) {
            peerConnection.addIceCandidate(candidate)
            Log.d(TAG, "ICE candidate added for device: $deviceId")
        } else {
            Log.w(TAG, "Peer connection not found for device: $deviceId")
        }
    }

    fun disconnect(deviceId: String) {
        val peerConnection = peerConnections.remove(deviceId)
        peerConnection?.close()
        
        if (_currentDeviceId.value == deviceId) {
            _currentDeviceId.value = null
            _connectionState.value = ConnectionState.DISCONNECTED
            _isStreaming.value = false
        }
        
        Log.d(TAG, "Disconnected from device: $deviceId")
    }

    fun disconnectAll() {
        peerConnections.values.forEach { it.close() }
        peerConnections.clear()
        _currentDeviceId.value = null
        _connectionState.value = ConnectionState.DISCONNECTED
        _isStreaming.value = false
        Log.d(TAG, "Disconnected from all devices")
    }

    fun getLocalVideoTrack(): VideoTrack? = localVideoTrack.value

    fun getRemoteVideoTrack(): VideoTrack? = remoteVideoTrack.value

    fun release() {
        disconnectAll()
        peerConnectionFactory?.dispose()
        peerConnectionFactory = null
        Log.d(TAG, "WebRTCManager released")
    }

<<<<<<< HEAD
    fun setStreamQuality(quality: String) {
        // TODO: Implement actual stream quality switching (e.g., change video track resolution or signal camera device)
        // For now, just log the change
        Log.d("WebRTCManager", "Switching stream quality to $quality")
    }

=======
>>>>>>> origin/main
    // Reference to signaling manager (will be injected)
    private var signalingManager: SignalingManager? = null

    fun setSignalingManager(signalingManager: SignalingManager) {
        this.signalingManager = signalingManager
    }
}

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    FAILED
}