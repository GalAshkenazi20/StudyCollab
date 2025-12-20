package com.example.studycollab.data.repository

import com.example.studycollab.data.model.CreateGroupRequest
import com.example.studycollab.data.model.StudyGroup
import com.example.studycollab.data.remote.ApiClient

class StudyGroupRepository {
    private val api = ApiClient.apiService

    suspend fun createGroup(name: String, courseId: String, creatorId: String, purpose: String): Result<StudyGroup> {
        return try {
            val request = CreateGroupRequest(
                groupName = name,
                courseId = courseId,
                creatorId = creatorId,
                purpose = purpose,
                description = "Created via Android App"
            )
            val response = api.createGroup(request)
            Result.success(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}