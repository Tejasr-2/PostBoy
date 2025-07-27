package com.webcamapp.mobile.camera

import android.content.Context
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.video.VideoCapture
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.webcamapp.mobile.data.model.CameraSelection
import com.webcamapp.mobile.data.model.VideoResolution
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CameraManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var audioEnabled = true

    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private val _cameraState = MutableStateFlow(CameraState.IDLE)
    val cameraState: StateFlow<CameraState> = _cameraState

    private val _currentCameraSelection = MutableStateFlow(CameraSelection.REAR)
    val currentCameraSelection: StateFlow<CameraSelection> = _currentCameraSelection

    private val _currentResolution = MutableStateFlow(VideoResolution.HD_720P)
    val currentResolution: StateFlow<VideoResolution> = _currentResolution

    private val _isFlashEnabled = MutableStateFlow(false)
    val isFlashEnabled: StateFlow<Boolean> = _isFlashEnabled

    companion object {
        private const val TAG = "CameraManager"
    }

    suspend fun startCamera() {
        try {
            _cameraState.value = CameraState.STARTING
            
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProvider = cameraProviderFuture.get()

            val cameraProvider = cameraProvider ?: return

            // Unbind all use cases before rebinding
            cameraProvider.unbindAll()

            // Set up the camera use cases
            setupCameraUseCases(cameraProvider)

            _cameraState.value = CameraState.READY
            Log.d(TAG, "Camera started successfully")
        } catch (e: Exception) {
            _cameraState.value = CameraState.ERROR
            Log.e(TAG, "Error starting camera", e)
            throw e
        }
    }

    private fun setupCameraUseCases(cameraProvider: ProcessCameraProvider) {
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(
                when (_currentCameraSelection.value) {
                    CameraSelection.FRONT -> CameraSelector.LENS_FACING_FRONT
                    CameraSelection.REAR -> CameraSelector.LENS_FACING_BACK
                }
            )
            .build()

        // Preview use case
        preview = Preview.Builder()
            .setTargetResolution(_currentResolution.value.toSize())
            .build()

        // Image capture use case
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetResolution(_currentResolution.value.toSize())
            .build()

        // Video capture use case
        val recorder = Recorder.Builder()
            .setQualitySelector(
                QualitySelector.from(
                    when (_currentResolution.value) {
                        VideoResolution.SD_360P -> Quality.SD
                        VideoResolution.SD_480P -> Quality.SD
                        VideoResolution.HD_720P -> Quality.HD
                        VideoResolution.HD_1080P -> Quality.FHD
                    }
                )
            )
            .build()
        videoCapture = VideoCapture.withOutput(recorder)

        try {
            camera = cameraProvider.bindToLifecycle(
                context as LifecycleOwner,
                cameraSelector,
                preview,
                imageCapture,
                videoCapture
            )

            // Set up camera controls
            setupCameraControls()
        } catch (e: Exception) {
            Log.e(TAG, "Use case binding failed", e)
            throw e
        }
    }

    private fun setupCameraControls() {
        camera?.let { camera ->
            // Set up flash control
            camera.cameraControl.enableTorch(_isFlashEnabled.value)
        }
    }

    suspend fun stopCamera() {
        try {
            _cameraState.value = CameraState.STOPPING
            
            cameraProvider?.unbindAll()
            cameraProvider = null
            camera = null
            preview = null
            imageCapture = null
            videoCapture = null

            _cameraState.value = CameraState.IDLE
            Log.d(TAG, "Camera stopped successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping camera", e)
            throw e
        }
    }

    suspend fun switchCamera() {
        val newSelection = when (_currentCameraSelection.value) {
            CameraSelection.FRONT -> CameraSelection.REAR
            CameraSelection.REAR -> CameraSelection.FRONT
        }
        _currentCameraSelection.value = newSelection
        
        // Restart camera with new selection
        stopCamera()
        startCamera()
    }

    suspend fun setResolution(resolution: VideoResolution) {
        _currentResolution.value = resolution
        // Restart camera with new resolution
        stopCamera()
        startCamera()
    }

    suspend fun toggleFlash() {
        _isFlashEnabled.value = !_isFlashEnabled.value
        camera?.cameraControl?.enableTorch(_isFlashEnabled.value)
    }

    suspend fun takePhoto(outputFile: File): Result<Unit> {
        return try {
            val imageCapture = imageCapture ?: throw IllegalStateException("Image capture not initialized")
            
            val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()
            
            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        Log.d(TAG, "Photo saved successfully")
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.e(TAG, "Error taking photo", exception)
                    }
                }
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error taking photo", e)
            Result.failure(e)
        }
    }

    suspend fun startVideoRecording(outputFile: File): Result<Unit> {
        return try {
            val videoCapture = videoCapture ?: throw IllegalStateException("Video capture not initialized")
            
            val outputOptions = VideoCapture.OutputFileOptions.Builder(outputFile).build()
            
            videoCapture.startRecording(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                object : VideoCapture.OnVideoSavedCallback {
                    override fun onVideoSaved(outputFileResults: VideoCapture.OutputFileResults) {
                        Log.d(TAG, "Video saved successfully")
                    }

                    override fun onError(videoCaptureError: Int, message: String, cause: Throwable?) {
                        Log.e(TAG, "Error recording video: $message", cause)
                    }
                }
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting video recording", e)
            Result.failure(e)
        }
    }

    suspend fun stopVideoRecording() {
        try {
            videoCapture?.stopRecording()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping video recording", e)
        }
    }

    fun getPreviewSurface(): androidx.camera.view.PreviewView? {
        // This will be implemented when we have the UI components
        return null
    }

    fun setPreviewSurface(previewView: androidx.camera.view.PreviewView) {
        preview?.setSurfaceProvider(previewView.surfaceProvider)
    }

    fun enableAudio(enabled: Boolean) {
        audioEnabled = enabled
        // Audio configuration will be handled in video recording
    }

    fun release() {
        cameraExecutor.shutdown()
    }

    private fun VideoResolution.toSize(): android.util.Size {
        return android.util.Size(width, height)
    }
}

enum class CameraState {
    IDLE,
    STARTING,
    READY,
    RECORDING,
    STOPPING,
    ERROR
}