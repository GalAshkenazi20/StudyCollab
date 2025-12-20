package com.example.studycollab.data.model

// This matches your MongoDB "users" collection
data class User(
    val _id: String,  // MongoDB ObjectId
    val role: String, // "student", "lecturer"
    val profile: UserProfile,
    val university: UniversityDetails
)

data class UserProfile(
    val fullName: String,
    val avatarUrl: String?
)

data class UniversityDetails(
    val email: String,
    val externalUserId: String
)