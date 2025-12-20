package com.example.studycollab.ui.courses

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studycollab.ui.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseListScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Courses") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            CourseButton("Algorithms 1", navController)
            CourseButton("Linear Algebra", navController)
            CourseButton("Intro to CS", navController)
        }
    }
}

@Composable
fun CourseButton(name: String, navController: NavController) {
    Button(
        onClick = { navController.navigate(Screen.CourseDetail.createRoute(name)) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).height(50.dp)
    ) {
        Text(name)
    }
}