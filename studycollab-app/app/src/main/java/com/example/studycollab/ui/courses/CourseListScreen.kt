package com.example.studycollab.ui.courses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.studycollab.data.model.Course
import com.example.studycollab.ui.Screen
import com.example.studycollab.ui.auth.AuthViewModel
import com.example.studycollab.ui.chat.StudyGroupViewModel
import com.example.studycollab.utils.UserSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseListScreen(
    navController: NavController,
    // Using the shared ViewModels to access real user data and course state
    studyViewModel: StudyGroupViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    // 1. GET THE REAL ID: Retrieve the MongoDB _id from the Auth session
//    val userId = authViewModel.currentUser?._id
    val userId = UserSession.UserSession.userId

    // 2. FETCH DATA: Use the real ID instead of a placeholder
    LaunchedEffect(userId) {
        userId?.let {
            studyViewModel.fetchMyCourses(it)
        }
    }

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
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (studyViewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (studyViewModel.myCourses.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (userId == null) "User session not found." else "No courses found for your account.",
                        color = Color.Gray
                    )
                    if (userId != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { studyViewModel.fetchMyCourses(userId) }) {
                            Text("Retry")
                        }
                    }
                }
            } else {
                // 3. DYNAMIC LIST: Display actual courses from MongoDB
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(studyViewModel.myCourses) { course ->
                        CourseItem(course, navController)
                    }
                }
            }
        }
    }
}

@Composable
fun CourseItem(course: Course, navController: NavController) {
    Card(
        onClick = { navController.navigate(Screen.CourseDetail.createRoute(course.name)) },
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Displaying both Name and Code from the updated Course model
            Text(
                text = course.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Code: ${course.code} | ${course.semester}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}