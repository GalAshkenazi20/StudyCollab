package com.example.studycollab

import com.example.studycollab.data.model.GroupAssignmentWork
import com.example.studycollab.data.remote.ApiService
import com.example.studycollab.ui.tasks.AssignmentViewModel
import com.example.studycollab.utils.UserSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class AssignmentViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun testAddSubTask_Success() = runTest {
        // 1. Arrange
        val mockApiService = mock<ApiService>()

        // CRITICAL: Set userId BEFORE ViewModel init to pass guard clauses
        UserSession.userId = "admin1"

        val viewModel = AssignmentViewModel(mockApiService)

        val mockWork = GroupAssignmentWork(
            id = "work1",
            groupId = "group1",
            assignmentId = "assign1",
            subTasks = emptyList()
        )

        // Use any() to match (String, Map) exactly as your ApiService requires
        whenever(mockApiService.addSubTask(any(), any()))
            .thenReturn(Response.success(mockWork))

        // 2. Act
        viewModel.addSubTask(
            workId = "work1",
            title = "Complete UI Design",
            assignedToId = "student2",
            groupId = "group1",
            assignmentId = "assign1"
        )

        // 3. Assert
        // This forces the launch{} block in the ViewModel to finish
        advanceUntilIdle()

        // Verify the mock was actually called
        verify(mockApiService).addSubTask(eq("work1"), any())

        assertNotNull("currentGroupWork should be updated by the API response", viewModel.currentGroupWork)
        assertEquals("work1", viewModel.currentGroupWork?.id)
    }
}