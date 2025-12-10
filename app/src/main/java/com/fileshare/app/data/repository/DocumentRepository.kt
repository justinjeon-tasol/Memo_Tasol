package com.fileshare.app.data.repository

import com.fileshare.app.domain.model.Document
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull

class DocumentRepository(
    private val apiService: com.fileshare.app.data.remote.ApiService
) {
    
    private val TAG = "DocumentRepository"
    private val baseUrl = com.fileshare.app.BuildConfig.API_BASE_URL
    
    fun getAllDocuments(): Flow<List<Document>> = flow {
        try {
            android.util.Log.d(TAG, "getAllDocuments: Fetching items from API...")
            val response = apiService.getItems()
            android.util.Log.d(TAG, "getAllDocuments: Response code=${response.code()}, isSuccessful=${response.isSuccessful}")
            
            if (response.isSuccessful) {
                val items = response.body() ?: emptyList()
                android.util.Log.d(TAG, "getAllDocuments: Found ${items.size} items")
                
                val documents = items.map { item ->
                    // Fetch images for each item
                    try {
                         val attachmentResponse = apiService.getAttachments(item.id)
                         val uris = if(attachmentResponse.isSuccessful) {
                             attachmentResponse.body()?.map { "${baseUrl}attachments/${it.id}/download" } ?: emptyList() 
                         } else emptyList()
                         
                         Document(
                             id = item.id,
                             title = item.title,
                             categoryId = item.categoryId ?: 0L, 
                             imageUris = uris,
                             memo = item.description,
                             createdAt = System.currentTimeMillis(), // TODO: Parse date
                             updatedAt = System.currentTimeMillis()
                         )
                    } catch (e: Exception) {
                        android.util.Log.e(TAG, "getAllDocuments: Error fetching attachments for item ${item.id}", e)
                        // Fallback if image fetch fails
                        Document(
                             id = item.id,
                             title = item.title,
                             categoryId = item.categoryId ?: 0L,
                             createdAt = System.currentTimeMillis(),
                             updatedAt = System.currentTimeMillis()
                        )
                    }
                }
                emit(documents)
            } else {
                android.util.Log.e(TAG, "getAllDocuments: API error - ${response.errorBody()?.string()}")
                emit(emptyList())
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "getAllDocuments: Exception occurred", e)
            emit(emptyList())
        }
    }

    // TODO: Implement category filtering on backend
    fun getDocumentsByCategory(categoryId: Long): Flow<List<Document>> = getAllDocuments()
        .map { list -> list.filter { it.categoryId == categoryId } }

    suspend fun getDocumentById(id: String): Document? {
        return try {
            val response = apiService.getItem(id)
            if (response.isSuccessful && response.body() != null) {
                val item = response.body()!!
                val attachmentResponse = apiService.getAttachments(item.id)
                val uris = if (attachmentResponse.isSuccessful) {
                    attachmentResponse.body()?.map { "${baseUrl}attachments/${it.id}/download" } ?: emptyList()
                } else emptyList()
                
                Document(
                    id = item.id,
                    title = item.title,
                    categoryId = item.categoryId ?: 0L,
                    imageUris = uris,
                    memo = item.description,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun getDocumentByIdFlow(id: String): Flow<Document?> = flow {
        try {
            val response = apiService.getItem(id)
            if (response.isSuccessful && response.body() != null) {
                val item = response.body()!!
                val attachmentResponse = apiService.getAttachments(item.id)
                val uris = if (attachmentResponse.isSuccessful) {
                    attachmentResponse.body()?.map { "${baseUrl}attachments/${it.id}/download" } ?: emptyList()
                } else emptyList()
                
                emit(Document(
                    id = item.id,
                    title = item.title,
                    categoryId = item.categoryId ?: 0L,
                    imageUris = uris,
                    memo = item.description,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                ))
            } else {
                emit(null)
            }
        } catch (e: Exception) {
            emit(null)
        }
    }

    suspend fun insertDocument(document: Document, fileTypes: List<String> = emptyList()): String {
        android.util.Log.d(TAG, "insertDocument: Creating document '${document.title}' with ${document.imageUris.size} images")
        
        val createDto = com.fileshare.app.data.remote.CreateItemDto(
            title = document.title,
            description = document.memo,
            dueDate = java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date()), // Default today
            categoryId = if (document.categoryId == 0L) null else document.categoryId
        )
        
        val response = apiService.createItem(createDto)
        android.util.Log.d(TAG, "insertDocument: createItem response code=${response.code()}, isSuccessful=${response.isSuccessful}")
        
        if (response.isSuccessful && response.body() != null) {
            val newItem = response.body()!!
            android.util.Log.d(TAG, "insertDocument: Created item with id=${newItem.id}")
            
            // Upload Images
            document.imageUris.forEach { uri ->
                 val file = java.io.File(uri)
                 android.util.Log.d(TAG, "insertDocument: Uploading file - uri=$uri, exists=${file.exists()}")
                 if (file.exists()) {
                     val requestFile = okhttp3.RequestBody.create("image/*".toMediaTypeOrNull(), file)
                     val body = okhttp3.MultipartBody.Part.createFormData("file", file.name, requestFile)
                     val uploadResponse = apiService.uploadAttachment(newItem.id, body)
                     android.util.Log.d(TAG, "insertDocument: Upload response code=${uploadResponse.code()}")
                 }
            }
            return newItem.id
        }
        
        val errorBody = response.errorBody()?.string()
        android.util.Log.e(TAG, "insertDocument: Failed to create document - $errorBody")
        throw Exception("Failed to create document: $errorBody")
    }

    suspend fun updateDocument(document: Document) {
        android.util.Log.d(TAG, "updateDocument: Updating document '${document.title}'")
        
        val updateDto = com.fileshare.app.data.remote.CreateItemDto(
            title = document.title,
            description = document.memo,
            dueDate = java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date()),
            categoryId = if (document.categoryId == 0L) null else document.categoryId
        )
        
        val response = apiService.updateItem(document.id, updateDto)
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            android.util.Log.e(TAG, "updateDocument: Failed - $errorBody")
            throw Exception("Failed to update document: $errorBody")
        }
        android.util.Log.d(TAG, "updateDocument: Success")
    }

    suspend fun deleteDocument(document: Document) {
        apiService.deleteItem(document.id)
    }

    suspend fun incrementShareCount(id: String) {
        // Not supported on backend yet
    }
}
