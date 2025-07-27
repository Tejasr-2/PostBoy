package com.webcamapp.mobile.ui.screens.home

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.webcamapp.mobile.data.model.Device
import com.webcamapp.mobile.data.model.Recording
import com.webcamapp.mobile.ui.components.InteractiveVideoPlayer
import java.text.SimpleDateFormat

@Composable
fun HomeScreen(navController: NavController) {
    // Fake data for preview; replace with real ViewModel wiring
    val devices = listOf(Device("dev1", "Front Door", true, 85, 100, "192.168.1.10"))
    var selectedDevice by remember { mutableStateOf(devices.first()) }
    val recentRecordings = listOf<Recording>() // TODO: wire to real data
    var isHD by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }
    val liveStreamUri = remember { mutableStateOf<Uri?>(null) } // TODO: wire to real stream

    Column(modifier = Modifier.fillMaxSize()) {
        // Device Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Device:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.width(8.dp))
            ExposedDropdownMenuBox(
                expanded = false,
                onExpandedChange = {}
            ) {
                TextField(
                    value = selectedDevice.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Device") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(false) },
                    modifier = Modifier.menuAnchor().weight(1f)
                )
                // TODO: Add dropdown menu for device selection
            }
        }
        // Live Video
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (liveStreamUri.value != null) {
                InteractiveVideoPlayer(
                    videoUri = liveStreamUri.value!!,
                    availableQualities = listOf("HD", "SD"),
                    onQualityChange = { isHD = it == "HD" },
                    initialQuality = if (isHD) "HD" else "SD"
                )
            } else {
                Icon(Icons.Default.Videocam, contentDescription = "Live", tint = Color.Gray, modifier = Modifier.size(96.dp))
            }
            // Overlay Controls
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { isHD = !isHD }) {
                    Icon(
                        imageVector = Icons.Default.Hd,
                        contentDescription = "HD Toggle",
                        tint = if (isHD) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
                IconButton(onClick = { isRecording = !isRecording }) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                        contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                        tint = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = { isMuted = !isMuted }) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = if (isMuted) "Unmute" else "Mute",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = { isFullscreen = !isFullscreen }) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "Fullscreen",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        // Recent Recordings
        Text(
            "Recent Recordings",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
        )
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp)
        ) {
            recentRecordings.forEach { rec ->
                Card(
                    modifier = Modifier
                        .width(140.dp)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { /* Play recording */ },
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column {
                        Image(
                            painter = rememberAsyncImagePainter(rec.filePath),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        )
                        Text(
                            SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(rec.startTime)),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}