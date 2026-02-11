package com.example.studycollab.ui.submissions

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studycollab.data.model.Submission
import com.example.studycollab.data.remote.ApiService
import kotlinx.coroutines.launch

class SubmissionViewModel(private val apiService: ApiService) : ViewModel() {
    // List of submissions to be displayed in the UI
    var submissions by mutableStateOf<List<Submission>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Fetches submissions using the assignment ID
    fun fetchSubmissions(assignmentId: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = apiService.getSubmissionsForAssignment(assignmentId)
                if (response.isSuccessful) {
                    submissions = response.body() ?: emptyList()
                    errorMessage = null
                } else {
                    errorMessage = "Failed to load submissions"
                }
            } catch (e: Exception) {
                errorMessage = "Network error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    // Sends the grade and feedback to the backend
    fun submitGrade(submissionId: String, grade: String, feedback: String) {
        viewModelScope.launch {
            try {
                val updateData = mapOf("grade" to grade, "feedback" to feedback)
                val response = apiService.updateGrade(submissionId, updateData)
                if (response.isSuccessful) {
                    // Update the local list immediately for a snappy UI
                    submissions = submissions.map {
                        if (it._id == submissionId) it.copy(grade = grade, feedback = feedback) else it
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Failed to submit grade"
            }
        }
    }
}