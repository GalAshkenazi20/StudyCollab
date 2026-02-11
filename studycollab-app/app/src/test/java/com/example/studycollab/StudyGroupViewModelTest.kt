import com.example.studycollab.MainDispatcherRule
import com.google.gson.JsonPrimitive // חובה לייבא את זה
import com.example.studycollab.data.model.*
import com.example.studycollab.data.repository.StudyGroupRepository
import com.example.studycollab.ui.chat.StudyGroupViewModel
import com.example.studycollab.utils.UserSession
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

class StudyGroupViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `test createGroup success`() = runTest {
        val mockRepo = mock<StudyGroupRepository>()
        val viewModel = StudyGroupViewModel(mockRepo)

        // תיקון 1: הוספת code ו-semester
        val mockCourse = Course("c1", "Android", code = "ANDR101", semester = "A")

        UserSession.userId = "user1"
        viewModel.groupName = "Test Group"
        viewModel.selectedCourse = mockCourse

        // תיקון 2: יצירת אובייקט StudyGroup עם JsonPrimitive ופרמטרים נכונים
        val expectedGroup = StudyGroup(
            _id = "g1",
            name = "Test Group",
            courseId = JsonPrimitive("c1"), // המרה ל-JsonElement
            purpose = "general",
            description = null,
            members = emptyList()
        )

        whenever(mockRepo.createGroup(any(), any(), any(), any(), any()))
            .thenReturn(Result.success(expectedGroup))

        viewModel.createGroup()

        assertEquals("Group Created!", viewModel.successMessage)
    }
}