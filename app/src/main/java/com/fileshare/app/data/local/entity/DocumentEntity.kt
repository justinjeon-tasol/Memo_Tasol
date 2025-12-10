package com.fileshare.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fileshare.app.domain.model.Document

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val categoryId: Long, // Changed from category: String to categoryId: Long
    val fileUri: String, // Keep for backward compatibility, stores first image
    val memo: String? = null,
    val fileSizeBytes: Long = 0,
    val mimeType: String = "image/jpeg",
    val createdAt: Long,
    val updatedAt: Long,
    val shareCount: Int = 0
) {
    fun toDomain(imageUris: List<String> = emptyList()): Document {
        // If imageUris is empty, use fileUri for backward compatibility
        val finalImageUris = if (imageUris.isEmpty() && fileUri.isNotBlank()) {
            listOf(fileUri)
        } else {
            imageUris
        }
        
        return Document(
            id = id.toString(),
            title = title,
            categoryId = categoryId,
            imageUris = finalImageUris,
            memo = memo,
            fileSizeBytes = fileSizeBytes,
            mimeType = mimeType,
            createdAt = createdAt,
            updatedAt = updatedAt,
            shareCount = shareCount
        )
    }

    companion object {
        fun fromDomain(document: Document): DocumentEntity {
            return DocumentEntity(
                id = document.id.toLongOrNull() ?: 0L,
                title = document.title,
                categoryId = document.categoryId,
                fileUri = document.imageUris.firstOrNull() ?: "",
                memo = document.memo,
                fileSizeBytes = document.fileSizeBytes,
                mimeType = document.mimeType,
                createdAt = document.createdAt,
                updatedAt = document.updatedAt,
                shareCount = document.shareCount
            )
        }
    }
}
