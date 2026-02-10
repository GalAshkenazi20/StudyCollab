package com.example.studycollab.ui.chat

import MainDispatcherRule
import com.example.studycollab.data.model.Message
import com.example.studycollab.data.repository.ChatRepository
import com.example.studycollab.utils.UserSession
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

class ChatViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `test sendMessage clears field and calls repo`() = runTest {
        // 1. Arrange
        val mockRepo = mock<ChatRepository>()
        val viewModel = ChatViewModel(mockRepo)

        UserSession.userId = "user_1"
        UserSession.userName = "Elad"

        // ודא שעדכנת את ChatViewModel להוסיף את הפרמטר poll (כפי שהוסבר קודם)
        viewModel.startChat("group_1", poll = false)

        viewModel.onMessageChange("Hello Team")

        // תיקון: שימוש ב-id במקום _id, והמרת הזמן ל-String
        val mockMessage = Message(
            id = "msg_99",
            groupId = "group_1",
            senderId = "user_1",
            senderName = "Elad",
            content = "Hello Team",
            timestamp = System.currentTimeMillis().toString()
        )

        whenever(mockRepo.sendMessage(any(), any(), any(), any()))
            .thenReturn(Result.success(mockMessage))

        // 2. Act
        viewModel.sendMessage()

        // 3. Assert
        assertEquals("", viewModel.messageText.value)
        verify(mockRepo).sendMessage(eq("group_1"), eq("user_1"), eq("Elad"), eq("Hello Team"))
    }
}