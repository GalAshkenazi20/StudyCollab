package com.example.studycollab.ui.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studycollab.data.model.Message
import com.example.studycollab.data.repository.ChatRepository
import com.example.studycollab.utils.UserSession
// מחקנו את השורות של dagger ו-hilt שהיו כאן ועשו בעיות
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

// הסרנו את @HiltViewModel כי אנחנו נעבוד בשיטה פשוטה יותר
class ChatViewModel(
    private val repository: ChatRepository
) : ViewModel() {

    // רשימת ההודעות
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    // הטקסט המוקלד
    private val _messageText = MutableStateFlow("")
    val messageText: StateFlow<String> = _messageText.asStateFlow()

    private var currentGroupId: String? = null

    // התחלת צ'אט: טעינה ורענון אוטומטי
    fun startChat(groupId: String) {
        currentGroupId = groupId
        startPollingMessages()
    }

    fun onMessageChange(text: String) {
        _messageText.value = text
    }

    fun sendMessage() {
        val content = _messageText.value
        val groupId = currentGroupId ?: return
        val currentUser = UserSession

        if (content.isBlank()) return

        viewModelScope.launch {
            _messageText.value = "" // ניקוי מהיר של השדה

            try {
                // תיקון השגיאה: המרה בטוחה למחרוזות (String) כדי שלא יהיה null
                val safeUserId = currentUser.userId.toString()
                val safeUserName = currentUser.userName ?: "Student"

                val result = repository.sendMessage(
                    groupId = groupId,
                    senderId = safeUserId,
                    senderName = safeUserName,
                    content = content
                )

                if (result.isSuccess) {
                    fetchMessages() // רענון מידי
                } else {
                    Log.e("ChatViewModel", "Failed to send message")
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error sending message", e)
            }
        }
    }

    // פונקציה שרצה כל 3 שניות לבדוק הודעות חדשות
    private fun startPollingMessages() {
        viewModelScope.launch {
            while (isActive && currentGroupId != null) {
                fetchMessages()
                delay(3000)
            }
        }
    }

    private suspend fun fetchMessages() {
        val groupId = currentGroupId ?: return
        val result = repository.getMessages(groupId)
        if (result.isSuccess) {
            _messages.value = result.getOrDefault(emptyList())
        }
    }
}