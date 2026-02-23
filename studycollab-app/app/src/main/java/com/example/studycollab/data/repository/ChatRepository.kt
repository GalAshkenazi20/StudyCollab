package com.example.studycollab.data.repository

import android.util.Log
import com.example.studycollab.data.model.Message
import com.example.studycollab.data.model.SendMessageRequest
import com.example.studycollab.data.remote.ApiService
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ChatRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getMessages(chatRoomId: String): Result<List<Message>> {
        return try {
            val response = apiService.getChatMessages(chatRoomId)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error fetching messages: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("ChatRepo", "Exception fetching messages", e)
            Result.failure(e)
        }
    }

    suspend fun sendMessage(chatRoomId: String, senderId: String, senderName: String, content: String): Result<Message> {
        return try {
            val request = SendMessageRequest(chatRoomId, senderId, senderName, content)
            val response = apiService.sendMessage(request)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error sending message: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("ChatRepo", "Exception sending message", e)
            Result.failure(e)
        }
    }
}