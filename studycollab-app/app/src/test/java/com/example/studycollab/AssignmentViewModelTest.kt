import com.example.studycollab.data.model.GroupAssignmentWork
import com.example.studycollab.data.remote.ApiService
import com.example.studycollab.ui.tasks.AssignmentViewModel
import com.example.studycollab.utils.UserSession
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*
import retrofit2.Response

class AssignmentViewModelTest {

    // חובה להוסיף את ה-Rule הזה כדי לאפשר בדיקת Coroutines ב-ViewModel
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun testAddSubTask_Success() = runTest {
        // 1. Arrange
        val mockApiService = mock<ApiService>()

        // הזרקת ה-Mock לתוך ה-ViewModel (דורש את הרפקטורינג ב-ViewModel)
        val viewModel = AssignmentViewModel(mockApiService)
        UserSession.userId = "admin1"

        val mockWork = GroupAssignmentWork(
            id = "work1",
            groupId = "group1",
            assignmentId = "assign1",
            subTasks = emptyList()
        )

        // תיקון: שימוש ב-retrofit2.Response.success
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
        assertNotNull(viewModel.currentGroupWork)
        assertEquals("work1", viewModel.currentGroupWork?.id)
    }
}