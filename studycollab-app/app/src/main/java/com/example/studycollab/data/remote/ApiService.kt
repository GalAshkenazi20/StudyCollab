package com.example.studycollab.data.remote

import com.example.studycollab.data.model.User
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    // This is an example login endpoint. We will use this later.
    // It sends a generic Map (JSON) and expects a User object back.
    @POST("auth/login")
    suspend fun loginUser(@Body credentials: Map<String, String>): User

    @POST("api/groups/create")
    suspend fun createGroup(@Body request: com.example.studycollab.data.model.CreateGroupRequest): com.example.studycollab.data.model.StudyGroup
}