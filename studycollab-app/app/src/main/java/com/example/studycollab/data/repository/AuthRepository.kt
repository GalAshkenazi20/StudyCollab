package com.example.studycollab.data.repository

import com.example.studycollab.data.model.User
import com.example.studycollab.data.remote.ApiClient
import com.example.studycollab.data.remote.ApiClient.apiService

class AuthRepository {
    private val api = ApiClient.apiService

    // Update signature to include password
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            // Add password to the payload map
            val payload = mapOf(
                "email" to email,
                "password" to password
            )
            val user = api.loginUser(payload)
            Result.success(user)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun loginWithMicrosoft(email: String): Result<User> {
        return try {
            // Assume your ApiService has a microsoftLogin(@Body request: LoginRequest)
            val response = apiService.microsoftLogin(mapOf("email" to email))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Could not find matching mock user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

