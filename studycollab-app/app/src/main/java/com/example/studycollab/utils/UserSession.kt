package com.example.studycollab.utils

object UserSession {
    var userId: String? = null
    var userName: String? = null
    var userRole: String? = null // ADDED: To distinguish between "student" and "lecturer"
    var token: String? = null

    fun isLoggedIn(): Boolean {
        return userId != null
    }

    fun logout() {
        userId = null
        userName = null
        userRole = null
        token = null
    }
}