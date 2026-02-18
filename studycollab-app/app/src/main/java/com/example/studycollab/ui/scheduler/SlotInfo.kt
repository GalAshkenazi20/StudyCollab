package com.example.studycollab.ui.scheduler

import com.google.gson.annotations.SerializedName

data class SlotInfo(
    @SerializedName("_id")
    val id: String,
    @SerializedName("dayOfWeek")
    val day: String,
    @SerializedName("startTime")
    val start: String,
    @SerializedName("endTime")
    val end: String,
    val isBooked: Boolean = false,
    val bookedBy: BookedByInfo? = null,
    val lecturerId: String? = null,
    val meetingLink: String? = null
)

data class BookedByInfo(
    @SerializedName("_id")
    val id: String? = null,
    val profile: ProfileInfo? = null
)

data class ProfileInfo(
    val fullName: String? = null
)