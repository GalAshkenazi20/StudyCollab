package com.example.studycollab.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weekly Timetable") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Sunday", style = MaterialTheme.typography.titleLarge)
            Text("• 10:00 - Intro to CS (Lecture)", modifier = Modifier.padding(start = 8.dp))
            Text("• 14:00 - Linear Algebra (Recitation)", modifier = Modifier.padding(start = 8.dp))

            Spacer(modifier = Modifier.height(16.dp))

            Text("Monday", style = MaterialTheme.typography.titleLarge)
            Text("• 09:00 - Algorithms (Lecture)", modifier = Modifier.padding(start = 8.dp))
            Text("• 23:59 - DUE: Algorithms HW1", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(start = 8.dp))
        }
    }
}