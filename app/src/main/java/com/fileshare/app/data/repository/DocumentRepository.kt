package com.fileshare.app.data.repository

import com.fileshare.app.domain.model.Document
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class DocumentRepository(
    private val apiService: com.fileshare.app.data.remote.ApiService
) {
    
    private val baseUrl = com.fileshare.app.BuildConfig.API_BASE_URL
    
    private fun parseDate(dateString: String): Long {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            format.timeZone = TimeZone.getTimeZone("UTC")
            format.parse(dateString)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    private fun getDownloadUrl(attachmentId: String): String {
        return "${baseUrl}attachments/$attachmentId/download"
    }

    fun getAllDocuments(): Flow<List<Document>> = flow {
        try {
            val response = apiService.getItems()
            if (response.isSuccessful) {
                val items = response.body() ?: emptyList()
                val documents = items.map { item ->
                    // Fetch images for each item
                    val uris = try {
                         val attachmentResponse = apiService.getAttachments(item.id)
                         if(attachmentResponse.isSuccessful) {
                             attachmentResponse.body()?.map { getDownloadUrl(it.id) } ?: emptyList() 
                         } else emptyList()
                    } catch (e: Exception) {
                        emptyList()
                    }
                         
                    Document(
                        id = item.id,
                        title = item.title,
                        categoryId = item.categoryId ?: 0L, 
                        imageUris = uris,
                        memo = item.description,
                        createdAt = parseDate(item.createdAt),
                        updatedAt = parseDate(item.updatedAt),
                        createdBy = item.getCreatorId() // 작성자 ID 매핑
                    )
                }
                emit(documents)
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }

    // TODO: Implement category filtering on backend
    fun getDocumentsByCategory(categoryId: Long): Flow<List<Document>> = getAllDocuments()
        .map { list -> list.filter { it.categoryId == categoryId } }

    suspend fun getDocumentById(id: String): Document? {
         return null 
    }

    fun getDocumentByIdFlow(id: String): Flow<Document?> = flow {
        try {
            val response = apiService.getItems()
            if (response.isSuccessful) {
                val items = response.body() ?: emptyList()
                val item = items.find { it.id == id }
                
                if (item != null) {
                    val uris = try {
                         val attachmentResponse = apiService.getAttachments(item.id)
                         if(attachmentResponse.isSuccessful) {
                             attachmentResponse.body()?.map { getDownloadUrl(it.id) } ?: emptyList() 
                         } else emptyList()
                    } catch (e: Exception) {
                        emptyList()
                    }

                    val document = Document(
                        id = item.id,
                        title = item.title,
                        categoryId = item.categoryId ?: 0L, 
                        imageUris = uris,
                        memo = item.description,
                        createdAt = parseDate(item.createdAt),
                        updatedAt = parseDate(item.updatedAt),
                        createdBy = item.getCreatorId() // 작성자 ID 매핑
                    )
                    emit(document)
                } else {
                    emit(null)
                }
            } else {
                emit(null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(null)
        }
    }

    suspend fun insertDocument(document: Document, fileTypes: List<String> = emptyList()): String {
        val createDto = com.fileshare.app.data.remote.CreateItemDto(
            title = document.title,
            description = document.memo,
            dueDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(java.util.Date()),
            categoryId = if (document.categoryId == 0L) null else document.categoryId
        )
        
        val response = apiService.createItem(createDto)
        if (response.isSuccessful && response.body() != null) {
            val newItem = response.body()!!
            
            // Upload Images
            document.imageUris.forEach { uri ->
                 val file = java.io.File(uri)
                 if (file.exists()) {
                     val requestFile = okhttp3.RequestBody.create("image/*".toMediaTypeOrNull(), file)
                     val body = okhttp3.MultipartBody.Part.createFormData("file", file.name, requestFile)
                     apiService.uploadAttachment(newItem.id, body)
                 }
            }
            return newItem.id
        }
        throw Exception("Failed to create document: ${response.code()}")
    }

    suspend fun deleteDocument(document: Document) {
        apiService.deleteItem(document.id)
    }

    suspend fun updateDocument(document: Document) {
        // Update logic not implemented fully yet, using delete/create logic or specific update endpoint
        // Assuming update endpoint exists or just skipping for now
    }

    suspend fun incrementShareCount(id: String) {
        // Not supported on backend yet
    }
}
