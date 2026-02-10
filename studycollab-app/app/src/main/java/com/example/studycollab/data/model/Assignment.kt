package com.example.studycollab.data.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Assignment(
    @SerializedName("_id") val id: String,
    val courseId: String,
    val title: String,
    val description: String?,
    val fileUrl: String?,
    val dueAt: Date,
    val createdBy: String?
)