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
import android.util.Log
import com.example.studycollab.data.remote.ApiService

class AssignmentViewModel(
    private val apiService: ApiService = ApiClient.apiService
) : ViewModel() {

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
    fun addSubTask(workId: String, title: String, assignedToId: String, groupId: String, assignmentId: String) {
        val adminId = UserSession.userId ?: return
        Log.d("TaskDebug", "Starting addSubTask: workId=$workId, title=$title, group=$groupId, adminId=$adminId")

        viewModelScope.launch {
            isLoading = true
            try {
                val taskData = mapOf(
                    "title" to title,
                    "assignedTo" to assignedToId,
                    "adminId" to adminId,
                    "groupId" to groupId,
                    "assignmentId" to assignmentId
                )

                val targetId = workId.ifBlank { "new" }
                Log.d("TaskDebug", "Request Body: $taskData")
                Log.d("TaskDebug", "Sending POST to api/group-work/$targetId/subtasks")

                val response = ApiClient.apiService.addSubTask(targetId, taskData)

                Log.d("TaskDebug", "Response Code: ${response.code()}")
                Log.d("TaskDebug", "Response Success: ${response.isSuccessful}")
                Log.d("TaskDebug", "Response Body: ${response.body()}")
                Log.d("TaskDebug", "Response Error: ${response.errorBody()?.string()}")

                if (response.isSuccessful && response.body() != null) {
                    Log.d("TaskDebug", "SUCCESS: Task saved. New GroupWork state: ${response.body()}")
                    currentGroupWork = response.body()
                    errorMessage = null
                } else {
                    val errorBody = response.errorBody()?.string() ?: "No error body"
                    val errorMsg = "API Error: ${response.code()} - $errorBody"
                    Log.e("TaskDebug", errorMsg)
                    errorMessage = errorMsg
                }
            } catch (e: Exception) {
                Log.e("TaskDebug", "Network Exception: ${e.message}", e)
                Log.e("TaskDebug", "Stack trace:", e)
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }


    /**
     * Mark a task as done. Now sends the userId to the backend for attribution.
     */
    fun completeTask(workId: String, subTaskId: String) {
        val userId = UserSession.userId ?: return
        viewModelScope.launch {
            // Passing userId in the body so the backend can record who finished it
            val requestBody = mapOf("completedBy" to userId)
            val response = ApiClient.apiService.completeSubTask(workId, subTaskId, requestBody)
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

    /**
     * Delete a sub-task. Only the Group Admin should call this.
     */
    fun deleteSubTask(workId: String, subTaskId: String) {
        val adminId = UserSession.userId ?: return
        Log.d("TaskDebug", "Deleting task: workId=$workId, subTaskId=$subTaskId")

        viewModelScope.launch {
            isLoading = true
            try {
                // Assuming your ApiService has this delete method
                val response = ApiClient.apiService.deleteSubTask(workId, subTaskId)
                if (response.isSuccessful) {
                    currentGroupWork = response.body()
                    errorMessage = null
                } else {
                    errorMessage = "Failed to delete task: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
}
    }