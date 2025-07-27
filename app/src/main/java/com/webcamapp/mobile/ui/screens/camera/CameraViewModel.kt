package com.webcamapp.mobile.ui.screens.camera

import androidx.lifecycle.ViewModel
import com.webcamapp.mobile.camera.CameraManager
import com.webcamapp.mobile.power.PowerManager
import com.webcamapp.mobile.recording.RecordingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    val cameraManager: CameraManager,
    val recordingManager: RecordingManager,
    val powerManager: PowerManager
) : ViewModel() {
    // ViewModel logic will be added as needed
}