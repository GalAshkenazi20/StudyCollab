package com.example.studycollab.ui.chat

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studycollab.data.model.ConsultationRoom
import com.example.studycollab.data.remote.ApiClient
import kotlinx.coroutines.launch
import android.util.Log

class LecturerChatViewModel : ViewModel() {

    // UI State
    val activeConsultations = mutableStateListOf<ConsultationRoom>()
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    /**
     * Fetches all ChatRooms of type 'lecturer_consultation' where this
     * user is assigned as the lecturer.
     */
    fun fetchConsultations(lecturerId: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = ApiClient.apiService.getLecturerConsultations(lecturerId)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!! // We already checked for null
                    Log.d("LecturerChatVM", "Raw Body Size: ${body.size}")
                    Log.d("LecturerChatVM", "Raw Body Content: $body")

                    activeConsultations.clear()
                    activeConsultations.addAll(body) // Use the variable here
                    Log.d("LecturerChatVM", "Loaded ${activeConsultations.size} consultations")
                } else {
                    errorMessage = "Failed to load messages."
                }
            } catch (e: Exception) {
                Log.e("LecturerChatVM", "Error: ${e.message}")
                errorMessage = "Network error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}