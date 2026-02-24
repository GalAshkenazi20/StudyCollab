package com.example.studycollab.ui.chat

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studycollab.utils.UserSession
import com.example.studycollab.data.model.*
import com.example.studycollab.data.remote.ApiClient
import com.example.studycollab.data.repository.StudyGroupRepository
import kotlinx.coroutines.launch
import android.util.Log
import com.example.studycollab.ui.Screen

class StudyGroupViewModel(
    private val repository: StudyGroupRepository = StudyGroupRepository(ApiClient.apiService)
) : ViewModel() {

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
        val currentUserId = UserSession.userId
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
                val currentUserId = UserSession.userId
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
        val currentUserId = UserSession.userId

        if (currentUserId == null) {
            errorMessage = "Error: You must be logged in."
            return
        }
        val participantIds = selectedStudents.map { it._id }.toMutableList()
        viewModelScope.launch {
            isLoading = true

            val result = repository.createGroup(groupName, course.id, currentUserId, purpose, participantIds)

            result.onSuccess {
                successMessage = "Group Created!"
                // Reset Fields
                groupName = ""
                selectedCourse = null
                selectedStudents.clear()
                availableClassmates.clear()
                fetchGroups(currentUserId) // Refresh list
            }.onFailure {
                errorMessage = it.message
                println("Error creating group: ${it.message}")
            }
            isLoading = false
        }
    }

    fun deleteGroup(groupId: String, onComplete: (Boolean) -> Unit) {
        val currentUserId = UserSession.userId ?: return

        viewModelScope.launch {
            isLoading = true
            val result = repository.deleteGroup(groupId, currentUserId)

            result.onSuccess {
                myGroups.removeAll { it._id == groupId }
                onComplete(true)
            }.onFailure {
                errorMessage = it.message
                onComplete(false)
            }
            isLoading = false
        }
    }

    private val TAG = "StudyGroupVM_Debug"

    // This is the variable the Screen is observing for navigation
    var consultationTargetRoute by mutableStateOf<String?>(null)

    // com.example.studycollab.ui.chat.StudyGroupViewModel.kt

    fun consultWithLecturer(groupId: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = ApiClient.apiService.openConsultation(groupId)
                Log.d("ConsultDebug", "Full Response: ${response.body()}") // DEBUG: Check all keys

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    val roomId = data["chatRoomId"] ?: data["consultationGroupId"] ?: ""

                    // DEBUG: Try to find why this is null
                    val name = data["lecturerName"] ?: data["subtitle"]
                    Log.d("ConsultDebug", "Extracted Name: $name")

                    val finalName = name ?: "Lecturer"
                    val encodedName = java.net.URLEncoder.encode(finalName, "UTF-8")

                    consultationTargetRoute = "chat/lecturer_consultation/$roomId/$encodedName"
                }
            } catch (e: Exception) {
                Log.e("ConsultDebug", "Error: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    fun openStandardChat(groupId: String) {
        viewModelScope.launch {
            Log.d(TAG, "openStandardChat called for groupId: $groupId")
            isLoading = true
            errorMessage = null
            try {
                val response = ApiClient.apiService.getStandardRoom(groupId)
                Log.d(TAG, "Standard Room Response Code: ${response.code()}")

                if (response.isSuccessful && response.body() != null) {
                    val roomId = response.body()!!["chatRoomId"]
                    Log.d(TAG, "Standard Room ID received: $roomId")

                    // FIX: Using the correct Screen route generator
                    val route = Screen.ChatRoom.createRoute(roomId ?: "")
                    Log.d(TAG, "Navigating to standard chat: $route")

                    consultationTargetRoute = route
                } else {
                    Log.e(TAG, "Failed to load standard room: ${response.code()}")
                    errorMessage = "Failed to load chat room."
                }
            } catch (e: Exception) {
                Log.e(TAG, "Network error in openStandardChat", e)
                errorMessage = "Network error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun openPeerConsultation(myGroupId: String, targetGroupId: String, targetGroupName: String) {
        Log.d("PeerNav", "1. Button Clicked! MyGroup: $myGroupId, Target: $targetGroupId")
        viewModelScope.launch {
            isLoading = true
            try {
                val response = ApiClient.apiService.openPeerConsultation(myGroupId, targetGroupId)
                Log.d("PeerNav", "2. Backend Response Code: ${response.code()}")

                if (response.isSuccessful && response.body() != null) {
                    val roomId = response.body()!!["chatRoomId"]
                    Log.d("PeerNav", "3. Room Resolved: $roomId")

                    // Triggers the LaunchedEffect in the UI
                    consultationTargetRoute = "chat/peer_consultation/$roomId/$targetGroupName"
                    Log.d("PeerNav", "4. consultationTargetRoute set to: $consultationTargetRoute")
                } else {
                    Log.e("PeerNav", "Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("PeerNav", "5. Network Crash", e)
                errorMessage = "Could not open peer chat."
            } finally {
                isLoading = false
            }
        }
    }

    var coursePeerGroups = mutableStateListOf<StudyGroup>()
        private set

    fun fetchGroupsByCourse(courseId: String) {
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.getGroupsByCourse(courseId)
                if (response.isSuccessful && response.body() != null) {
                    coursePeerGroups.clear()
                    coursePeerGroups.addAll(response.body()!!)
                    Log.d("PeerDebug", "Fetched ${coursePeerGroups.size} groups for course $courseId")
                }
            } catch (e: Exception) {
                Log.e("PeerDebug", "Network error: ${e.message}")
            }
        }
    }

    fun clearConsultationRoute() {
        consultationTargetRoute = null
    }
}