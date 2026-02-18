package com.example.studycollab.ui.scheduler

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studycollab.data.model.LecturerInfo
import com.example.studycollab.data.remote.ApiClient
import com.example.studycollab.utils.UserSession
import kotlinx.coroutines.launch

class OfficeHoursViewModel : ViewModel() {

    var slots by mutableStateOf<List<SlotInfo>>(emptyList())
        private set

    var lecturers by mutableStateOf<List<LecturerInfo>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // --- Lecturer Functions ---

    // Fetch all slots for the logged-in lecturer
    fun fetchMySlots() {
        val lecturerId = UserSession.userId ?: return
        fetchSlots(lecturerId)
    }

    // Fetch slots for any lecturer (used by student too)
    fun fetchSlots(lecturerId: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = ApiClient.apiService.getOfficeHours(lecturerId)
                if (response.isSuccessful) {
                    slots = response.body() ?: emptyList()
                    errorMessage = null
                }
            } catch (e: Exception) {
                errorMessage = "Failed to load slots"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    // Create a new slot
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
                val response = ApiClient.apiService.createOfficeHour(newSlot)
                if (response.isSuccessful) {
                    fetchMySlots() // Refresh
                }
            } catch (e: Exception) {
                errorMessage = "Error creating slot"
                e.printStackTrace()
            }
        }
    }

    // Delete a slot
    fun deleteSlot(slotId: String) {
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.deleteOfficeHour(slotId)
                if (response.isSuccessful) {
                    slots = slots.filter { it.id != slotId }
                }
            } catch (e: Exception) {
                errorMessage = "Delete failed"
                e.printStackTrace()
            }
        }
    }

    // --- Student Functions ---

    // Fetch list of all lecturers
    fun fetchLecturers() {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = ApiClient.apiService.getAllLecturers()
                if (response.isSuccessful) {
                    lecturers = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                errorMessage = "Failed to load lecturers"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    // Fetch only available (unbooked) slots for a lecturer
    fun fetchAvailableSlots(lecturerId: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = ApiClient.apiService.getAvailableOfficeHours(lecturerId)
                if (response.isSuccessful) {
                    slots = response.body() ?: emptyList()
                    errorMessage = null
                }
            } catch (e: Exception) {
                errorMessage = "Failed to load available slots"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    // Book a slot
    fun bookSlot(slotId: String) {
        val studentId = UserSession.userId ?: return
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.bookOfficeHour(
                    slotId,
                    mapOf("studentId" to studentId)
                )
                if (response.isSuccessful) {
                    // Remove booked slot from the list
                    slots = slots.filter { it.id != slotId }
                }
            } catch (e: Exception) {
                errorMessage = "Booking failed"
                e.printStackTrace()
            }
        }
    }
}