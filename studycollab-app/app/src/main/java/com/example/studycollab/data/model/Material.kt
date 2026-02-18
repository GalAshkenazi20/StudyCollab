package com.example.studycollab.data.model

import com.google.gson.annotations.SerializedName

data class Material(
    @SerializedName("_id")
    val id: String,
    val courseId: String,
    val title: String,
    val fileUrl: String,
    val uploadedBy: String? = null,
    val createdAt: String? = null
)