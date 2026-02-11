package com.example.studycollab.data.remote

import com.example.studycollab.data.model.*
import com.example.studycollab.ui.scheduler.SlotInfo
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // --- Auth ---
    @POST("auth/login")
    suspend fun loginUser(@Body credentials: Map<String, String>): User

    // --- Study Group Endpoints ---
    @GET("api/groups/user/{userId}")
    suspend fun getUserGroups(@Path("userId") userId: String): Response<List<StudyGroup>>

    @POST("api/groups/create")
    suspend fun createGroup(@Body request: CreateGroupRequest): Response<StudyGroup>

    @GET("api/groups/{groupId}/participants")
    suspend fun getGroupParticipants(@Path("groupId") groupId: String): Response<List<GroupMember>>

    @HTTP(method = "DELETE", path = "api/groups/{groupId}", hasBody = true)
    suspend fun deleteGroup(@Path("groupId") id: String, @Body body: Map<String, String>): Response<ResponseBody>

    // --- Course & Student Endpoints ---
    @GET("api/courses/user/{userId}")
    suspend fun getUserCourses(@Path("userId") userId: String): Response<List<Course>>

    @GET("api/courses/{courseId}/students")
    suspend fun getStudentsByCourse(@Path("courseId") courseId: String): Response<List<User>>

    // --- Notifications ---
    @GET("api/notifications/user/{userId}")
    suspend fun getNotifications(@Path("userId") userId: String): Response<List<Notification>>

    @DELETE("api/notifications/{id}")
    suspend fun deleteNotification(@Path("id") id: String): Response<Unit>

    // --- Chat Endpoints (NEW) ---
    @GET("api/messages/{groupId}")
    suspend fun getGroupMessages(@Path("groupId") groupId: String): Response<List<Message>>

    @POST("api/messages")
    suspend fun sendMessage(@Body request: SendMessageRequest): Response<Message>

    // --- Assignments ---
    @GET("api/assignments/course/{courseId}")
    suspend fun getCourseAssignments(@Path("courseId") courseId: String): Response<List<Assignment>>

    // --- Group Work Breakdown ---
    @GET("api/group-work/{groupId}/{assignmentId}")
    suspend fun getGroupWork(
        @Path("groupId") groupId: String,
        @Path("assignmentId") assignmentId: String
    ): Response<GroupAssignmentWork>

    @POST("api/group-work/{workId}/subtasks")
    suspend fun addSubTask(
        @Path("workId") workId: String,
        @Body taskData: Map<String, String> // title, assignedTo, adminId, groupId, assignmentId
    ): Response<GroupAssignmentWork>

    @PATCH("api/group-work/{workId}/subtasks/{subTaskId}/complete")
    suspend fun completeSubTask(
        @Path("workId") workId: String,
        @Path("subTaskId") subTaskId: String,
        @Body body: Map<String, String> // ADD THIS to accept the userId/completedBy data
    ): Response<GroupAssignmentWork>

    @PATCH("api/group-work/{workId}/subtasks/{subTaskId}/approve")
    suspend fun approveSubTask(
        @Path("workId") workId: String,
        @Path("subTaskId") subTaskId: String,
        @Body adminData: Map<String, String> // adminId
    ): Response<GroupAssignmentWork>

    // --- ADDED: Sub-task Deletion (Admin Only) ---
    @DELETE("api/group-work/{workId}/subtasks/{subTaskId}")
    suspend fun deleteSubTask(
        @Path("workId") workId: String,
        @Path("subTaskId") subTaskId: String
    ): Response<GroupAssignmentWork>

    // In com.example.studycollab.data.remote.ApiService
    @GET("submissions/assignment/{assignmentId}")
    suspend fun getSubmissionsForAssignment(
        @Path("assignmentId") assignmentId: String
    ): Response<List<Submission>>

    @PATCH("submissions/{submissionId}/grade")
    suspend fun updateGrade(
        @Path("submissionId") submissionId: String,
        @Body data: Map<String, String>
    ): Response<Submission>

    // In com.example.studycollab.data.remote.ApiService
    @GET("api/office-hours/lecturer/{lecturerId}")
    suspend fun getOfficeHours(@Path("lecturerId") lecturerId: String): Response<List<SlotInfo>>

    @POST("api/office-hours/create")
    suspend fun createOfficeHour(@Body data: Map<String, String>): Response<SlotInfo>

    @PATCH("api/office-hours/book/{slotId}")
    suspend fun bookOfficeHour(@Path("slotId") slotId: String, @Body data: Map<String, String>): Response<Unit>

    @DELETE("api/office-hours/{slotId}")
    suspend fun deleteOfficeHour(@Path("slotId") slotId: String): Response<Unit>
}