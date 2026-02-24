package com.example.studycollab.services

import android.util.Log
import com.example.studycollab.data.remote.ApiClient
import com.example.studycollab.utils.UserSession
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object FCMTokenManager {
    private const val TAG = "FCMTokenManager"

    /**
     * Call this right after successful login to register the device for push.
     */
    fun registerAfterLogin() {
        val userId = UserSession.userId ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                Log.d(TAG, "FCM Token: $token")

                ApiClient.apiService.registerFcmToken(
                    mapOf("userId" to userId, "fcmToken" to token)
                )
                Log.d(TAG, "Token registered with backend")
            } catch (e: Exception) {
                Log.e(TAG, "Token registration failed: ${e.message}")
            }
        }
    }
}