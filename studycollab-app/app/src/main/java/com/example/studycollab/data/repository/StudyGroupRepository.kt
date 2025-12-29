package com.example.studycollab.data.repository

import com.example.studycollab.data.model.*
import com.example.studycollab.data.remote.ApiService
import retrofit2.Response

class StudyGroupRepository(private val api: ApiService) {

    // 1. MUST MATCH: viewModel calls 'getUserCourses'
    suspend fun getUserCourses(userId: String): List<Course> {
        return try {
            val response = api.getUserCourses(userId)
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // 2. MUST MATCH: viewModel calls 'getStudentsInCourse'
    suspend fun getStudentsInCourse(courseId: String): List<User> {
        return try {
            val response = api.getStudentsByCourse(courseId)
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // 3. Fetching existing groups
    suspend fun getGroupsForUser(userId: String): List<StudyGroup> {
        return try {
            val response = api.getUserGroups(userId)
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // 4. Creating a new group
    suspend fun createGroup(
        name: String,
        courseId: String,
        creatorId: String,
        purpose: String
    ): Result<StudyGroup> {
        return try {
            val request = CreateGroupRequest(name, courseId, creatorId, purpose, "")
            val response = api.createGroup(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Server Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}