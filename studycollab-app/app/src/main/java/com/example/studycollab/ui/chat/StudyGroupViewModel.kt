package com.example.studycollab.ui.chat

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studycollab.utils.UserSession
import com.example.studycollab.data.model.*
import com.example.studycollab.data.remote.ApiClient
import com.example.studycollab.data.repository.StudyGroupRepository
import kotlinx.coroutines.launch

class StudyGroupViewModel : ViewModel() {
    // Initialize repository
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

    fun loadInitialData() {
        val currentUserId = UserSession.UserSession.userId
        if (currentUserId != null) {
            fetchMyCourses(currentUserId)
            fetchGroups(currentUserId)
        } else {
            errorMessage = "Please login to see your groups"
        }
    }

    fun fetchGroups(userId: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                val groups = repository.getGroupsForUser(userId)
                myGroups.clear()
                myGroups.addAll(groups)
            } catch (e: Exception) {
                errorMessage = "Failed to fetch groups: ${e.message}"
            }
            isLoading = false
        }
    }

    fun fetchMyCourses(userId: String) {
        viewModelScope.launch {
            try {
                val courses = repository.getUserCourses(userId)
                myCourses.clear()
                myCourses.addAll(courses)
            } catch (e: Exception) {
                errorMessage = "Failed to fetch courses"
            }
        }
    }

    fun onCourseSelected(course: Course) {
        selectedCourse = course
        selectedStudents.clear()
        viewModelScope.launch {
            try {

                val classmates = repository.getStudentsInCourse(course.id)

                val currentUserId = UserSession.UserSession.userId
                val filteredList = classmates.filter { student ->
                    student._id != currentUserId
                }

                availableClassmates.clear()
                availableClassmates.addAll(filteredList)
            } catch (e: Exception) {
                errorMessage = "Failed to load classmates"
            }
        }
    }

    fun toggleStudentSelection(student: User) {
        if (selectedStudents.contains(student)) selectedStudents.remove(student)
        else selectedStudents.add(student)
    }

    fun createGroup() {
        val course = selectedCourse ?: return

        val currentUserId = UserSession.UserSession.userId

        if (currentUserId == null) {
            errorMessage = "Error: You must be logged in."
            return
        }

        viewModelScope.launch {
            isLoading = true

            val result = repository.createGroup(groupName, course.id, currentUserId, purpose)

            result.onSuccess {
                successMessage = "Group Created!"
                groupName = ""
                selectedCourse = null
                selectedStudents.clear()
                availableClassmates.clear()
                fetchGroups(currentUserId)
            }.onFailure {
                errorMessage = it.message
                println("Error creating group: ${it.message}")
            }
            isLoading = false
        }
    }
}