package com.example.studycollab.ui.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studycollab.data.model.Course
import com.example.studycollab.data.remote.ApiClient
import com.example.studycollab.utils.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CourseViewModel : ViewModel() {
    private val _selectedCourse = MutableStateFlow<Course?>(null)
    val selectedCourse: StateFlow<Course?> = _selectedCourse

    fun loadCourseDetails(courseCode: String) {
        viewModelScope.launch {
            val userId = UserSession.userId ?: return@launch
            val response = ApiClient.apiService.getUserCourses(userId)
            if (response.isSuccessful) {
                // Find the course in the list by code
                _selectedCourse.value = response.body()?.find { it.code == courseCode }
            }
        }
    }
}