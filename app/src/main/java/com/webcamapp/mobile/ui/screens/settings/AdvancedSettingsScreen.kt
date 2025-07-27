package com.webcamapp.mobile.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
<<<<<<< HEAD
import androidx.compose.foundation.text.KeyboardOptions
=======
>>>>>>> origin/main
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
<<<<<<< HEAD
import androidx.compose.ui.text.input.KeyboardType
=======
>>>>>>> origin/main
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.webcamapp.mobile.advanced.*
import com.webcamapp.mobile.optimization.*
import com.webcamapp.mobile.playback.VideoPlaybackManager
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdvancedSettingsScreen(navController: NavController) {
    val viewModel: AdvancedSettingsViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    
    val privacyZones by viewModel.privacyZones.collectAsState()
    val aiMotionDetection by viewModel.aiMotionDetection.collectAsState()
    val advancedRecording by viewModel.advancedRecording.collectAsState()
    val performanceMode by viewModel.performanceMode.collectAsState()
    val optimizationSettings by viewModel.optimizationSettings.collectAsState()
    val analytics by viewModel.analytics.collectAsState()
<<<<<<< HEAD
    val dateFormat by viewModel.dateFormat.collectAsState()
    val now = remember(dateFormat) { java.util.Date() }
    val formattedNow = remember(dateFormat, now) {
        try {
            java.text.SimpleDateFormat(dateFormat, java.util.Locale.getDefault()).format(now)
        } catch (e: Exception) {
            "Invalid format"
        }
    }
    var dateFormatInput by remember { mutableStateOf(dateFormat) }
    var showDateFormatError by remember { mutableStateOf(false) }
=======
>>>>>>> origin/main

    var showAddPrivacyZone by remember { mutableStateOf(false) }
    var showPerformanceDialog by remember { mutableStateOf(false) }
    var showAnalyticsDialog by remember { mutableStateOf(false) }

<<<<<<< HEAD
    val storageLimitGB by viewModel.storageLimitGB.collectAsState()
    val storageInfo by viewModel.storageInfo.collectAsState()
    var storageLimitInput by remember { mutableStateOf(storageLimitGB?.toString() ?: "") }
    var unlimitedStorage by remember { mutableStateOf(storageLimitGB == null) }

=======
>>>>>>> origin/main
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
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            
            Text(
                text = "Advanced Settings",
                style = MaterialTheme.typography.headlineMedium
            )
            
            IconButton(onClick = { showAnalyticsDialog = true }) {
                Icon(Icons.Default.Analytics, contentDescription = "Analytics")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Performance Optimization Section
            item {
                PerformanceOptimizationCard(
                    performanceMode = performanceMode,
                    optimizationSettings = optimizationSettings,
                    onPerformanceModeChange = { viewModel.setPerformanceMode(it) },
                    onShowPerformanceDialog = { showPerformanceDialog = true }
                )
            }

            // Privacy Zones Section
            item {
                PrivacyZonesCard(
                    privacyZones = privacyZones,
                    onAddZone = { showAddPrivacyZone = true },
                    onDeleteZone = { viewModel.removePrivacyZone(it) },
                    onToggleZone = { zone -> viewModel.updatePrivacyZone(zone) }
                )
            }

            // AI Motion Detection Section
            item {
                AIMotionDetectionCard(
                    aiMotionDetection = aiMotionDetection,
                    onToggleAI = { viewModel.enableAIMotionDetection(it) }
                )
            }

            // Advanced Recording Section
            item {
                AdvancedRecordingCard(
                    advancedRecording = advancedRecording,
                    onConfigChange = { viewModel.updateAdvancedRecordingConfig(it) }
                )
            }

            // Analytics Section
            item {
                AnalyticsCard(
                    analytics = analytics,
                    onResetAnalytics = { viewModel.resetAnalytics() }
                )
            }
<<<<<<< HEAD

            // Date/Time Format Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Date/Time Format", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = dateFormatInput,
                            onValueChange = {
                                dateFormatInput = it
                                showDateFormatError = false
                            },
                            label = { Text("Date/Time Format Pattern") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            isError = showDateFormatError,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Preview: $formattedNow",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (showDateFormatError || formattedNow == "Invalid format") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row {
                            Button(onClick = {
                                try {
                                    java.text.SimpleDateFormat(dateFormatInput, java.util.Locale.getDefault()).format(now)
                                    viewModel.setDateFormat(dateFormatInput)
                                    showDateFormatError = false
                                } catch (e: Exception) {
                                    showDateFormatError = true
                                }
                            }) {
                                Text("Apply")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(onClick = {
                                dateFormatInput = "yyyy-MM-dd HH:mm:ss"
                                viewModel.setDateFormat("yyyy-MM-dd HH:mm:ss")
                                showDateFormatError = false
                            }) {
                                Text("Reset Default")
                            }
                        }
                        if (showDateFormatError || formattedNow == "Invalid format") {
                            Text(
                                text = "Invalid date/time format pattern.",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "See: https://developer.android.com/reference/java/text/SimpleDateFormat",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Storage Limit Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Storage Limit", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = unlimitedStorage,
                                onCheckedChange = {
                                    unlimitedStorage = it
                                    if (it) viewModel.setStorageLimitGB(null)
                                }
                            )
                            Text("Unlimited Storage")
                        }
                        if (!unlimitedStorage) {
                            TextField(
                                value = storageLimitInput,
                                onValueChange = {
                                    storageLimitInput = it.filter { c -> c.isDigit() || c == '.' }
                                },
                                label = { Text("Max Storage (GB)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(onClick = {
                                val gb = storageLimitInput.toFloatOrNull()
                                if (gb != null && gb > 0) {
                                    viewModel.setStorageLimitGB(gb)
                                }
                            }) {
                                Text("Apply")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Current Usage: ${storageInfo.totalSizeMB / 1024} GB / ${storageLimitGB?.let { "$it GB" } ?: "Unlimited"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
=======
>>>>>>> origin/main
        }
    }

    // Add Privacy Zone Dialog
    if (showAddPrivacyZone) {
        AddPrivacyZoneDialog(
            onDismiss = { showAddPrivacyZone = false },
            onAddZone = { zone ->
                viewModel.addPrivacyZone(zone)
                showAddPrivacyZone = false
            }
        )
    }

    // Performance Dialog
    if (showPerformanceDialog) {
        PerformanceDialog(
            performanceMode = performanceMode,
            onDismiss = { showPerformanceDialog = false },
            onModeChange = { viewModel.setPerformanceMode(it) }
        )
    }

    // Analytics Dialog
    if (showAnalyticsDialog) {
        AnalyticsDialog(
            analytics = analytics,
            onDismiss = { showAnalyticsDialog = false }
        )
    }
}

@Composable
fun PerformanceOptimizationCard(
    performanceMode: PerformanceMode,
    optimizationSettings: OptimizationSettings,
    onPerformanceModeChange: (PerformanceMode) -> Unit,
    onShowPerformanceDialog: () -> Unit
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
                Text(
                    text = "Performance Optimization",
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = onShowPerformanceDialog) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Current Mode: ${performanceMode.name.replace("_", " ")}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Frame Rate: ${optimizationSettings.frameRate} FPS",
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = "Resolution: ${optimizationSettings.resolution.name}",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { onPerformanceModeChange(PerformanceMode.HIGH_PERFORMANCE) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (performanceMode == PerformanceMode.HIGH_PERFORMANCE) 
                            MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text("High")
                }
                
                Button(
                    onClick = { onPerformanceModeChange(PerformanceMode.BALANCED) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (performanceMode == PerformanceMode.BALANCED) 
                            MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text("Balanced")
                }
                
                Button(
                    onClick = { onPerformanceModeChange(PerformanceMode.BATTERY_SAVE) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (performanceMode == PerformanceMode.BATTERY_SAVE) 
                            MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text("Battery")
                }
            }
        }
    }
}

@Composable
fun PrivacyZonesCard(
    privacyZones: List<PrivacyZone>,
    onAddZone: () -> Unit,
    onDeleteZone: (String) -> Unit,
    onToggleZone: (PrivacyZone) -> Unit
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
                Text(
                    text = "Privacy Zones",
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = onAddZone) {
                    Icon(Icons.Default.Add, contentDescription = "Add Zone")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (privacyZones.isEmpty()) {
                Text(
                    text = "No privacy zones configured",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                privacyZones.forEach { zone ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = zone.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Position: (${(zone.x * 100).toInt()}%, ${(zone.y * 100).toInt()}%)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Switch(
                            checked = zone.isActive,
                            onCheckedChange = { 
                                onToggleZone(zone.copy(isActive = it))
                            }
                        )
                        
                        IconButton(onClick = { onDeleteZone(zone.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
fun AIMotionDetectionCard(
    aiMotionDetection: Boolean,
    onToggleAI: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "AI Motion Detection",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Use AI-powered motion detection for improved accuracy",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enable AI Detection",
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked = aiMotionDetection,
                    onCheckedChange = onToggleAI
                )
            }
        }
    }
}

@Composable
fun AdvancedRecordingCard(
    advancedRecording: AdvancedRecordingConfig,
    onConfigChange: (AdvancedRecordingConfig) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Advanced Recording",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Frame Rate: ${advancedRecording.frameRate} FPS",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Quality: ${advancedRecording.quality.name}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Pre-motion Buffer: ${advancedRecording.preMotionBuffer / 1000}s",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Post-motion Buffer: ${advancedRecording.postMotionBuffer / 1000}s",
                style = MaterialTheme.typography.bodyMedium
            )

            if (advancedRecording.isScheduled) {
                Text(
                    text = "Scheduled Recording: Enabled",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun AnalyticsCard(
    analytics: AnalyticsReport,
    onResetAnalytics: () -> Unit
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
                Text(
                    text = "Camera Analytics",
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = onResetAnalytics) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Motion Events: ${analytics.totalMotionEvents}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Recordings: ${analytics.totalRecordings}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Total Recording Time: ${analytics.totalRecordingTime / 1000 / 60} minutes",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "AI Motion Events: ${analytics.aiMotionEvents}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Uptime: ${analytics.uptime / 1000 / 60 / 60} hours",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun AddPrivacyZoneDialog(
    onDismiss: () -> Unit,
    onAddZone: (PrivacyZone) -> Unit
) {
    var zoneName by remember { mutableStateOf("") }
    var zoneX by remember { mutableStateOf(0.1f) }
    var zoneY by remember { mutableStateOf(0.1f) }
    var zoneWidth by remember { mutableStateOf(0.3f) }
    var zoneHeight by remember { mutableStateOf(0.3f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Privacy Zone") },
        text = {
            Column {
                TextField(
                    value = zoneName,
                    onValueChange = { zoneName = it },
                    label = { Text("Zone Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("Position (0.0 - 1.0):")
                Text("X: $zoneX, Y: $zoneY")
                Text("Width: $zoneWidth, Height: $zoneHeight")
                
                // Sliders for position and size
                Text("X Position")
                Slider(
                    value = zoneX,
                    onValueChange = { zoneX = it },
                    valueRange = 0f..1f
                )
                
                Text("Y Position")
                Slider(
                    value = zoneY,
                    onValueChange = { zoneY = it },
                    valueRange = 0f..1f
                )
                
                Text("Width")
                Slider(
                    value = zoneWidth,
                    onValueChange = { zoneWidth = it },
                    valueRange = 0.1f..1f
                )
                
                Text("Height")
                Slider(
                    value = zoneHeight,
                    onValueChange = { zoneHeight = it },
                    valueRange = 0.1f..1f
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (zoneName.isNotBlank()) {
                        val zone = PrivacyZone(
                            id = UUID.randomUUID().toString(),
                            name = zoneName,
                            x = zoneX,
                            y = zoneY,
                            width = zoneWidth,
                            height = zoneHeight
                        )
                        onAddZone(zone)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PerformanceDialog(
    performanceMode: PerformanceMode,
    onDismiss: () -> Unit,
    onModeChange: (PerformanceMode) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Performance Mode") },
        text = {
            Column {
                PerformanceMode.values().forEach { mode ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = performanceMode == mode,
                            onClick = { onModeChange(mode) }
                        )
                        Text(
                            text = mode.name.replace("_", " "),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@Composable
fun AnalyticsDialog(
    analytics: AnalyticsReport,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detailed Analytics") },
        text = {
            Column {
                Text("Total Motion Events: ${analytics.totalMotionEvents}")
                Text("Total Recordings: ${analytics.totalRecordings}")
                Text("Total Recording Time: ${analytics.totalRecordingTime / 1000 / 60} minutes")
                Text("Privacy Zone Events: ${analytics.privacyZoneEvents}")
                Text("AI Motion Events: ${analytics.aiMotionEvents}")
                Text("Average AI Confidence: ${String.format("%.2f", analytics.averageAIConfidence)}")
                Text("Uptime: ${analytics.uptime / 1000 / 60 / 60} hours")
                Text("Active Privacy Zones: ${analytics.activePrivacyZones}")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}