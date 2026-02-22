package com.example.studycollab.ui.auth

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studycollab.R
import com.example.studycollab.data.model.User
import com.example.studycollab.data.repository.AuthRepository
import com.example.studycollab.utils.UserSession
import kotlinx.coroutines.launch
import com.microsoft.identity.client.*
import com.microsoft.identity.client.exception.MsalException
import android.util.Log

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()
    private var mSingleAccountApp: ISingleAccountPublicClientApplication? = null

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
     * Initializes the Microsoft SDK. Call this from the Splash Screen.
     */
    fun initMsal(context: Context) {
        Log.d("MSAL_DEBUG", "Initializing MSAL...") // ADD THIS
        PublicClientApplication.createSingleAccountPublicClientApplication(
            context,
            R.raw.auth_config_single_account,
            object : IPublicClientApplication.ISingleAccountApplicationCreatedListener {
                override fun onCreated(application: ISingleAccountPublicClientApplication) {
                    mSingleAccountApp = application
                    Log.d("MSAL_DEBUG", "MSAL initialized successfully!") // ADD THIS
                }

                override fun onError(exception: MsalException) {
                    // CHANGE THIS: Log the full error to Logcat
                    Log.e("MSAL_DEBUG", "MSAL initialization FAILED: ${exception.errorCode}", exception)
                    errorMessage = "MSAL Init Error: ${exception.message}"
                }
            }
        )
    }

    /**
     * Triggers the Microsoft Sign-In UI.
     */
    fun loginWithMicrosoft(activity: Activity) {
        Log.d("MSAL_DEBUG", "Button Clicked!")
        isLoading = true
        errorMessage = null

        if (mSingleAccountApp == null) {
            isLoading = false
            errorMessage = "Microsoft SDK is not ready yet. Please try again in a moment."
            Log.e("MSAL_DEBUG", "mSingleAccountApp is NULL. Check your initMsal or JSON file.")
            return
        }

        Log.d("MSAL_DEBUG", "Starting MSAL Sign-In Flow...")
        mSingleAccountApp?.signIn(activity, null, arrayOf("user.read"), object : AuthenticationCallback {
            override fun onSuccess(authenticationResult: IAuthenticationResult) {
                Log.d("MSAL_DEBUG", "Success! Email: ${authenticationResult.account.username}")
                val email = authenticationResult.account.username
                loginWithMicrosoftEmail(email)
            }

            override fun onError(exception: MsalException) {
                isLoading = false
                errorMessage = "Microsoft Login Failed: ${exception.message}"
                Log.e("MSAL_DEBUG", "MSAL Error: ${exception.message}", exception)
            }

            override fun onCancel() {
                isLoading = false
                Log.d("MSAL_DEBUG", "User cancelled the login.")
            }
        })
    }

    private fun loginWithMicrosoftEmail(email: String) {
        viewModelScope.launch {
            // You will need to add 'loginWithMicrosoft' to your AuthRepository
            val result = repository.loginWithMicrosoft(email)
            isLoading = false

            result.onSuccess { user ->
                currentUser = user
                UserSession.userId = user._id
                UserSession.userName = user.profile.fullName
                UserSession.userRole = user.role
            }.onFailure {
                errorMessage = "Account link failed: ${it.message}"
            }
        }
    }

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
            val result = repository.login(email, password)
            isLoading = false

            result.onSuccess { user ->
                currentUser = user
                UserSession.userId = user._id
                UserSession.userName = user.profile.fullName
                // FIXED: Capture the role ("student" or "lecturer") from the backend
                UserSession.userRole = user.role
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