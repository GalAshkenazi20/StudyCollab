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
    fun login(email: String) {
        if (email.isBlank()) {
            errorMessage = "Please enter your university email"
            return
        }

        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            val result = repository.login(email)
            isLoading = false

            result.onSuccess { user ->
                currentUser = user

                // --- הנה התוספת הקריטית! ---
                // אנחנו שומרים את ה-ID בזיכרון הגלובלי כדי ששאר המסכים יוכלו להשתמש בו
                // שים לב: אם במודל שלך זה נקרא id ולא _id, תשנה בהתאם
                UserSession.UserSession.userId = user._id
                UserSession.UserSession.userName = user.profile.fullName
                // ---------------------------

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