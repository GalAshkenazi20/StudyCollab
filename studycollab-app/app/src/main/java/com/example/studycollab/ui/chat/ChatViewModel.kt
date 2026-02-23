package com.example.studycollab.ui.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studycollab.data.model.Message
import com.example.studycollab.data.repository.ChatRepository
import com.example.studycollab.utils.UserSession
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _messageText = MutableStateFlow("")
    val messageText: StateFlow<String> = _messageText.asStateFlow()

    // This is the source of truth for the room session
    private var currentRoomId: String? = null

    /**
     * @param chatRoomId The unique ObjectId of the room (Group, Consultation, or Forum)
     * @param isConsultation if true, indicates we are in consultation mode
     */
    fun startChat(chatRoomId: String, isConsultation: Boolean = false, poll: Boolean = true) {

        this.currentRoomId = chatRoomId

        if (poll) {
            startPollingMessages()
        }
    }

    fun onMessageChange(text: String) {
        _messageText.value = text
    }

    fun sendMessage() {
        val content = _messageText.value
        val roomId = currentRoomId ?: return // Points to consultation room if in that mode
        val currentUser = UserSession

        if (content.isBlank()) return

        viewModelScope.launch {
            _messageText.value = ""
            try {
                val safeUserId = currentUser.userId.toString()
                val safeUserName = currentUser.userName ?: "Student"

                // Sends to the new prefixed room ID
                val result = repository.sendMessage(
                    chatRoomId = roomId,
                    senderId = safeUserId,
                    senderName = safeUserName,
                    content = content
                )

                if (result.isSuccess) {
                    fetchMessages()
                } else {
                    Log.e("ChatViewModel", "Failed to send message to $roomId")
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error sending message", e)
            }
        }
    }

    private fun startPollingMessages() {
        viewModelScope.launch {
            while (isActive && currentRoomId != null) {
                fetchMessages()
                delay(3000)
            }
        }
    }

    private suspend fun fetchMessages() {
        val roomId = currentRoomId ?: return
        val result = repository.getMessages(roomId)
        if (result.isSuccess) {
            _messages.value = result.getOrDefault(emptyList())
        }
    }
}