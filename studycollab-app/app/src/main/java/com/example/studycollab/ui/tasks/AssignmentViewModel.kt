package com.example.studycollab.ui.tasks

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studycollab.data.model.Assignment
import com.example.studycollab.data.model.GroupAssignmentWork
import com.example.studycollab.data.remote.ApiClient
import com.example.studycollab.utils.UserSession
import kotlinx.coroutines.launch

class AssignmentViewModel : ViewModel() {

    // State for the list of global assignments in a course
    var courseAssignments by mutableStateOf<List<Assignment>>(emptyList())
        private set

    // State for the specific group's task breakdown
    var currentGroupWork by mutableStateOf<GroupAssignmentWork?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    /**
     * Fetch all global assignments created by lecturers for a specific course.
     */
    fun fetchCourseAssignments(courseId: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = ApiClient.apiService.getCourseAssignments(courseId)
                if (response.isSuccessful) {
                    courseAssignments = response.body() ?: emptyList()
                } else {
                    errorMessage = "Failed to load assignments"
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Fetch or initialize the specific sub-tasks for a group related to an assignment.
     */
    fun fetchGroupWork(groupId: String, assignmentId: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = ApiClient.apiService.getGroupWork(groupId, assignmentId)
                if (response.isSuccessful) {
                    currentGroupWork = response.body()
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Add a new sub-task. Only the Group Admin should call this.
     */
    fun addSubTask(workId: String, title: String, assignedToId: String) {
        val adminId = UserSession.userId ?: return
        viewModelScope.launch {
            val taskData = mapOf(
                "title" to title,
                "assignedTo" to assignedToId,
                "adminId" to adminId
            )
            val response = ApiClient.apiService.addSubTask(workId, taskData)
            if (response.isSuccessful) {
                currentGroupWork = response.body()
            }
        }
    }

    /**
     * Mark a task as done. This moves status to 'pending_approval'.
     */
    fun completeTask(workId: String, subTaskId: String) {
        viewModelScope.launch {
            val response = ApiClient.apiService.completeSubTask(workId, subTaskId)
            if (response.isSuccessful) {
                currentGroupWork = response.body()
            }
        }
    }

    /**
     * Approve a completed task. Only the Group Admin should call this.
     */
    fun approveTask(workId: String, subTaskId: String) {
        val adminId = UserSession.userId ?: return
        viewModelScope.launch {
            val adminData = mapOf("adminId" to adminId)
            val response = ApiClient.apiService.approveSubTask(workId, subTaskId, adminData)
            if (response.isSuccessful) {
                currentGroupWork = response.body()
            }
        }
    }
}