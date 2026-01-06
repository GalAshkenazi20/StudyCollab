package com.example.studycollab.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studycollab.data.model.User
import com.example.studycollab.data.repository.AuthRepository
import com.example.studycollab.utils.UserSession
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    // --- UI STATE ---
    // Using 'private set' ensures only the ViewModel can modify these values
    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Renamed from 'loggedInUser' to 'currentUser' to fix the UI reference error
    var currentUser by mutableStateOf<User?>(null)
        private set

    /**
     * Attempts to log in a user using their email.
     * On success, sets the 'currentUser' session object.
     */
    // Change the login function to accept the password
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Please enter both email and password"
            return
        }

        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            // Pass both email and password to the repository
            val result = repository.login(email, password)
            isLoading = false

            result.onSuccess { user ->
                currentUser = user
                UserSession.UserSession.userId = user._id
                UserSession.UserSession.userName = user.profile.fullName
            }.onFailure {
                errorMessage = "Login failed: ${it.message}"
            }
        }
    }

    /**
     * Clears the session and logs the user out.
     */
    fun logout() {
        currentUser = null
    }
}