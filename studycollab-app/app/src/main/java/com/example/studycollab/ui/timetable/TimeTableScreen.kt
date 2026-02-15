package com.example.studycollab.ui.timetable // שים לב לחבילה הנכונה

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.studycollab.data.model.Course
import com.example.studycollab.ui.courses.CourseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeTableScreen(
    navController: NavController, // החזרנו את ה-navController
    courseViewModel: CourseViewModel = viewModel()
) {
    // 1. שליפת הקורסים
    val courses by courseViewModel.myCourses.collectAsState()

    // 2. לוגיקת מיון ימים
    val daysOrder = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
    val scheduleMap = remember(courses) {
        courses.filter { it.schedule != null }
            .groupBy { it.schedule!!.day }
            .toSortedMap(compareBy { daysOrder.indexOf(it) })
    }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5)) // רקע אפור בהיר
                .padding(16.dp)
        ) {
            if (scheduleMap.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No classes scheduled yet", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    scheduleMap.forEach { (day, dailyCourses) ->
                        item { DayHeader(day) }

                        val sortedCourses = dailyCourses.sortedBy { it.schedule?.startTime }
                        items(sortedCourses) { course ->
                            CourseScheduleCard(course)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DayHeader(day: String) {
    Text(
        text = day,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun CourseScheduleCard(course: Course) {
    val schedule = course.schedule!!
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(60.dp)
            ) {
                Text(text = schedule.startTime, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "|", color = Color.Gray, modifier = Modifier.padding(vertical = 2.dp))
                Text(text = schedule.endTime, color = Color.Gray, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = course.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "${course.code} • ${schedule.location}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        }
    }
}