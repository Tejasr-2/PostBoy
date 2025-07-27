package com.webcamapp.mobile.ui.screens.camera

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.view.PreviewView
import androidx.navigation.NavController
import com.webcamapp.mobile.camera.CameraManager
import com.webcamapp.mobile.camera.CameraState
import com.webcamapp.mobile.data.model.CameraSelection
import com.webcamapp.mobile.data.model.VideoResolution
import com.webcamapp.mobile.power.PowerManager
import com.webcamapp.mobile.recording.RecordingManager
import com.webcamapp.mobile.recording.RecordingState
import com.webcamapp.mobile.service.CameraService
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectAsState
import kotlinx.coroutines.launch

@Composable
fun CameraScreen(navController: NavController) {
    val viewModel: CameraViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val cameraManager = viewModel.cameraManager
    val recordingManager = viewModel.recordingManager
    val powerManager = viewModel.powerManager
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        
        val cameraState by cameraManager.cameraState.collectAsState()
        val recordingState by recordingManager.recordingState.collectAsState()
        val batteryLevel by powerManager.batteryLevel.collectAsState()
        val isOverheating by powerManager.isOverheating.collectAsState()
        val currentCamera by cameraManager.currentCameraSelection.collectAsState()
        val isFlashEnabled by cameraManager.isFlashEnabled.collectAsState()

        var showSettings by remember { mutableStateOf(false) }
        var previewView by remember { mutableStateOf<PreviewView?>(null) }

        LaunchedEffect(Unit) {
            // Start camera when screen is created
            cameraManager.startCamera()
        }

        LaunchedEffect(previewView) {
            previewView?.let { view ->
                cameraManager.setPreviewSurface(view)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Status Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Battery indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BatteryFull,
                        contentDescription = "Battery",
                        tint = when {
                            batteryLevel <= 15 -> MaterialTheme.colorScheme.error
                            batteryLevel <= 30 -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("$batteryLevel%")
                }

                // Overheating indicator
                if (isOverheating) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Overheating",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Overheating", color = MaterialTheme.colorScheme.error)
                    }
                }

                // Settings button
                IconButton(onClick = { showSettings = true }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Camera Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .aspectRatio(16f / 9f)
            ) {
                AndroidView(
                    factory = { context ->
                        PreviewView(context).apply {
                            previewView = this
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Camera state overlay
                when (cameraState) {
                    CameraState.STARTING -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    CameraState.ERROR -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Camera Error",
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    else -> {}
                }

                // Recording indicator
                if (recordingState == RecordingState.RECORDING) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FiberManualRecord,
                                contentDescription = "Recording",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("REC", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Camera Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Switch camera button
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            cameraManager.switchCamera()
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(
                        imageVector = if (currentCamera == CameraSelection.FRONT) 
                            Icons.Default.CameraRear else Icons.Default.CameraFront,
                        contentDescription = "Switch Camera"
                    )
                }

                // Record button
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            if (recordingState == RecordingState.RECORDING) {
                                recordingManager.stopRecording()
                            } else {
                                recordingManager.startRecording()
                            }
                        }
                    },
                    containerColor = if (recordingState == RecordingState.RECORDING) 
                        MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = if (recordingState == RecordingState.RECORDING) 
                            Icons.Default.Stop else Icons.Default.FiberManualRecord,
                        contentDescription = if (recordingState == RecordingState.RECORDING) 
                            "Stop Recording" else "Start Recording"
                    )
                }

                // Flash button
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            cameraManager.toggleFlash()
                        }
                    },
                    containerColor = if (isFlashEnabled) 
                        MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
                ) {
                    Icon(
                        imageVector = if (isFlashEnabled) 
                            Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Toggle Flash"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Start/Stop camera service
                Button(
                    onClick = {
                        val intent = Intent(context, CameraService::class.java).apply {
                            action = if (cameraState == CameraState.IDLE) 
                                CameraService.ACTION_START_CAMERA else CameraService.ACTION_STOP_CAMERA
                        }
                        context.startService(intent)
                    }
                ) {
                    Text(if (cameraState == CameraState.IDLE) "Start Camera" else "Stop Camera")
                }

                // Change role button
                Button(
                    onClick = { navController.navigate("role_selection") }
                ) {
                    Text("Change Role")
                }
            }
        }

        // Settings dialog
        if (showSettings) {
            CameraSettingsDialog(
                onDismiss = { showSettings = false },
                onResolutionChanged = { resolution ->
                    scope.launch {
                        cameraManager.setResolution(resolution)
                    }
                }
            )
        }
    }
}

@Composable
fun CameraSettingsDialog(
    onDismiss: () -> Unit,
    onResolutionChanged: (VideoResolution) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Camera Settings") },
        text = {
            Column {
                Text("Resolution", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                VideoResolution.values().forEach { resolution ->
                    TextButton(
                        onClick = { onResolutionChanged(resolution) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(resolution.label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}