package com.example.studycollab.data.model

import com.google.gson.annotations.SerializedName

data class GroupAssignmentWork(
    @SerializedName("_id") val id: String,
    val assignmentId: String,
    val groupId: String,
    val subTasks: List<SubTask> = emptyList()
)

data class SubTask(
    @SerializedName("_id") val id: String? = null,
    val title: String,
    // FIXED: Changed from String? to User? to handle the .populate() from backend
    val assignedTo: User? = null,
    val status: String = "todo",
    val completedAt: String? = null,
    // This correctly maps to your User.kt and the populated backend JSON
    val completedBy: User? = null
)