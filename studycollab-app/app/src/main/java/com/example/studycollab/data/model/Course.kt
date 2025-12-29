package com.example.studycollab.data.model

import com.google.gson.annotations.SerializedName

data class Course(
    @SerializedName("_id") // This maps the MongoDB "_id" to "id" in Kotlin
    val id: String,
    val name: String,
    val code: String,      // Added to match DB
    val semester: String   // Added to match DB
)