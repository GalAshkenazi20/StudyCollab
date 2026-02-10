package com.example.studycollab.data.model

import com.google.gson.annotations.SerializedName

data class Course(
    @SerializedName("_id")
    val id: String,
    val name: String,
    val code: String,
    val semester: String,

    val totalLectures: Int = 13,
    val completedLectures: Int = 0,
    val topics: List<Topic> = emptyList()
)

data class Topic(
    val title: String,
    val isCompleted: Boolean
)