package com.example.studycollab.ui.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studycollab.data.repository.StudyGroupRepository
import kotlinx.coroutines.launch

class StudyGroupViewModel : ViewModel() {
    private val repository = StudyGroupRepository()

    // State for the UI to observe
    var isLoading by mutableStateOf(false)
    var successMessage by mutableStateOf<String?>(null)
    var errorMessage by mutableStateOf<String?>(null)

    fun createGroup(name: String, courseId: String, creatorId: String, purpose: String) {
        isLoading = true
        successMessage = null
        errorMessage = null

        viewModelScope.launch {
            val result = repository.createGroup(name, courseId, creatorId, purpose)
            isLoading = false

            result.onSuccess { group ->
                successMessage = "Group '${group.name}' Created Successfully!"
            }.onFailure {
                errorMessage = "Failed: ${it.message}"
            }
        }
    }
}