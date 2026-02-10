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
    val assignedTo: String?, // User ID
    val status: String = "todo" // 'todo', 'pending_approval', 'completed'
)