package com.fileshare.app.data.remote

import com.google.gson.annotations.SerializedName

// Request to create an item
data class CreateItemDto(
    val title: String,
    val description: String?,
    @SerializedName("due_date") val dueDate: String,
    @SerializedName("status") val status: String = "PLANNED",
    @SerializedName("category_id") val categoryId: Long? = null
)

// Response for an Item (Document)
data class ItemResponse(
    val id: String,
    val title: String,
    val description: String?,
    @SerializedName("due_date") val dueDate: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("category_id") val categoryId: Long?,
    @SerializedName("created_by") val createdBy: String? = null,
    @SerializedName("created_by_id") val createdById: String? = null // 백엔드 원본 필드
) {
    // created_by 또는 created_by_id 중 하나라도 있으면 반환
    fun getCreatorId(): String = createdBy ?: createdById ?: ""
}

// Response for an Attachment (File)
data class AttachmentResponse(
    val id: String,
    @SerializedName("file_name") val fileName: String,
    @SerializedName("mime_type") val mimeType: String,
    @SerializedName("file_size") val fileSize: Long
)

// User Management DTOs
enum class UserRole {
    ADMIN, USER
}

data class UserDto(
    val id: String,
    val username: String,
    val email: String?,
    val role: UserRole,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("created_at") val createdAt: String
)

data class CreateUserDto(
    val username: String,
    val password: String,
    val role: UserRole = UserRole.USER,
    val email: String? = null
)

data class UpdateUserDto(
    val username: String? = null,
    val password: String? = null,
    val role: UserRole? = null,
    val email: String? = null,
    @SerializedName("is_active") val isActive: Boolean? = null
)
