package com.example.studycollab.data.model

import com.google.gson.annotations.SerializedName

data class Course(
    @SerializedName("_id")
    val id: String,
    val name: String,
    val code: String,
    val semester: String,
    val lecturerName: String? = null,
    val totalLectures: Int = 13,
    val completedLectures: Int = 0,

    // שומרים על רשימת הנושאים שהיתה לך
    val topics: List<Topic> = emptyList(),

    // --- החדש: מערכת שעות ---
    val schedule: Schedule? = null
)

// המחלקה החדשה שמייצגת את הזמנים (יום, שעה, מיקום)
data class Schedule(
    val day: String,       // e.g., "Sunday"
    val startTime: String, // e.g., "08:00"
    val endTime: String,   // e.g., "11:00"
    val location: String   // e.g., "Building 3, Room 101"
)

// המחלקה הקיימת (לא נגענו בה)
data class Topic(
    val title: String,
    val isCompleted: Boolean
)