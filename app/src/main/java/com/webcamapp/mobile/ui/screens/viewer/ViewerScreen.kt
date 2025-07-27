package com.webcamapp.mobile.ui.screens.viewer

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.navigation.NavController
import com.google.zxing.integration.android.IntentIntegrator
import com.webcamapp.mobile.data.model.Device
import com.webcamapp.mobile.pairing.DevicePairingManager
import com.webcamapp.mobile.pairing.PairingResult
import com.webcamapp.mobile.pairing.PairingState
import com.webcamapp.mobile.streaming.StreamingService
import com.webcamapp.mobile.webrtc.ConnectionState
import com.webcamapp.mobile.webrtc.WebRTCManager
import org.webrtc.SurfaceViewRenderer
import javax.inject.Inject

@Composable
fun ViewerScreen(navController: NavController) {
    val viewModel: ViewerViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val pairedDevices by viewModel.pairedDevices.collectAsState()
    val pairingState by viewModel.pairingState.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val isStreaming by viewModel.isStreaming.collectAsState()
    val currentDevice by viewModel.currentDevice.collectAsState()

    var showQRScanner by remember { mutableStateOf(false) }
    var showDeviceDetails by remember { mutableStateOf<Device?>(null) }
    var showSettings by remember { mutableStateOf(false) }

    // QR Scanner launcher
    val qrScannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val scanResult = IntentIntegrator.parseActivityResult(result.resultCode, result.data)
            scanResult?.contents?.let { qrData ->
                scope.launch {
                    viewModel.pairWithDevice(qrData)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Viewer Device",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Row {
                IconButton(onClick = { navController.navigate("advanced_settings") }) {
                    Icon(Icons.Default.Settings, contentDescription = "Advanced Settings")
                }
                IconButton(onClick = { showQRScanner = true }) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan QR Code")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Connection Status
        ConnectionStatusCard(connectionState, isStreaming)

        Spacer(modifier = Modifier.height(16.dp))

        // Paired Devices
        Text(
            text = "Paired Camera Devices",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (pairedDevices.isEmpty()) {
            EmptyDevicesCard(
                onScanQR = { showQRScanner = true }
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(pairedDevices) { device ->
                    DeviceCard(
                        device = device,
                        isConnected = currentDevice?.id == device.id && isStreaming,
                        onConnect = {
                            scope.launch {
                                viewModel.connectToDevice(device)
                            }
                        },
                        onDisconnect = {
                            scope.launch {
                                viewModel.disconnectFromDevice(device.id)
                            }
                        },
                        onDetails = { showDeviceDetails = device },
                        onUnpair = {
                            scope.launch {
                                viewModel.unpairDevice(device.id)
                            }
                        }
                    )
                }
            }
        }

        // Bottom Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { showQRScanner = true },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Camera")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = { navController.navigate("role_selection") },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.SwapHoriz, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Change Role")
            }
        }
    }

    // QR Scanner Dialog
    if (showQRScanner) {
        QRScannerDialog(
            onDismiss = { showQRScanner = false },
            onQRCodeScanned = { qrData ->
                scope.launch {
                    viewModel.pairWithDevice(qrData)
                }
                showQRScanner = false
            }
        )
    }

    // Pairing State Dialog
    when (pairingState) {
        PairingState.PAIRING -> {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("Pairing Device") },
                text = { 
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Connecting to camera device...")
                    }
                },
                confirmButton = { }
            )
        }
        PairingState.SUCCESS -> {
            AlertDialog(
                onDismissRequest = { viewModel.resetPairingState() },
                title = { Text("Pairing Successful") },
                text = { Text("Successfully paired with camera device!") },
                confirmButton = {
                    TextButton(onClick = { viewModel.resetPairingState() }) {
                        Text("OK")
                    }
                }
            )
        }
        PairingState.FAILED -> {
            AlertDialog(
                onDismissRequest = { viewModel.resetPairingState() },
                title = { Text("Pairing Failed") },
                text = { Text("Failed to pair with camera device. Please try again.") },
                confirmButton = {
                    TextButton(onClick = { viewModel.resetPairingState() }) {
                        Text("OK")
                    }
                }
            )
        }
        else -> {}
    }

    // Device Details Dialog
    showDeviceDetails?.let { device ->
        DeviceDetailsDialog(
            device = device,
            onDismiss = { showDeviceDetails = null },
            onConnect = {
                scope.launch {
                    viewModel.connectToDevice(device)
                }
                showDeviceDetails = null
            },
            onUnpair = {
                scope.launch {
                    viewModel.unpairDevice(device.id)
                }
                showDeviceDetails = null
            }
        )
    }

    // Settings Dialog
    if (showSettings) {
        ViewerSettingsDialog(
            onDismiss = { showSettings = false }
        )
    }
}

@Composable
fun ConnectionStatusCard(connectionState: ConnectionState, isStreaming: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (connectionState) {
                ConnectionState.CONNECTED -> MaterialTheme.colorScheme.primaryContainer
                ConnectionState.CONNECTING -> MaterialTheme.colorScheme.tertiaryContainer
                ConnectionState.FAILED -> MaterialTheme.colorScheme.errorContainer
                ConnectionState.DISCONNECTED -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (connectionState) {
                    ConnectionState.CONNECTED -> Icons.Default.Wifi
                    ConnectionState.CONNECTING -> Icons.Default.Sync
                    ConnectionState.FAILED -> Icons.Default.Error
                    ConnectionState.DISCONNECTED -> Icons.Default.WifiOff
                },
                contentDescription = "Connection Status",
                tint = when (connectionState) {
                    ConnectionState.CONNECTED -> MaterialTheme.colorScheme.primary
                    ConnectionState.CONNECTING -> MaterialTheme.colorScheme.tertiary
                    ConnectionState.FAILED -> MaterialTheme.colorScheme.error
                    ConnectionState.DISCONNECTED -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = when (connectionState) {
                        ConnectionState.CONNECTED -> "Connected"
                        ConnectionState.CONNECTING -> "Connecting..."
                        ConnectionState.FAILED -> "Connection Failed"
                        ConnectionState.DISCONNECTED -> "Disconnected"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
                
                if (isStreaming) {
                    Text(
                        text = "Live streaming active",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyDevicesCard(onScanQR: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = "No Devices",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No camera devices paired",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Scan a QR code to pair with a camera device",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(onClick = onScanQR) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scan QR Code")
            }
        }
    }
}

@Composable
fun DeviceCard(
    device: Device,
    isConnected: Boolean,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onDetails: () -> Unit,
    onUnpair: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = device.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (device.isOnline) Icons.Default.Circle else Icons.Default.Circle,
                            contentDescription = "Status",
                            modifier = Modifier.size(8.dp),
                            tint = if (device.isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = if (device.isOnline) "Online" else "Offline",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "Battery: ${device.batteryLevel}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Row {
                    IconButton(onClick = onDetails) {
                        Icon(Icons.Default.Info, contentDescription = "Device Details")
                    }
                    
                    if (isConnected) {
                        IconButton(onClick = onDisconnect) {
                            Icon(Icons.Default.Stop, contentDescription = "Disconnect")
                        }
                    } else {
                        IconButton(onClick = onConnect) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Connect")
                        }
                    }
                }
            }
            
            if (isConnected) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun QRScannerDialog(
    onDismiss: () -> Unit,
    onQRCodeScanned: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Scan QR Code") },
        text = { 
            Text("Point your camera at the QR code on the camera device to pair.")
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeviceDetailsDialog(
    device: Device,
    onDismiss: () -> Unit,
    onConnect: () -> Unit,
    onUnpair: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(device.name) },
        text = {
            Column {
                Text("Device ID: ${device.id}")
                Text("Status: ${if (device.isOnline) "Online" else "Offline"}")
                Text("Battery: ${device.batteryLevel}%")
                Text("Storage: ${device.storageRemaining} MB remaining")
                Text("Last seen: ${java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(device.lastSeen))}")
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = onConnect) {
                    Text("Connect")
                }
                TextButton(onClick = onUnpair) {
                    Text("Unpair")
                }
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}

@Composable
fun ViewerSettingsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Viewer Settings") },
        text = {
            Column {
                Text("Signaling Server: ws://localhost:8080")
                Text("Auto-reconnect: Enabled")
                Text("Stream Quality: HD")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}