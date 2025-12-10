package com.fileshare.app.data.local.entity

import androidx.room.*
import com.fileshare.app.domain.model.FileType

/**
 * 문서에 첨부된 파일 (이미지 또는 PDF)
 */
@Entity(
    tableName = "document_files",
    foreignKeys = [
        ForeignKey(
            entity = DocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("documentId")]
)
data class DocumentImageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val documentId: Long,
    
    @ColumnInfo(name = "fileUri")
    val imageUri: String, // 파일 경로 (이미지 또는 PDF)
    
    val fileType: String = "IMAGE", // "IMAGE" or "PDF"
    
    val displayOrder: Int,
    
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getFileTypeEnum(): FileType {
        return try {
            FileType.valueOf(fileType)
        } catch (e: Exception) {
            FileType.IMAGE
        }
    }
}
