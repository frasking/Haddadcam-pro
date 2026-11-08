package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AboutDesignerScreen
import com.example.ui.screens.AddEditCameraScreen
import com.example.ui.screens.CameraDetailScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EventLogScreen
import com.example.ui.screens.GalleryScreen
import com.example.ui.screens.SecurityShieldScreen
import com.example.ui.theme.AlertRed
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SurveillanceBgDark
import com.example.ui.theme.SurveillanceSurfaceDark
import com.example.ui.theme.SyrianGreenLight
import com.example.ui.theme.SyrianGreenPrimary
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.SurveillanceViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Ensure RTL layout direction for Arabic experience
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    HaddadCamProApp()
                }
            }
        }
    }
}

@Composable
fun HaddadCamProApp(
    viewModel: SurveillanceViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val events by viewModel.events.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        val msg = snackbarMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Hide bottom bar on focus / edit screens
            if (currentScreen != ScreenDestination.CAMERA_DETAIL && currentScreen != ScreenDestination.ADD_EDIT_CAMERA) {
                NavigationBar(
                    containerColor = SurveillanceSurfaceDark,
                    contentColor = SyrianGreenLight
                ) {
                    // Dashboard Navigation
                    NavigationBarItem(
                        selected = currentScreen == ScreenDestination.DASHBOARD,
                        onClick = { viewModel.navigateTo(ScreenDestination.DASHBOARD) },
                        icon = { Icon(Icons.Default.GridView, contentDescription = "Dashboard") },
                        label = { Text("المراقبة", fontSize = 11.sp) },
                        colors = navigationItemColors(),
                        modifier = Modifier.testTag("nav_dashboard")
                    )

                    // Event Log Navigation
                    NavigationBarItem(
                        selected = currentScreen == ScreenDestination.EVENT_LOGS,
                        onClick = { viewModel.navigateTo(ScreenDestination.EVENT_LOGS) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (events.isNotEmpty()) {
                                        Badge(containerColor = AlertRed, contentColor = Color.White) {
                                            Text("${events.size}")
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = "Events")
                            }
                        },
                        label = { Text("التنبيهات", fontSize = 11.sp) },
                        colors = navigationItemColors(),
                        modifier = Modifier.testTag("nav_events")
                    )

                    // Media Gallery Navigation
                    NavigationBarItem(
                        selected = currentScreen == ScreenDestination.MEDIA_GALLERY,
                        onClick = { viewModel.navigateTo(ScreenDestination.MEDIA_GALLERY) },
                        icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery") },
                        label = { Text("المعرض", fontSize = 11.sp) },
                        colors = navigationItemColors(),
                        modifier = Modifier.testTag("nav_gallery")
                    )

                    // Security Shield Navigation
                    NavigationBarItem(
                        selected = currentScreen == ScreenDestination.SECURITY_SHIELD,
                        onClick = { viewModel.navigateTo(ScreenDestination.SECURITY_SHIELD) },
                        icon = { Icon(Icons.Default.Security, contentDescription = "Security Shield") },
                        label = { Text("التشفير", fontSize = 11.sp) },
                        colors = navigationItemColors(),
                        modifier = Modifier.testTag("nav_shield")
                    )

                    // About Designer Screen Navigation
                    NavigationBarItem(
                        selected = currentScreen == ScreenDestination.ABOUT_DESIGNER,
                        onClick = { viewModel.navigateTo(ScreenDestination.ABOUT_DESIGNER) },
                        icon = { Icon(Icons.Default.Info, contentDescription = "About Designer") },
                        label = { Text("المصمم", fontSize = 11.sp) },
                        colors = navigationItemColors(),
                        modifier = Modifier.testTag("nav_about")
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = SurveillanceBgDark
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                ScreenDestination.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                ScreenDestination.CAMERA_DETAIL -> CameraDetailScreen(viewModel = viewModel)
                ScreenDestination.ADD_EDIT_CAMERA -> AddEditCameraScreen(viewModel = viewModel)
                ScreenDestination.EVENT_LOGS -> EventLogScreen(viewModel = viewModel)
                ScreenDestination.MEDIA_GALLERY -> GalleryScreen(viewModel = viewModel)
                ScreenDestination.SECURITY_SHIELD -> SecurityShieldScreen(viewModel = viewModel)
                ScreenDestination.ABOUT_DESIGNER -> AboutDesignerScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun navigationItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = SyrianGreenLight,
    selectedTextColor = SyrianGreenLight,
    unselectedIconColor = Color(0xFF7E9387),
    unselectedTextColor = Color(0xFF7E9387),
    indicatorColor = Color(0xFF14241B)
)
