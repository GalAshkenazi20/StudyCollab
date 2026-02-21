package com.example.studycollab.data.model

data class NotificationPreferences(
    val groupUpdates: Boolean = true,
    val deadlines: Boolean = true,
    val scheduleChanges: Boolean = true,
    val peerActivity: Boolean = true,
    val officeHours: Boolean = true,
    val deadlineReminderHours: Int = 24
)