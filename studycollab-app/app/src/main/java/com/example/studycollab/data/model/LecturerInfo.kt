package com.example.studycollab.data.model

import com.google.gson.annotations.SerializedName

data class LecturerInfo(
    @SerializedName("_id")
    val id: String,
    val profile: LecturerProfile
)

data class LecturerProfile(
    val fullName: String
)