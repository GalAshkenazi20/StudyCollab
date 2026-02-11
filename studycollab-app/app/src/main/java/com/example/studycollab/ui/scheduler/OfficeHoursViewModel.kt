package com.example.studycollab.ui.scheduler

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studycollab.data.remote.ApiService
import com.example.studycollab.utils.UserSession
import kotlinx.coroutines.launch

class OfficeHoursViewModel(private val apiService: ApiService) : ViewModel() {

    var slots by mutableStateOf<List<SlotInfo>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // 1. Fetch all slots for a specific lecturer
    fun fetchSlots(lecturerId: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = apiService.getOfficeHours(lecturerId)
                if (response.isSuccessful) {
                    slots = response.body() ?: emptyList()
                    errorMessage = null
                }
            } catch (e: Exception) {
                errorMessage = "Failed to load slots"
            } finally {
                isLoading = false
            }
        }
    }

    // 2. Create a new slot (Lecturer only)
    fun createSlot(day: String, start: String, end: String) {
        val lecturerId = UserSession.userId ?: return
        viewModelScope.launch {
            try {
                val newSlot = mapOf(
                    "lecturerId" to lecturerId,
                    "dayOfWeek" to day,
                    "startTime" to start,
                    "endTime" to end
                )
                val response = apiService.createOfficeHour(newSlot)
                if (response.isSuccessful) {
                    // Refresh the list locally
                    fetchSlots(lecturerId)
                }
            } catch (e: Exception) {
                errorMessage = "Error creating slot"
            }
        }
    }

    // 3. Book a slot (Student only)
    fun bookSlot(slotId: String) {
        val studentId = UserSession.userId ?: return
        viewModelScope.launch {
            try {
                val response = apiService.bookOfficeHour(slotId, mapOf("studentId" to studentId))
                if (response.isSuccessful) {
                    // Remove or update the slot in the UI
                    slots = slots.filter { it.id != slotId }
                }
            } catch (e: Exception) {
                errorMessage = "Booking failed"
            }
        }
    }

    // 4. Delete a slot (Lecturer only)
    fun deleteSlot(slotId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.deleteOfficeHour(slotId)
                if (response.isSuccessful) {
                    slots = slots.filter { it.id != slotId }
                }
            } catch (e: Exception) {
                errorMessage = "Delete failed"
            }
        }
    }
}