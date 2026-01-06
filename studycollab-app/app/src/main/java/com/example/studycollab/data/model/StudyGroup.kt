package com.example.studycollab.data.model

import com.google.gson.JsonElement

// Matches the "StudyGroup" collection in MongoDB
data class StudyGroup(
    val _id: String,
    val name: String,
    val courseId: JsonElement,
    val purpose: String, // "general", "exam_study", etc.
    val description: String?,
    val members: List<GroupMember>
)

data class GroupMember(
    val userId: JsonElement,
    val role: String, // "admin", "student"
    val status: String, // "active", "pending"
    val joinedAt: String? = null
)

// This helper class is used when SENDING data to create a new group
data class CreateGroupRequest(
    val groupName: String,
    val courseId: String,
    val creatorId: String,
    val purpose: String,
    val memberIds: List<String>
)

// Inside StudyGroup.kt or a new Utility file

fun GroupMember.getParticipantId(): String {
    return if (userId.isJsonPrimitive) userId.asString else userId.asJsonObject.get("_id").asString
}

fun GroupMember.getParticipantName(): String {
    return try {
        if (userId.isJsonObject) {
            // Access the nested profile -> fullName
            userId.asJsonObject.getAsJsonObject("profile").get("fullName").asString
        } else {
            "Student ($userId)" // Fallback for old data not yet populated
        }
    } catch (e: Exception) { "Student" }
}

fun StudyGroup.getCourseName(): String {
    return try {
        if (courseId.isJsonObject) courseId.asJsonObject.get("name").asString
        else courseId.asString
    } catch (e: Exception) { "General Course" }
}