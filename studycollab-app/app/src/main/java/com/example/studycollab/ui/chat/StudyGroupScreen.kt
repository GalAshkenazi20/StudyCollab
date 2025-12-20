package com.example.studycollab.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studycollab.ui.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyGroupScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Study Groups") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { // BACK BUTTON
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.CreateStudyGroup.route) }) {
                Icon(Icons.Default.Add, "Create Group")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            // Placeholder List
            Text("Active Groups:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(10.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Text("Calculus 1 - Exam Prep", modifier = Modifier.padding(16.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Text("Physics Lab Team", modifier = Modifier.padding(16.dp))
            }
        }
    }
}