package com.example.studycollab.ui.auth

import android.Manifest
import android.app.Activity
import android.content.Context
import android.location.Geocoder
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.studycollab.ui.Screen
import com.google.android.gms.location.LocationServices
import java.util.*

@Composable
fun SplashScreen(navController: NavController, viewModel: AuthViewModel) {
    val context = LocalContext.current
    val activity = context as? Activity

    // State Management
    var startAnimation by remember { mutableStateOf(false) }
    var isLocationVerified by remember { mutableStateOf(false) }
    var isCheckingLocation by remember { mutableStateOf(true) }
    var showErrorDialog by remember { mutableStateOf<String?>(null) }
    var hasNavigated by remember { mutableStateOf(false) }

    val currentUser = viewModel.currentUser

    // FIX: Separate initMsal (runs once) from navigation check (runs on currentUser change)
    LaunchedEffect(Unit) {
        viewModel.initMsal(context)
    }

    // FIX: Watch for login — navigate only once using hasNavigated guard
    LaunchedEffect(currentUser) {
        if (currentUser != null && !hasNavigated) {
            hasNavigated = true
            navController.navigate("dashboard") {
                popUpTo(Screen.Splash.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    // 1. Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions.values.all { it }
        if (isGranted) {
            checkUserLocation(context,
                onVerified = {
                    isLocationVerified = true
                    isCheckingLocation = false
                },
                onError = { message ->
                    showErrorDialog = message
                    isCheckingLocation = false
                }
            )
        } else {
            showErrorDialog = "Location permission is required to verify your regional access settings."
            isCheckingLocation = false
        }
    }

    // Initial Trigger
    LaunchedEffect(Unit) {
        startAnimation = true
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // Regional Restriction Dialog
    if (showErrorDialog != null) {
        AlertDialog(
            onDismissRequest = { activity?.finish() },
            title = { Text("Access Restricted", fontWeight = FontWeight.Bold) },
            text = { Text(showErrorDialog!!) },
            confirmButton = {
                TextButton(onClick = { activity?.finish() }) {
                    Text("Close App")
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            // Animated Logo/Title
            AnimatedVisibility(
                visible = startAnimation,
                enter = fadeIn(spring()) + scaleIn(initialScale = 0.8f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "StudyCollab",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = (-1).sp
                    )
                    Text(
                        text = "The future of academic teamwork",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))

            // Loading state for location
            if (isCheckingLocation) {
                CircularProgressIndicator(color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Verifying regional access...", color = Color.White.copy(alpha = 0.7f))
            }

            // Animated Buttons Container (Visible only if location is verified)
            AnimatedVisibility(
                visible = isLocationVerified,
                enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn()
            ) {
                Column {
                    Button(
                        onClick = { navController.navigate(Screen.Login.route) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MaterialTheme.colorScheme.primary),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                    ) {
                        Text("Sign in with Credentials", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            if (activity != null) {
                                viewModel.loginWithMicrosoft(activity)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("Microsoft Authenticator", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

/**
 * Helper function to check coordinates and translate to Country Code.
 * Compares the detected physical country with the device's system locale.
 */
private fun checkUserLocation(context: Context, onVerified: () -> Unit, onError: (String) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val deviceCountry = Locale.getDefault().country

    try {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                val detectedCountry = addresses?.firstOrNull()?.countryCode

                if (detectedCountry == deviceCountry) {
                    onVerified()
                } else {
                    onError("Access Restricted: Your physical location ($detectedCountry) must match your device's regional settings ($deviceCountry).")
                }
            } else {
                // If lastLocation is null, proceed for development/demo purposes
                onVerified()
            }
        }.addOnFailureListener {
            onError("Failed to determine location. Please check your GPS settings.")
        }
    } catch (e: SecurityException) {
        onError("Location access error.")
    }
}