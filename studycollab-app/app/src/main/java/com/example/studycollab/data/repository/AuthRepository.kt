package com.example.studycollab.data.repository

import com.example.studycollab.data.model.User
import com.example.studycollab.data.remote.ApiClient

class AuthRepository {
    private val api = ApiClient.apiService

    suspend fun login(email: String): Result<User> {
        return try {
            // We send the email in a map, matching what your backend expects
            val payload = mapOf("email" to email)
            val user = api.loginUser(payload)
            Result.success(user)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}