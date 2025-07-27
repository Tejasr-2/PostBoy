package com.webcamapp.mobile.di

import com.webcamapp.mobile.camera.CameraManager
import com.webcamapp.mobile.motion.MotionDetector
import com.webcamapp.mobile.pairing.DevicePairingManager
import com.webcamapp.mobile.power.PowerManager
import com.webcamapp.mobile.qr.QRCodeManager
import com.webcamapp.mobile.recording.RecordingManager
import com.webcamapp.mobile.webrtc.SignalingManager
import com.webcamapp.mobile.webrtc.WebRTCManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @Provides
    @Singleton
    fun provideCameraManager(cameraManager: CameraManager): CameraManager = cameraManager

    @Provides
    @Singleton
    fun provideMotionDetector(motionDetector: MotionDetector): MotionDetector = motionDetector

    @Provides
    @Singleton
    fun provideRecordingManager(recordingManager: RecordingManager): RecordingManager = recordingManager

    @Provides
    @Singleton
    fun providePowerManager(powerManager: PowerManager): PowerManager = powerManager

    // Phase 3: WebRTC and Streaming Components
    @Provides
    @Singleton
    fun provideWebRTCManager(webRTCManager: WebRTCManager): WebRTCManager = webRTCManager

    @Provides
    @Singleton
    fun provideSignalingManager(signalingManager: SignalingManager): SignalingManager = signalingManager

    @Provides
    @Singleton
    fun provideQRCodeManager(qrCodeManager: QRCodeManager): QRCodeManager = qrCodeManager

    @Provides
    @Singleton
    fun provideDevicePairingManager(devicePairingManager: DevicePairingManager): DevicePairingManager = devicePairingManager
}