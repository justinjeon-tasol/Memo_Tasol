package com.fileshare.app.data.local.dao

import androidx.room.*
import com.fileshare.app.data.local.entity.DocumentImageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentImageDao {
    
    @Query("SELECT * FROM document_files ORDER BY displayOrder ASC")
    fun getAllImages(): Flow<List<DocumentImageEntity>>
    
    @Query("SELECT * FROM document_files WHERE documentId = :documentId ORDER BY displayOrder ASC")
    fun getImagesByDocumentId(documentId: Long): Flow<List<DocumentImageEntity>>
    
    @Query("SELECT * FROM document_files WHERE documentId = :documentId ORDER BY displayOrder ASC")
    suspend fun getImagesByDocumentIdSync(documentId: Long): List<DocumentImageEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: DocumentImageEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImages(images: List<DocumentImageEntity>)
    
    @Delete
    suspend fun deleteImage(image: DocumentImageEntity)
    
    @Query("DELETE FROM document_files WHERE id = :id")
    suspend fun deleteImageById(id: Long)
    
    @Query("DELETE FROM document_files WHERE documentId = :documentId")
    suspend fun deleteImagesByDocumentId(documentId: Long)
}
