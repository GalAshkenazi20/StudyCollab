package com.example.studycollab.data.model

import com.google.gson.annotations.SerializedName

data class Message(
    @SerializedName("_id") val id: String,
    // Changed from groupId to chatRoomId to match ChatRoom architecture
    val chatRoomId: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val timestamp: String
)

data class SendMessageRequest(
    // Changed from groupId to chatRoomId
    val chatRoomId: String,
    val senderId: String,
    val senderName: String,
    val content: String
)

data class ConsultationRoom(
    val chatRoomId: String,
    val groupName: String,
    val type: String,
    val createdAt: String
)