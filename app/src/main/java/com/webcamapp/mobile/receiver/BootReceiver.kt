package com.webcamapp.mobile.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.webcamapp.mobile.data.local.UserPreferences
import com.webcamapp.mobile.service.CameraService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var userPreferences: UserPreferences

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_QUICKBOOT_POWERON -> {
                Log.d(TAG, "Device boot completed")
                handleBootCompleted(context)
            }
        }
    }

    private fun handleBootCompleted(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Check if auto-start is enabled
                val autoStartEnabled = userPreferences.autoStartEnabled.first()
                
                if (autoStartEnabled) {
                    Log.d(TAG, "Auto-start is enabled, starting camera service")
                    
                    // Check if user has selected camera role
                    val userRole = userPreferences.userRole.first()
                    if (userRole == "CAMERA") {
                        startCameraService(context)
                    } else {
                        Log.d(TAG, "User role is not camera, skipping auto-start")
                    }
                } else {
                    Log.d(TAG, "Auto-start is disabled")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling boot completion", e)
            }
        }
    }

    private fun startCameraService(context: Context) {
        try {
            val serviceIntent = Intent(context, CameraService::class.java).apply {
                action = CameraService.ACTION_START_CAMERA
            }
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            
            Log.d(TAG, "Camera service started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting camera service", e)
        }
    }
}