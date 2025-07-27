package com.webcamapp.mobile.di

import com.webcamapp.mobile.camera.CameraManager
import com.webcamapp.mobile.motion.MotionDetector
import com.webcamapp.mobile.pairing.DevicePairingManager
import com.webcamapp.mobile.power.PowerManager
import com.webcamapp.mobile.qr.QRCodeManager
import com.webcamapp.mobile.recording.RecordingManager
import com.webcamapp.mobile.webrtc.SignalingManager
import com.webcamapp.mobile.webrtc.WebRTCManager
import com.webcamapp.mobile.advanced.AdvancedCameraManager
import com.webcamapp.mobile.optimization.PerformanceOptimizer
import com.webcamapp.mobile.playback.VideoPlaybackManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
<<<<<<< HEAD
import android.app.Application
import kotlinx.coroutines.runBlocking
import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.dataStore
import androidx.datastore.preferences.core.first
=======
>>>>>>> origin/main

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
<<<<<<< HEAD
    fun provideRecordingManager(
        @ApplicationContext context: Context,
        advancedCameraManager: AdvancedCameraManager,
        application: Application
    ): RecordingManager {
        val manager = RecordingManager(context, advancedCameraManager)
        // Load date format from DataStore synchronously at startup
        val dataStore = context.applicationContext.let { (it as Application).dataStore }
        val key = androidx.datastore.preferences.core.stringPreferencesKey("date_format")
        val dateFormat = runBlocking {
            dataStore.data.first()[key] ?: "yyyy-MM-dd HH:mm:ss"
        }
        manager.dateFormat = dateFormat
        return manager
    }
=======
    fun provideRecordingManager(recordingManager: RecordingManager): RecordingManager = recordingManager
>>>>>>> origin/main

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

    // Phase 5: Advanced Features & Optimization Components
    @Provides
    @Singleton
    fun provideAdvancedCameraManager(advancedCameraManager: AdvancedCameraManager): AdvancedCameraManager = advancedCameraManager

    @Provides
    @Singleton
    fun providePerformanceOptimizer(performanceOptimizer: PerformanceOptimizer): PerformanceOptimizer = performanceOptimizer

    @Provides
    @Singleton
    fun provideVideoPlaybackManager(videoPlaybackManager: VideoPlaybackManager): VideoPlaybackManager = videoPlaybackManager
}