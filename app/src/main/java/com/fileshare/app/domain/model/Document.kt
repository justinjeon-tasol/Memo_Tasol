package com.fileshare.app.domain.model

data class Document(
    val id: String = "",
    val title: String,
    val categoryId: Long, // Changed from category: DocumentCategory to categoryId
    val imageUris: List<String> = emptyList(), // Multi-image support
    val memo: String? = null,
    val fileSizeBytes: Long = 0,
    val mimeType: String = "image/jpeg",
    val createdAt: Long,
    val updatedAt: Long,
    val shareCount: Int = 0
) {
    // First image for backward compatibility
    val fileUri: String
        get() = imageUris.firstOrNull() ?: ""
}
