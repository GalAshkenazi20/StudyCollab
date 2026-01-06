package com.example.studycollab.ui.notifications

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studycollab.data.model.Notification
import com.example.studycollab.data.remote.ApiClient
import com.example.studycollab.utils.UserSession
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for managing notifications for the logged-in user.
 * It interacts with the ApiService to fetch and delete notifications from MongoDB Atlas.
 */
class NotificationViewModel : ViewModel() {

    // A thread-safe state list that Compose observes for UI updates
    private val _notifications = mutableStateListOf<Notification>()
    val notifications: List<Notification> = _notifications
    val notificationCount: Int get() = _notifications.size

    // UI State management
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    init {
        loadNotifications()
    }

    /**
     * Fetches all notifications for the current user stored in UserSession.
     */
    fun loadNotifications() {
        val userId = UserSession.UserSession.userId
        if (userId == null) {
            errorMessage = "User not logged in"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // Assuming your ApiService has a method getNotifications(userId)
                val response = ApiClient.apiService.getNotifications(userId)
                if (response.isSuccessful && response.body() != null) {
                    _notifications.clear()
                    _notifications.addAll(response.body()!!)
                } else {
                    errorMessage = "Failed to load notifications: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Network error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Deletes a notification from the database and removes it from the UI list.
     * This is triggered when a user clicks a notification in the NotificationScreen.
     */
    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            try {
                // Optimistic UI update: Remove from list immediately
                val indexToRemove = _notifications.indexOfFirst { it._id == notificationId }
                var temporaryNotification: Notification? = null

                if (indexToRemove != -1) {
                    temporaryNotification = _notifications.removeAt(indexToRemove)
                }

                // API call to delete from MongoDB Atlas
                val response = ApiClient.apiService.deleteNotification(notificationId)

                if (!response.isSuccessful) {
                    // Rollback if the server call fails
                    temporaryNotification?.let { _notifications.add(indexToRemove, it) }
                    errorMessage = "Could not delete notification on server"
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.localizedMessage}"
            }
        }
    }

    // Add a public refresh function
    fun refresh() {
        loadNotifications()
    }
}