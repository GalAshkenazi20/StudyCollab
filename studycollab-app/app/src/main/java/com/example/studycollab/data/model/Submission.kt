package com.example.studycollab.data.model

data class Submission(
    val _id: String,
    val assignmentId: String,
    val groupId: GroupInfo, // Populated from backend
    val fileUrl: String,
    val grade: String?,
    val feedback: String?,
    val submittedAt: String
)

data class GroupInfo(
    val _id: String,
    val groupName: String
)

data class SubmissionState(
    val groupName: String,
    val status: String,
    val grade: String?
)