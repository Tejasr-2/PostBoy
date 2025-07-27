package com.webcamapp.mobile.recording

import android.content.Context
import android.util.Log
import com.webcamapp.mobile.data.model.MotionEvent
import com.webcamapp.mobile.data.model.Recording
import com.webcamapp.mobile.data.model.RecordingType
import com.webcamapp.mobile.advanced.AdvancedCameraManager
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val advancedCameraManager: AdvancedCameraManager
) {
    private var currentRecording: Recording? = null
    private var recordingFile: File? = null
    private var isRecording = false
    private var storageLimitGB: Float? = null

    private val _recordingState = MutableStateFlow(RecordingState.IDLE)
    val recordingState: StateFlow<RecordingState> = _recordingState

    private val _currentRecordingDuration = MutableStateFlow(0L)
    val currentRecordingDuration: StateFlow<Long> = _currentRecordingDuration

    private val _storageInfo = MutableStateFlow(StorageInfo())
    val storageInfo: StateFlow<StorageInfo> = _storageInfo

    var dateFormat: String = "yyyy-MM-dd HH:mm:ss"

    private val MAX_SEGMENT_DURATION_MS = 4 * 60 * 60 * 1000L // 4 hours in ms
    private var segmentStartTime: Long = 0L
    private var segmentJob: kotlinx.coroutines.Job? = null

    companion object {
        private const val TAG = "RecordingManager"
        private const val RECORDINGS_DIR = "recordings"
        private const val THUMBNAILS_DIR = "thumbnails"
        private const val MAX_STORAGE_USAGE_MB = 1024L // 1GB
        private const val MOTION_RECORDING_DURATION_MS = 30000L // 30 seconds
    }

    init {
        createDirectories()
        updateStorageInfo()
    }

    private fun createDirectories() {
        val recordingsDir = File(context.filesDir, RECORDINGS_DIR)
        val thumbnailsDir = File(context.filesDir, THUMBNAILS_DIR)
        
        if (!recordingsDir.exists()) {
            recordingsDir.mkdirs()
        }
        if (!thumbnailsDir.exists()) {
            thumbnailsDir.mkdirs()
        }
    }

    suspend fun startRecording(recordingType: RecordingType = RecordingType.CONTINUOUS): Result<Recording> {
        if (isRecording) {
            Log.w(TAG, "Recording is already in progress")
            return Result.failure(IllegalStateException("Recording already in progress"))
        }

        return try {
            // Check storage space
            if (!hasEnoughStorageSpace()) {
                cleanupOldRecordings()
                if (!hasEnoughStorageSpace()) {
                    return Result.failure(IllegalStateException("Insufficient storage space"))
                }
            }

            val recordingFile = createRecordingFile(recordingType)
            val recording = Recording(
                id = java.util.UUID.randomUUID().toString(),
                deviceId = "", // Will be set by the service
                filePath = recordingFile.absolutePath,
                fileName = recordingFile.name,
                fileSize = 0L,
                duration = 0L,
                startTime = System.currentTimeMillis(),
                endTime = 0L,
                recordingType = recordingType,
                isUploaded = false
            )

            this.recordingFile = recordingFile
            this.currentRecording = recording
            this.isRecording = true
            _recordingState.value = RecordingState.RECORDING
            _currentRecordingDuration.value = 0L

            segmentStartTime = System.currentTimeMillis()
            segmentJob?.cancel()
            segmentJob = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                while (isRecording) {
                    val elapsed = System.currentTimeMillis() - segmentStartTime
                    if (elapsed >= MAX_SEGMENT_DURATION_MS) {
                        stopRecording()
                        startRecording() // start new segment
                        break
                    }
                    kotlinx.coroutines.delay(60_000) // check every minute
                }
            }

            Log.d(TAG, "Started ${recordingType.name} recording: ${recordingFile.name}")
            Result.success(recording)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting recording", e)
            Result.failure(e)
        }
    }

    suspend fun startMotionRecording(motionEvent: MotionEvent): Result<Recording> {
        return startRecording(RecordingType.MOTION_TRIGGERED).also { result ->
            result.onSuccess { recording ->
                // Schedule automatic stop for motion recordings
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    kotlinx.coroutines.delay(MOTION_RECORDING_DURATION_MS)
                    if (isRecording && currentRecording?.id == recording.id) {
                        stopRecording()
                    }
                }
            }
        }
    }

    suspend fun stopRecording(): Result<Recording?> {
        segmentJob?.cancel()
        if (!isRecording) {
            Log.w(TAG, "No recording in progress")
            return Result.success(null)
        }

        return try {
            val recording = currentRecording ?: throw IllegalStateException("No current recording")
            val recordingFile = recordingFile ?: throw IllegalStateException("No recording file")

            val endTime = System.currentTimeMillis()
            val duration = endTime - recording.startTime
            val fileSize = recordingFile.length()

            val updatedRecording = recording.copy(
                endTime = endTime,
                duration = duration,
                fileSize = fileSize
            )

            this.isRecording = false
            this.currentRecording = null
            this.recordingFile = null
            _recordingState.value = RecordingState.IDLE
            _currentRecordingDuration.value = 0L

            // Update storage info
            updateStorageInfo()

            // Post-process video to overlay date/time and app name
            val overlayedFile = File(recordingFile.parent, "overlayed_${recordingFile.name}")
            val overlaySuccess = overlayDateTimeAndAppNameOnVideo(recordingFile, overlayedFile, dateFormat, "WebcamApp")
            val finalFile = if (overlaySuccess) {
                // Replace original file
                if (recordingFile.delete()) {
                    overlayedFile.renameTo(recordingFile)
                    recordingFile
                } else {
                    overlayedFile // fallback: keep overlayed file
                }
            } else {
                recordingFile // fallback: original file
            }
            val finalRecording = updatedRecording.copy(filePath = finalFile.absolutePath, fileSize = finalFile.length())

            Log.d(TAG, "Stopped recording: ${finalRecording.fileName}, duration: ${finalRecording.duration}ms, size: ${finalRecording.fileSize}bytes, overlay: $overlaySuccess")
            Result.success(finalRecording)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recording", e)
            Result.failure(e)
        }
    }

    private fun createRecordingFile(recordingType: RecordingType): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val typePrefix = when (recordingType) {
            RecordingType.CONTINUOUS -> "continuous"
            RecordingType.MOTION_TRIGGERED -> "motion"
            RecordingType.MANUAL -> "manual"
        }
        val fileName = "${typePrefix}_${timestamp}.mp4"
        return File(File(context.filesDir, RECORDINGS_DIR), fileName)
    }

    private fun hasEnoughStorageSpace(): Boolean {
        val availableSpace = context.filesDir.freeSpace
        val requiredSpace = MAX_STORAGE_USAGE_MB * 1024 * 1024 // Convert to bytes
        return availableSpace > requiredSpace
    }

    private fun cleanupOldRecordings() {
        val recordingsDir = File(context.filesDir, RECORDINGS_DIR)
        val recordings = recordingsDir.listFiles()?.filter { it.extension == "mp4" } ?: return
        
        // Sort by modification time (oldest first)
        val sortedRecordings = recordings.sortedBy { it.lastModified() }
        
        // Remove oldest recordings until we have enough space
        for (recording in sortedRecordings) {
            if (hasEnoughStorageSpace()) {
                break
            }
            recording.delete()
            Log.d(TAG, "Deleted old recording: ${recording.name}")
        }
    }

    private fun updateStorageInfo() {
        val recordingsDir = File(context.filesDir, RECORDINGS_DIR)
        val recordings = recordingsDir.listFiles()?.filter { it.extension == "mp4" } ?: emptyList()
        
        val totalSize = recordings.sumOf { it.length() }
        val totalSizeMB = totalSize / (1024 * 1024)
        val maxSizeMB = MAX_STORAGE_USAGE_MB
        
        _storageInfo.value = StorageInfo(
            totalRecordings = recordings.size,
            totalSizeMB = totalSizeMB,
            maxSizeMB = maxSizeMB,
            availableSpaceMB = context.filesDir.freeSpace / (1024 * 1024)
        )
    }

    fun getCurrentRecording(): Recording? = currentRecording

    fun isRecording(): Boolean = isRecording

    fun getRecordingsDirectory(): File = File(context.filesDir, RECORDINGS_DIR)

    fun getThumbnailsDirectory(): File = File(context.filesDir, THUMBNAILS_DIR)

    fun deleteRecording(recording: Recording): Boolean {
        val file = File(recording.filePath)
        val deleted = file.delete()
        if (deleted) {
            updateStorageInfo()
            Log.d(TAG, "Deleted recording: ${recording.fileName}")
        }
        return deleted
    }

    fun getRecordingFile(recording: Recording): File? {
        val file = File(recording.filePath)
        return if (file.exists()) file else null
    }

    fun createThumbnail(recording: Recording): File? {
        try {
            val videoFile = File(recording.filePath)
            if (!videoFile.exists()) {
                Log.w(TAG, "Video file not found: ${recording.filePath}")
                return null
            }

            val thumbnailsDir = getThumbnailsDirectory()
            if (!thumbnailsDir.exists()) {
                thumbnailsDir.mkdirs()
            }

            val thumbnailName = "${recording.fileName.substringBeforeLast(".")}_thumb.jpg"
            val thumbnailFile = File(thumbnailsDir, thumbnailName)

            // Use MediaMetadataRetriever to extract thumbnail
            val retriever = android.media.MediaMetadataRetriever()
            retriever.setDataSource(videoFile.absolutePath)
            
            // Get thumbnail at 1 second into the video
            val bitmap = retriever.frameAtTime(1000000, android.media.MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            retriever.release()

            if (bitmap != null) {
                // Overlay date/time and app name
                val overlayedBitmap = advancedCameraManager.overlayDateTimeAndAppName(
                    bitmap, dateFormat, "WebcamApp"
                )
                // Save bitmap as JPEG
                val outputStream = java.io.FileOutputStream(thumbnailFile)
                overlayedBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream)
                outputStream.close()
                bitmap.recycle()
                overlayedBitmap.recycle()

                Log.d(TAG, "Created thumbnail: ${thumbnailFile.absolutePath}")
                return thumbnailFile
            } else {
                Log.w(TAG, "Failed to extract thumbnail from video")
                return null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating thumbnail", e)
            return null
        }
    }

    suspend fun overlayDateTimeAndAppNameOnVideo(
        inputFile: File,
        outputFile: File,
        dateFormat: String = this.dateFormat,
        appName: String = "WebcamApp"
    ): Boolean {
        // Compose FFmpeg drawtext filter
        val fontSize = 24
        val fontColor = "white"
        val boxColor = "black@0.5"
        val boxBorder = 5
        val x = 20
        val y = "h-line_h-20"
        // FFmpeg date/time: %{localtime:%Y-%m-%d %H\\:%M\\:%S}
        val ffmpegDateFormat = dateFormat
            .replace("yyyy", "%Y")
            .replace("MM", "%m")
            .replace("dd", "%d")
            .replace("HH", "%H")
            .replace("mm", "%M")
            .replace("ss", "%S")
        val drawtext = "drawtext=fontfile=/system/fonts/Roboto-Regular.ttf:text='%{localtime:$ffmpegDateFormat}  |  $appName':fontcolor=$fontColor:fontsize=$fontSize:box=1:boxcolor=$boxColor:boxborderw=$boxBorder:x=$x:y=$y"
        val cmd = "-y -i '${inputFile.absolutePath}' -vf $drawtext -codec:a copy '${outputFile.absolutePath}'"
        val session = FFmpegKit.execute(cmd)
        return ReturnCode.isSuccess(session.returnCode)
    }

    fun setStorageLimitGB(gb: Float?) {
        storageLimitGB = gb
        enforceStorageLimit()
    }
    private fun enforceStorageLimit() {
        val limitBytes = storageLimitGB?.let { (it * 1024 * 1024 * 1024).toLong() }
        if (limitBytes == null) return // unlimited
        val recordingsDir = File(context.filesDir, RECORDINGS_DIR)
        val recordings = recordingsDir.listFiles()?.filter { it.extension == "mp4" } ?: return
        var totalSize = recordings.sumOf { it.length() }
        val sortedRecordings = recordings.sortedBy { it.lastModified() }
        for (file in sortedRecordings) {
            if (totalSize <= limitBytes) break
            totalSize -= file.length()
            file.delete()
            Log.d(TAG, "Deleted old recording (storage limit): ${file.name}")
        }
        updateStorageInfo()
    }

    fun getAllRecordingsGroupedByDay(): Map<String, List<Recording>> {
        val recordingsDir = File(context.filesDir, RECORDINGS_DIR)
        val files = recordingsDir.listFiles()?.filter { it.extension == "mp4" } ?: emptyList()
        val recs = files.mapNotNull { file ->
            // You may want to load from DB if available
            Recording(
                id = file.nameWithoutExtension,
                fileName = file.name,
                filePath = file.absolutePath,
                startTime = file.lastModified(),
                endTime = file.lastModified(),
                duration = 0L, // Could be improved
                fileSize = file.length(),
                type = RecordingType.CONTINUOUS // Could be improved
            )
        }
        return recs.groupBy {
            SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(it.startTime))
        }.toSortedMap(compareByDescending { it })
    }

    fun getRecordingsForDeviceGroupedByDay(deviceId: String): Map<String, List<Recording>> {
        val recordingsDir = File(context.filesDir, RECORDINGS_DIR)
        val files = recordingsDir.listFiles()?.filter { it.extension == "mp4" && it.name.startsWith(deviceId) } ?: emptyList()
        val recs = files.mapNotNull { file ->
            Recording(
                id = file.nameWithoutExtension,
                fileName = file.name,
                filePath = file.absolutePath,
                startTime = file.lastModified(),
                endTime = file.lastModified(),
                duration = 0L,
                fileSize = file.length(),
                type = RecordingType.CONTINUOUS
            )
        }
        return recs.groupBy {
            SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(it.startTime))
        }.toSortedMap(compareByDescending { it })
    }
}

enum class RecordingState {
    IDLE,
    RECORDING,
    PAUSED,
    ERROR
}

data class StorageInfo(
    val totalRecordings: Int = 0,
    val totalSizeMB: Long = 0L,
    val maxSizeMB: Long = 1024L,
    val availableSpaceMB: Long = 0L
) {
    val usagePercentage: Float
        get() = if (maxSizeMB > 0) (totalSizeMB.toFloat() / maxSizeMB) * 100 else 0f
    
    val isStorageFull: Boolean
        get() = totalSizeMB >= maxSizeMB
}