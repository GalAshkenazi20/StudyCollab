package com.example.studycollab.ui.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studycollab.data.model.Course
import com.example.studycollab.data.remote.ApiClient
import com.example.studycollab.utils.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CourseViewModel : ViewModel() {

    // 1. הוספנו את זה: משתנה שמחזיק את רשימת הקורסים ללוח השנה
    private val _myCourses = MutableStateFlow<List<Course>>(emptyList())
    val myCourses: StateFlow<List<Course>> = _myCourses

    // משתנה לקורס בודד שנבחר (למסך פרטי קורס)
    private val _selectedCourse = MutableStateFlow<Course?>(null)
    val selectedCourse: StateFlow<Course?> = _selectedCourse

    // 2. בעת טעינת ה-ViewModel, נשלוף אוטומטית את הקורסים
    init {
        loadUserCourses()
    }

    // פונקציה לשליפת כל הקורסים של המשתמש (עבור ה-TimeTable)
    fun loadUserCourses() {
        viewModelScope.launch {
            val userId = UserSession.userId ?: return@launch
            try {
                val response = ApiClient.apiService.getUserCourses(userId)
                if (response.isSuccessful) {
                    _myCourses.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // פונקציה לטעינת קורס ספציפי (קיימת אצלך)
    fun loadCourseDetails(courseCode: String) {
        viewModelScope.launch {
            // אם הרשימה כבר טעונה, ננסה למצוא בה את הקורס כדי לחסוך קריאת רשת
            val existingCourse = _myCourses.value.find { it.code == courseCode }
            if (existingCourse != null) {
                _selectedCourse.value = existingCourse
                return@launch
            }

            // אם לא מצאנו, נשלוף מהשרת (כמו שהיה לך מקודם)
            val userId = UserSession.userId ?: return@launch
            try {
                val response = ApiClient.apiService.getUserCourses(userId)
                if (response.isSuccessful) {
                    _selectedCourse.value = response.body()?.find { it.code == courseCode }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}