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
import androidx.navigation.NavController
import com.example.studycollab.data.model.Course
import com.example.studycollab.ui.chat.StudyGroupViewModel
import com.example.studycollab.utils.UserSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseListScreen(
    navController: NavController,
    // SHARED VIEWMODEL: Removed the '= viewModel()' default to ensure
    // it must be passed from AppNavigation.
    studyViewModel: StudyGroupViewModel
) {
    // 1. FIXED ACCESS: Changed UserSession.UserSession.userId to UserSession.userId
    val userId = UserSession.userId

    // 2. FETCH DATA: Triggered when the screen opens
    LaunchedEffect(userId) {
        userId?.let {
            studyViewModel.fetchMyCourses(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("הקורסים שלי") }, // Switched to Hebrew to match your SRS
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
                        text = if (userId == null) "שגיאה בטעינת משתמש" else "לא נמצאו קורסים המשויכים לחשבון שלך.",
                        color = Color.Gray
                    )
                    if (userId != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { studyViewModel.fetchMyCourses(userId) }) {
                            Text("נסה שנית")
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
        onClick = {
            // Correctly passing both name and code as required by your AppNavigation route
            navController.navigate("course_detail/${course.name}/${course.code}")
        },
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = course.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "קוד: ${course.code} | סמסטר ${course.semester}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}