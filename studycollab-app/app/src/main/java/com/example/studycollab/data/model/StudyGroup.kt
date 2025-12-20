package com.example.studycollab.data.model

// Matches the "StudyGroup" collection in MongoDB
data class StudyGroup(
    val _id: String,
    val name: String,
    val courseId: String,
    val purpose: String, // "general", "exam_study", etc.
    val description: String?,
    val members: List<GroupMember>
)

data class GroupMember(
    val userId: String,
    val role: String, // "admin", "student"
    val status: String // "active", "pending"
)

// This helper class is used when SENDING data to create a new group
data class CreateGroupRequest(
    val groupName: String,
    val courseId: String,
    val creatorId: String,
    val purpose: String,
    val description: String
)