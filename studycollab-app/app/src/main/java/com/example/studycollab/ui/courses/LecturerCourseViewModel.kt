package com.example.studycollab.ui.courses

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.studycollab.data.model.Course
import com.example.studycollab.data.model.Material
import com.example.studycollab.data.remote.ApiClient
import com.example.studycollab.utils.UserSession
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class LecturerCourseViewModel(application: Application) : AndroidViewModel(application) {

    var lecturerCourses by mutableStateOf<List<Course>>(emptyList())
        private set
    var materials by mutableStateOf<List<Material>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var selectedCourse by mutableStateOf<Course?>(null)
        private set

    // Fetch courses for the logged-in lecturer
    fun fetchLecturerCourses() {
        viewModelScope.launch {
            isLoading = true
            try {
                val userId = UserSession.userId ?: return@launch
                val response = ApiClient.apiService.getUserCourses(userId)
                if (response.isSuccessful) {
                    lecturerCourses = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    // Toggle topic completion
    fun toggleTopic(courseId: String, topicIndex: Int) {
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.toggleTopic(courseId, topicIndex)
                if (response.isSuccessful) {
                    // Update local state
                    selectedCourse = response.body()
                    // Also refresh the list so other screens reflect changes
                    fetchLecturerCourses()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Add a new topic to syllabus
    fun addTopic(courseId: String, title: String) {
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.addTopic(courseId, mapOf("title" to title))
                if (response.isSuccessful) {
                    selectedCourse = response.body()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Remove a topic
    fun removeTopic(courseId: String, topicIndex: Int) {
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.removeTopic(courseId, topicIndex)
                if (response.isSuccessful) {
                    selectedCourse = response.body()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Load course details
    fun loadCourse(courseId: String) {
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.getCourseById(courseId)
                if (response.isSuccessful) {
                    selectedCourse = response.body()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // --- Materials ---

    fun fetchMaterials(courseId: String) {
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.getCourseMaterials(courseId)
                if (response.isSuccessful) {
                    materials = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun uploadMaterial(courseId: String, title: String, fileUri: Uri) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                val filePart = uriToMultipart(context, fileUri, "file")
                val courseIdBody = courseId.toRequestBody("text/plain".toMediaTypeOrNull())
                val titleBody = title.toRequestBody("text/plain".toMediaTypeOrNull())
                val lecturerIdBody = (UserSession.userId ?: "").toRequestBody("text/plain".toMediaTypeOrNull())

                val response = ApiClient.apiService.uploadMaterial(filePart, courseIdBody, titleBody, lecturerIdBody)
                if (response.isSuccessful) {
                    fetchMaterials(courseId) // Refresh list
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteMaterial(materialId: String, courseId: String) {
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.deleteMaterial(materialId)
                if (response.isSuccessful) {
                    fetchMaterials(courseId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // --- Assignments ---

    fun uploadAssignment(courseId: String, title: String, dueAt: String, fileUri: Uri) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                val filePart = uriToMultipart(context, fileUri, "file")

                val response = ApiClient.apiService.uploadAssignment(
                    file = filePart,
                    courseId = courseId.toRequestBody("text/plain".toMediaTypeOrNull()),
                    title = title.toRequestBody("text/plain".toMediaTypeOrNull()),
                    description = "".toRequestBody("text/plain".toMediaTypeOrNull()),
                    dueAt = dueAt.toRequestBody("text/plain".toMediaTypeOrNull()),
                    creatorId = (UserSession.userId ?: "").toRequestBody("text/plain".toMediaTypeOrNull())
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Helper: Convert Uri to MultipartBody.Part
    private fun uriToMultipart(context: Context, uri: Uri, fieldName: String): MultipartBody.Part {
        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(uri)!!
        val bytes = inputStream.readBytes()
        inputStream.close()

        val fileName = uri.lastPathSegment ?: "file.pdf"
        val mimeType = contentResolver.getType(uri) ?: "application/pdf"
        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())

        return MultipartBody.Part.createFormData(fieldName, fileName, requestBody)
    }
}