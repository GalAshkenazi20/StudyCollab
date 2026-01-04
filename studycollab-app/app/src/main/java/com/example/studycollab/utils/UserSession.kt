package com.example.studycollab.utils

class UserSession {
    object UserSession {
        var userId: String? = null
        var userName: String? = null
        var token: String? = null

        fun isLoggedIn(): Boolean {
            return userId != null
        }

        fun logout() {
            userId = null
            userName = null
            token = null
        }
    }
}