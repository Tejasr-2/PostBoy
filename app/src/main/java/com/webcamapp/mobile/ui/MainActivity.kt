package com.webcamapp.mobile.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
<<<<<<< HEAD
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
=======
>>>>>>> origin/main
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
import com.webcamapp.mobile.ui.screens.settings.AdvancedSettingsScreen
import com.webcamapp.mobile.ui.theme.WebcamAppTheme
import dagger.hilt.android.AndroidEntryPoint
<<<<<<< HEAD
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.Icon
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.padding
import androidx.compose.foundation.currentBackStackEntryAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.navigate
import androidx.compose.material3.popUpTo
import androidx.compose.material3.launchSingleTop
import androidx.compose.material3.restoreState
import androidx.compose.material3.graph

data class NavItem(val route: String, val icon: ImageVector, val label: String)
=======
>>>>>>> origin/main

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
<<<<<<< HEAD
    val navItems = listOf(
        NavItem("home", Icons.Default.Home, "Home"),
        NavItem("recordings", Icons.Default.VideoLibrary, "Recordings"),
        NavItem("devices", Icons.Default.Devices, "Devices"),
        NavItem("settings", Icons.Default.Settings, "Settings")
    )
    Scaffold(
        bottomBar = {
            NavigationBar {
                val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
                navItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute != item.route) navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { CameraScreen(navController) }
            composable("recordings") { ViewerScreen(navController) }
            composable("devices") { RoleSelectionScreen(navController) }
            composable("settings") { AdvancedSettingsScreen(navController) }
=======

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
        composable("advanced_settings") {
            AdvancedSettingsScreen(navController = navController)
>>>>>>> origin/main
        }
    }
}