package com.example.studycollab.data.model

data class Notification(
    val _id: String,
    val userId: String,
    val title: String,
    val message: String,
    val relatedId: String?, // ID of the group or course related to the notification
    val createdAt: String
)