package com.webcamapp.mobile.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.webcamapp.mobile.ui.screens.auth.LoginScreen
import com.webcamapp.mobile.ui.screens.auth.RegisterScreen
import com.webcamapp.mobile.ui.screens.camera.CameraScreen
import com.webcamapp.mobile.ui.screens.viewer.ViewerScreen
import com.webcamapp.mobile.ui.screens.role.RoleSelectionScreen
import com.webcamapp.mobile.ui.theme.WebcamAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WebcamAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WebcamApp()
                }
            }
        }
    }
}

@Composable
fun WebcamApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController = navController)
        }
        composable("register") {
            RegisterScreen(navController = navController)
        }
        composable("role_selection") {
            RoleSelectionScreen(navController = navController)
        }
        composable("camera") {
            CameraScreen(navController = navController)
        }
        composable("viewer") {
            ViewerScreen(navController = navController)
        }
    }
}