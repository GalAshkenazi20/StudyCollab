package com.example.studycollab.data.remote

import com.example.studycollab.data.model.*
import com.example.studycollab.ui.scheduler.SlotInfo
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface ApiService {

    // --- Auth ---
    @POST("auth/login")
    suspend fun loginUser(@Body credentials: Map<String, String>): User

    // --- Course Topics (Lecturer) ---
    @PUT("api/courses/{courseId}/topics/{topicIndex}/toggle")
    suspend fun toggleTopic(
        @Path("courseId") courseId: String,
        @Path("topicIndex") topicIndex: Int
    ): Response<Course>

    @PUT("api/courses/{courseId}/topics")
    suspend fun addTopic(
        @Path("courseId") courseId: String,
        @Body body: Map<String, String>
    ): Response<Course>

    @DELETE("api/courses/{courseId}/topics/{topicIndex}")
    suspend fun removeTopic(
        @Path("courseId") courseId: String,
        @Path("topicIndex") topicIndex: Int
    ): Response<Course>

    // --- Get single course ---
    @GET("api/courses/{courseId}")
    suspend fun getCourseById(
        @Path("courseId") courseId: String
    ): Response<Course>

    // --- Materials ---
    @GET("api/materials/course/{courseId}")
    suspend fun getCourseMaterials(
        @Path("courseId") courseId: String
    ): Response<List<Material>>

    @Multipart
    @POST("api/materials/upload")
    suspend fun uploadMaterial(
        @Part file: MultipartBody.Part,
        @Part("courseId") courseId: RequestBody,
        @Part("title") title: RequestBody,
        @Part("lecturerId") lecturerId: RequestBody
    ): Response<Material>

    @DELETE("api/materials/{materialId}")
    suspend fun deleteMaterial(
        @Path("materialId") materialId: String
    ): Response<Unit>

    // --- Assignments with file upload ---
    @Multipart
    @POST("api/assignments/upload")
    suspend fun uploadAssignment(
        @Part file: MultipartBody.Part?,
        @Part("courseId") courseId: RequestBody,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("dueAt") dueAt: RequestBody,
        @Part("creatorId") creatorId: RequestBody
    ): Response<Assignment>

    // --- Submissions with file upload ---
    @Multipart
    @POST("api/submissions/upload")
    suspend fun uploadSubmission(
        @Part file: MultipartBody.Part,
        @Part("assignmentId") assignmentId: RequestBody,
        @Part("courseId") courseId: RequestBody,
        @Part("groupId") groupId: RequestBody
    ): Response<Submission>

    // --- Notification Preferences ---
    @GET("auth/preferences/{userId}")
    suspend fun getNotificationPreferences(
        @Path("userId") userId: String
    ): Response<NotificationPreferences>

    @PUT("auth/preferences/{userId}")
    suspend fun updateNotificationPreferences(
        @Path("userId") userId: String,
        @Body preferences: NotificationPreferences
    ): Response<NotificationPreferences>

    // --- Study Group Endpoints ---
    @GET("api/groups/user/{userId}")
    suspend fun getUserGroups(@Path("userId") userId: String): Response<List<StudyGroup>>

    @POST("api/groups/create")
    suspend fun createGroup(@Body request: CreateGroupRequest): Response<StudyGroup>

    @GET("api/groups/{groupId}/participants")
    suspend fun getGroupParticipants(@Path("groupId") groupId: String): Response<List<GroupMember>>

    @HTTP(method = "DELETE", path = "api/groups/{groupId}", hasBody = true)
    suspend fun deleteGroup(@Path("groupId") id: String, @Body body: Map<String, String>): Response<ResponseBody>

    @GET("api/groups/course/{courseId}")
    suspend fun getGroupsByCourse(
        @Path("courseId") courseId: String
    ): Response<List<StudyGroup>>

    @POST("api/groups/consultation")
    suspend fun setupConsultation(
        @Body data: Map<String, String>
    ): Response<StudyGroup>

    @GET("api/courses/{courseId}/lecturer")
    suspend fun getCourseLecturer(
        @Path("courseId") courseId: String
    ): Response<Map<String, String>>

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

    // --- Chat Endpoints ---
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

    // --- Lecturers list (for student office hours) ---
    @GET("auth/lecturers")
    suspend fun getAllLecturers(): Response<List<LecturerInfo>>

    // --- Office Hours ---
    @GET("api/office-hours/available/{lecturerId}")
    suspend fun getAvailableOfficeHours(
        @Path("lecturerId") lecturerId: String
    ): Response<List<SlotInfo>>

    @GET("api/office-hours/lecturer/{lecturerId}")
    suspend fun getOfficeHours(@Path("lecturerId") lecturerId: String): Response<List<SlotInfo>>

    @POST("api/office-hours/create")
    suspend fun createOfficeHour(@Body data: Map<String, String>): Response<SlotInfo>

    @PATCH("api/office-hours/book/{slotId}")
    suspend fun bookOfficeHour(@Path("slotId") slotId: String, @Body data: Map<String, String>): Response<Unit>

    @DELETE("api/office-hours/{slotId}")
    suspend fun deleteOfficeHour(@Path("slotId") slotId: String): Response<Unit>

    // --- Group Work Sub-tasks ---
    @POST("api/group-work/{workId}/subtasks")
    suspend fun addSubTask(
        @Path("workId") workId: String,
        @Body taskData: Map<String, String>
    ): Response<GroupAssignmentWork>

    @PATCH("api/group-work/{workId}/subtasks/{subTaskId}/complete")
    suspend fun completeSubTask(
        @Path("workId") workId: String,
        @Path("subTaskId") subTaskId: String,
        @Body body: Map<String, String>
    ): Response<GroupAssignmentWork>

    @PATCH("api/group-work/{workId}/subtasks/{subTaskId}/approve")
    suspend fun approveSubTask(
        @Path("workId") workId: String,
        @Path("subTaskId") subTaskId: String,
        @Body adminData: Map<String, String>
    ): Response<GroupAssignmentWork>

    @DELETE("api/group-work/{workId}/subtasks/{subTaskId}")
    suspend fun deleteSubTask(
        @Path("workId") workId: String,
        @Path("subTaskId") subTaskId: String
    ): Response<GroupAssignmentWork>

    // --- Submissions ---
    @GET("submissions/assignment/{assignmentId}")
    suspend fun getSubmissionsForAssignment(
        @Path("assignmentId") assignmentId: String
    ): Response<List<Submission>>

    @PATCH("submissions/{submissionId}/grade")
    suspend fun updateGrade(
        @Path("submissionId") submissionId: String,
        @Body data: Map<String, String>
    ): Response<Submission>
}