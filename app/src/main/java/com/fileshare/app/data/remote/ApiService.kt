package com.fileshare.app.data.remote

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.DELETE
import retrofit2.http.Path
import retrofit2.http.Part
import retrofit2.http.PATCH
// DTO 클래스들은 RemoteDataModels.kt에 정의된 것을 사용합니다. (같은 패키지라 import 불필요)

// Login 관련 DTO는 RemoteDataModels.kt에 없다면 남겨두거나 이동해야 합니다.
// RemoteDataModels.kt 확인 결과 LoginRequest/Response가 없으므로 여기 유지합니다.
data class LoginRequest(val username: String, val password: String) 
data class LoginResponse(val access_token: String, val role: String?)

interface ApiService {
    // Auth
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // Items (Documents)
    @GET("items")
    suspend fun getItems(): Response<List<ItemResponse>>

    @GET("items/{id}")
    suspend fun getItem(@Path("id") id: String): Response<ItemResponse>

    @POST("items")
    suspend fun createItem(@Body request: CreateItemDto): Response<ItemResponse>

    @DELETE("items/{id}")
    suspend fun deleteItem(@Path("id") id: String): Response<Unit>

    @PATCH("items/{id}")
    suspend fun updateItem(@Path("id") id: String, @Body request: CreateItemDto): Response<ItemResponse>

    // Attachments
    @GET("items/{id}/attachments")
    suspend fun getAttachments(@Path("id") itemId: String): Response<List<AttachmentResponse>>

    @Multipart
    @POST("items/{id}/attachments")
    suspend fun uploadAttachment(
        @Path("id") itemId: String,
        @Part file: MultipartBody.Part
    ): Response<AttachmentResponse>

    // Users (Admin Only)
    @GET("users")
    suspend fun getUsers(): Response<List<UserDto>>

    @POST("users")
    suspend fun createUser(@Body request: CreateUserDto): Response<UserDto>

    @PATCH("users/{id}")
    suspend fun updateUser(@Path("id") id: String, @Body request: UpdateUserDto): Response<UserDto>
}
