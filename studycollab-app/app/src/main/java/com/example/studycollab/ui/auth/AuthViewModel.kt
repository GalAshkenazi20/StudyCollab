package com.example.studycollab.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studycollab.data.model.User
import com.example.studycollab.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    // State variables that the UI listens to
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var loggedInUser by mutableStateOf<User?>(null)

    fun login(email: String) {
        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            val result = repository.login(email)
            isLoading = false

            result.onSuccess { user ->
                loggedInUser = user
            }.onFailure {
                errorMessage = "Login failed: ${it.message}"
            }
        }
    }
}