package com.fileshare.app.domain.model

data class Document(
    val id: String = "",
    val title: String,
    val categoryId: Long,
    val imageUris: List<String> = emptyList(),
    val memo: String? = null,
    val fileSizeBytes: Long = 0,
    val mimeType: String = "image/jpeg",
    val createdAt: Long,
    val updatedAt: Long,
    val shareCount: Int = 0,
    val createdBy: String = "" // 작성자 ID 필드 추가
) {
    val fileUri: String
        get() = imageUris.firstOrNull() ?: ""
}
