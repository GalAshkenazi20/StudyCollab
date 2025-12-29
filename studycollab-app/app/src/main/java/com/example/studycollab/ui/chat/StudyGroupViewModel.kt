package com.example.studycollab.ui.chat

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studycollab.data.model.*
import com.example.studycollab.data.remote.ApiClient
import com.example.studycollab.data.repository.StudyGroupRepository
import kotlinx.coroutines.launch

class StudyGroupViewModel : ViewModel() {
    // Initialize repository with the ApiService from your ApiClient
    private val repository = StudyGroupRepository(ApiClient.apiService)

    // UI States
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    // Data Lists
    val myGroups = mutableStateListOf<StudyGroup>()
    val myCourses = mutableStateListOf<Course>()
    val availableClassmates = mutableStateListOf<User>()

    // Form Inputs
    var groupName by mutableStateOf("")
    var purpose by mutableStateOf("general")
    var selectedCourse by mutableStateOf<Course?>(null)
    val selectedStudents = mutableStateListOf<User>()

    fun fetchGroups(userId: String) {
        viewModelScope.launch {
            isLoading = true
            val groups = repository.getGroupsForUser(userId)
            myGroups.clear()
            myGroups.addAll(groups)
            isLoading = false
        }
    }

    fun fetchMyCourses(userId: String) {
        viewModelScope.launch {
            val courses = repository.getUserCourses(userId)
            myCourses.clear()
            myCourses.addAll(courses)
        }
    }

    fun onCourseSelected(course: Course) {
        selectedCourse = course
        selectedStudents.clear()
        viewModelScope.launch {
            val classmates = repository.getStudentsInCourse(course.id)
            availableClassmates.clear()
            availableClassmates.addAll(classmates)
        }
    }

    fun toggleStudentSelection(student: User) {
        if (selectedStudents.contains(student)) selectedStudents.remove(student)
        else selectedStudents.add(student)
    }

    fun createGroup() {
        val course = selectedCourse ?: return
        viewModelScope.launch {
            isLoading = true
            val result = repository.createGroup(groupName, course.id, "USER_ID", purpose)
            result.onSuccess {
                successMessage = "Group Created!"
                fetchGroups("USER_ID")
            }.onFailure { errorMessage = it.message }
            isLoading = false
        }
    }
}