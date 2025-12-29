package com.example.studycollab.data.remote

import com.example.studycollab.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("auth/login")
    suspend fun loginUser(@Body credentials: Map<String, String>): User

    // --- Study Group Endpoints ---
    @GET("api/groups/user/{userId}")
    suspend fun getUserGroups(@Path("userId") userId: String): Response<List<StudyGroup>>

    @POST("api/groups/create")
    suspend fun createGroup(@Body request: CreateGroupRequest): Response<StudyGroup>

    // --- Course & Student Endpoints ---
    @GET("api/courses/user/{userId}")
    suspend fun getUserCourses(@Path("userId") userId: String): Response<List<Course>>

    @GET("api/courses/{courseId}/students")
    suspend fun getStudentsByCourse(@Path("courseId") courseId: String): Response<List<User>>
}