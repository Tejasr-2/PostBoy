package com.webcamapp.mobile.ui.components

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun InteractiveVideoPlayer(
    videoUri: Uri,
    modifier: Modifier = Modifier,
    availableQualities: List<String> = listOf("HD", "SD"),
    onQualityChange: (String) -> Unit = {},
    onSpeedChange: (Float) -> Unit = {},
    initialQuality: String = "HD",
    initialSpeed: Float = 1.0f
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(true) }
    var showControls by remember { mutableStateOf(true) }
    var selectedQuality by remember { mutableStateOf(initialQuality) }
    var selectedSpeed by remember { mutableStateOf(initialSpeed) }
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()
            playWhenReady = true
        }
    }
    DisposableEffect(videoUri) {
        player.setMediaItem(MediaItem.fromUri(videoUri))
        player.prepare()
        onDispose { player.release() }
    }
    LaunchedEffect(isPlaying) {
        player.playWhenReady = isPlaying
    }
    LaunchedEffect(selectedSpeed) {
        player.setPlaybackSpeed(selectedSpeed)
    }
    Box(modifier = modifier.background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    this.player = player
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        if (showControls) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Play/Pause
                    IconButton(onClick = { isPlaying = !isPlaying }) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White
                        )
                    }
                    // Seek Back
                    IconButton(onClick = { player.seekBack() }) {
                        Icon(Icons.Default.FastRewind, contentDescription = "Rewind", tint = Color.White)
                    }
                    // Seek Forward
                    IconButton(onClick = { player.seekForward() }) {
                        Icon(Icons.Default.FastForward, contentDescription = "Forward", tint = Color.White)
                    }
                    // Quality
                    var showQualityMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showQualityMenu = !showQualityMenu }) {
                            Icon(
                                imageVector = Icons.Default.Hd,
                                contentDescription = "Quality",
                                tint = if (selectedQuality == "HD") Color(0xFFFF9800) else Color.Gray
                            )
                        }
                        DropdownMenu(
                            expanded = showQualityMenu,
                            onDismissRequest = { showQualityMenu = false }
                        ) {
                            availableQualities.forEach { quality ->
                                DropdownMenuItem(
                                    text = { Text(quality) },
                                    onClick = {
                                        selectedQuality = quality
                                        onQualityChange(quality)
                                        showQualityMenu = false
                                    }
                                )
                            }
                        }
                    }
                    // Speed
                    var showSpeedMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showSpeedMenu = !showSpeedMenu }) {
                            Icon(Icons.Default.Speed, contentDescription = "Speed", tint = Color.White)
                        }
                        DropdownMenu(
                            expanded = showSpeedMenu,
                            onDismissRequest = { showSpeedMenu = false }
                        ) {
                            listOf(0.25f, 0.5f, 1.0f, 1.5f, 2.0f).forEach { speed ->
                                DropdownMenuItem(
                                    text = { Text("${speed}x") },
                                    onClick = {
                                        selectedSpeed = speed
                                        onSpeedChange(speed)
                                        showSpeedMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        // Tap to show/hide controls
        Box(modifier = Modifier.fillMaxSize().background(Color.Transparent).clickable {
            showControls = !showControls
        })
    }
}