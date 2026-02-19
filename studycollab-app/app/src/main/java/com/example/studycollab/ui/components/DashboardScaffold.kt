package com.example.studycollab.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studycollab.ui.Screen
import com.example.studycollab.utils.UserSession
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScaffold(
    title: String,
    navController: NavController,
    notificationCount: Int,
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("StudyCollab Menu", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
                HorizontalDivider()

                // Burger Menu Item: Notification Manager
                NavigationDrawerItem(
                    label = { Text("Notification Manager") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.NotificationManager.route)
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) }
                )

                Spacer(modifier = Modifier.weight(1f)) // Push logout to bottom

                NavigationDrawerItem(
                    label = { Text("Logout") },
                    selected = false,
                    onClick = {
                        UserSession.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = null) }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        // FIX: Added Burger Menu icon to the Dashboard TopBar
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Open Menu")
                        }
                    },
                    actions = {
                        // FIX: Re-added the Notification Bell icon with Badge
                        BadgedBox(
                            badge = { if (notificationCount > 0) Badge { Text("$notificationCount") } }
                        ) {
                            IconButton(onClick = { navController.navigate(Screen.Notifications.route) }) {
                                Icon(Icons.Default.Notifications, contentDescription = "View Notifications")
                            }
                        }
                    }
                )
            },
            content = content
        )
    }
}