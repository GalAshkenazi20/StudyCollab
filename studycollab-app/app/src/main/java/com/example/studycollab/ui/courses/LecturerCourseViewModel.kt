package com.example.studycollab.ui.courses

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
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
import java.text.SimpleDateFormat
import java.util.*

class LecturerCourseViewModel(application: Application) : AndroidViewModel(application) {

    var lecturerCourses by mutableStateOf<List<Course>>(emptyList())
        private set
    var materials by mutableStateOf<List<Material>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var selectedCourse by mutableStateOf<Course?>(null)
        private set
    var uploadStatus by mutableStateOf<String?>(null)

    // --- Course & Syllabus Management ---

    fun fetchLecturerCourses() {
        viewModelScope.launch {
            isLoading = true
            try {
                val userId = UserSession.userId ?: return@launch
                val response = ApiClient.apiService.getUserCourses(userId)
                if (response.isSuccessful) {
                    lecturerCourses = response.body() ?: emptyList()
                }
            } catch (e: Exception) { Log.e("LecturerVM", "Fetch failed", e) }
            finally { isLoading = false }
        }
    }

    fun loadCourse(courseId: String) {
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.getCourseById(courseId)
                if (response.isSuccessful) {
                    selectedCourse = response.body()
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun toggleTopic(courseId: String, topicIndex: Int) {
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.toggleTopic(courseId, topicIndex)
                if (response.isSuccessful) {
                    selectedCourse = response.body()
                    fetchLecturerCourses() // Sync lists
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun addTopic(courseId: String, title: String) {
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.addTopic(courseId, mapOf("title" to title))
                if (response.isSuccessful) {
                    selectedCourse = response.body()
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun removeTopic(courseId: String, topicIndex: Int) {
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.removeTopic(courseId, topicIndex)
                if (response.isSuccessful) {
                    selectedCourse = response.body()
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // --- Materials & Assignments (Previously Refactored) ---

    fun fetchMaterials(courseId: String) {
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.getCourseMaterials(courseId)
                if (response.isSuccessful) materials = response.body() ?: emptyList()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun uploadMaterial(courseId: String, title: String, fileUri: Uri) {
        viewModelScope.launch {
            isLoading = true
            try {
                val context = getApplication<Application>().applicationContext
                val filePart = uriToMultipart(context, fileUri, "file")
                val response = ApiClient.apiService.uploadMaterial(
                    filePart,
                    courseId.toRequestBody("text/plain".toMediaTypeOrNull()),
                    title.toRequestBody("text/plain".toMediaTypeOrNull()),
                    (UserSession.userId ?: "").toRequestBody("text/plain".toMediaTypeOrNull())
                )
                if (response.isSuccessful) fetchMaterials(courseId)
            } finally { isLoading = false }
        }
    }

    fun deleteMaterial(materialId: String, courseId: String) {
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.deleteMaterial(materialId)
                if (response.isSuccessful) fetchMaterials(courseId)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun publishAssignment(courseId: String, title: String, deadlineMillis: Long, fileUri: Uri?) {
        viewModelScope.launch {
            isLoading = true
            try {
                val context = getApplication<Application>().applicationContext
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                val formattedDate = sdf.format(Date(deadlineMillis))
                val filePart = fileUri?.let { uriToMultipart(context, it, "file") }

                ApiClient.apiService.uploadAssignment(
                    file = filePart,
                    courseId = courseId.toRequestBody("text/plain".toMediaTypeOrNull()),
                    title = title.toRequestBody("text/plain".toMediaTypeOrNull()),
                    description = "New Assignment Published".toRequestBody("text/plain".toMediaTypeOrNull()),
                    dueAt = formattedDate.toRequestBody("text/plain".toMediaTypeOrNull()),
                    creatorId = (UserSession.userId ?: "").toRequestBody("text/plain".toMediaTypeOrNull())
                )
                uploadStatus = "Assignment Published!"
            } finally { isLoading = false }
        }
    }

    private fun uriToMultipart(context: Context, uri: Uri, fieldName: String): MultipartBody.Part {
        val inputStream = context.contentResolver.openInputStream(uri)!!
        val bytes = inputStream.readBytes()
        inputStream.close()
        val fileName = uri.lastPathSegment ?: "file"
        val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
        return MultipartBody.Part.createFormData(fieldName, fileName, bytes.toRequestBody(mimeType.toMediaTypeOrNull()))
    }
}