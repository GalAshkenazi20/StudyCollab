package com.example.studycollab.data.model

import com.google.gson.annotations.SerializedName

data class Message(
    @SerializedName("_id") val id: String,
    val groupId: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val timestamp: String
)


data class SendMessageRequest(
    val groupId: String,
    val senderId: String,
    val senderName: String,
    val content: String
)