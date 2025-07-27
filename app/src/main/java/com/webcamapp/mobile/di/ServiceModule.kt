package com.webcamapp.mobile.di

import com.webcamapp.mobile.camera.CameraManager
import com.webcamapp.mobile.motion.MotionDetector
import com.webcamapp.mobile.power.PowerManager
import com.webcamapp.mobile.recording.RecordingManager
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
}