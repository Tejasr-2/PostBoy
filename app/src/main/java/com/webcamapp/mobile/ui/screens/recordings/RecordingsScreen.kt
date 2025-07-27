package com.webcamapp.mobile.ui.screens.recordings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.stickyHeader
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.webcamapp.mobile.data.model.Recording
import java.text.SimpleDateFormat

@Composable
fun RecordingsScreen(navController: NavController) {
    // TODO: Wire to real ViewModel data
    val recordingsByDay = mapOf<String, List<Recording>>()
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    val listState = rememberLazyListState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Search/Filter Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search recordings") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        // Recordings List
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            recordingsByDay.forEach { (date, recs) ->
                stickyHeader {
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
                        Text(
                            text = date,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
                itemsIndexed(recs) { idx, rec ->
                    var dismissed by remember { mutableStateOf(false) }
                    if (!dismissed && (searchQuery.text.isBlank() || rec.fileName.contains(searchQuery.text, true))) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .pointerInput(Unit) {
                                    detectHorizontalDragGestures { change, dragAmount ->
                                        if (dragAmount > 100) dismissed = true // Swipe right to delete
                                    }
                                }
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(rec.filePath),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            rec.fileName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 1
                                        )
                                        Text(
                                            SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(rec.startTime)),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    IconButton(onClick = { /* Play recording */ }) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                                    }
                                    IconButton(onClick = { dismissed = true }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}