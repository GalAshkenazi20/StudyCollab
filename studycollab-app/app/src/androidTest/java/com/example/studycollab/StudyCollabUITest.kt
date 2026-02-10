package com.example.studycollab

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

class StudyCollabUITest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testLoginFlow() {
        // 1. Input credentials
        composeTestRule.onNodeWithText("University Email").performTextInput("elad0@msmail.uni.ac.il")
        composeTestRule.onNodeWithText("Password").performTextInput("123456")

        // 2. Click login button
        composeTestRule.onNodeWithText("Login").performClick()

        // 3. Wait for Dashboard title
        composeTestRule.waitUntil(5000) {
            composeTestRule
                .onAllNodesWithText("Dashboard", substring = true, ignoreCase = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // 4. Verify arrival
        composeTestRule.onNodeWithText("Dashboard", substring = true, ignoreCase = true).assertIsDisplayed()
    }

    @Test
    fun testChatSendMessageClearsInput() {
        // 1. Login sequence
        composeTestRule.onNodeWithText("University Email").performTextInput("elad0@msmail.uni.ac.il")
        composeTestRule.onNodeWithText("Password").performTextInput("123456")
        composeTestRule.onNodeWithText("Login").performClick()

        // 2. Navigate to Study Groups screen
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithText("Study Groups", substring = true, ignoreCase = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Study Groups", substring = true, ignoreCase = true).performClick()

        // 3. Select the "CS" group
        composeTestRule.waitUntil(10000) {
            composeTestRule
                .onAllNodesWithText("CS", substring = true, ignoreCase = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("CS", substring = true, ignoreCase = true).performClick()

        // 4. Enter the Chat room
        composeTestRule.waitUntil(5000) {
            composeTestRule
                .onAllNodesWithText("Group Chat", ignoreCase = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Group Chat", ignoreCase = true).performClick()

        // 5. Interaction: Type message
        val testMessage = "Hello Espresso Test!"

        composeTestRule.waitUntil(5000) {
            composeTestRule
                .onAllNodesWithText("Type a message...", ignoreCase = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Type a message...", ignoreCase = true).performTextInput(testMessage)

        // 6. Click Send
        composeTestRule.onNodeWithContentDescription("Send", ignoreCase = true).performClick()

        // 7. Verification:
        // We do NOT use assertDoesNotExist(testMessage) because the message is now in the history.
        // Instead, we verify that the placeholder "Type a message..." is visible again in the input field.
        composeTestRule.onNodeWithText("Type a message...", ignoreCase = true).assertIsDisplayed()
    }
}