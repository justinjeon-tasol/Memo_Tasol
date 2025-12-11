package com.fileshare.app.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fileshare.app.data.repository.DocumentRepository
import com.fileshare.app.domain.model.Document
import com.fileshare.app.util.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DocumentViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: DocumentRepository
    
    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // 새로고침 트리거
    private val _refreshTrigger = MutableStateFlow(0)
    
    private val allDocuments: StateFlow<List<Document>> = combine(
        _selectedCategoryId,
        _refreshTrigger
    ) { categoryId, _ -> categoryId }
        .flatMapLatest { categoryId ->
            if (categoryId == null) {
                repository.getAllDocuments()
            } else {
                repository.getDocumentsByCategory(categoryId)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val documents: StateFlow<List<Document>> = combine(
        allDocuments,
        _searchQuery
    ) { docs, query ->
        if (query.isBlank()) {
            docs
        } else {
            docs.filter { doc ->
                doc.title.contains(query, ignoreCase = true) ||
                doc.memo?.contains(query, ignoreCase = true) == true
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        val appContainer = (application as com.fileshare.app.FileShareApplication).container
        repository = DocumentRepository(appContainer.apiService)
    }
    
    fun refreshDocuments() {
        // 캐시 비우기 - 수정된 내용이 반영되도록
        _documentCache.clear()
        _refreshTrigger.value++
    }
    
    fun selectCategory(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun clearSearch() {
        _searchQuery.value = ""
    }
    
    fun addDocument(document: Document) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.insertDocument(document)
                _error.value = null
                refreshDocuments()
            } catch (e: Exception) {
                _error.value = e.message ?: "문서 추가 실패"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateDocument(document: Document) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.updateDocument(document)
                _error.value = null
                refreshDocuments()
            } catch (e: Exception) {
                _error.value = e.message ?: "문서 수정 실패"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteDocument(document: Document) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                document.imageUris.forEach { uri ->
                    if (!uri.startsWith("http://") && !uri.startsWith("https://")) {
                        FileUtils.deleteFile(uri)
                    }
                }
                repository.deleteDocument(document)
                _error.value = null
                refreshDocuments()
            } catch (e: Exception) {
                _error.value = e.message ?: "문서 삭제 실패"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 문서 상세 캐시
    private val _documentCache = mutableMapOf<String, StateFlow<Document?>>()
    
    fun getDocumentById(id: String): StateFlow<Document?> {
        return _documentCache.getOrPut(id) {
            repository.getDocumentByIdFlow(id)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = null
                )
        }
    }
    
    fun clearDocumentCache(id: String) {
        _documentCache.remove(id)
    }
    
    fun incrementShareCount(documentId: String) {
        viewModelScope.launch {
            try {
                repository.incrementShareCount(documentId)
            } catch (e: Exception) {
                // Ignore error
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }

    // 문서 공유 기능 (다운로드 후 공유)
    fun shareDocument(context: Context, document: Document) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val filePaths = mutableListOf<String>()
                val appContainer = (getApplication() as com.fileshare.app.FileShareApplication).container
                val client = appContainer.okHttpClient
                
                // FileUtils가 사용하는 폴더와 동일한 곳에 저장
                val documentsDir = java.io.File(context.filesDir, "documents")
                if (!documentsDir.exists()) documentsDir.mkdirs()

                document.imageUris.forEachIndexed { index, uri ->
                    if (uri.startsWith("http")) {
                        // 원격 파일 다운로드
                        val request = okhttp3.Request.Builder().url(uri).build()
                        val response = client.newCall(request).execute()
                        if (response.isSuccessful) {
                            val bytes = response.body?.bytes()
                            if (bytes != null) {
                                val ext = if (uri.endsWith(".pdf", true)) "pdf" else "jpg"
                                val fileName = "share_${document.id}_${System.currentTimeMillis()}_$index.$ext"
                                val file = java.io.File(documentsDir, fileName)
                                java.io.FileOutputStream(file).use { it.write(bytes) }
                                filePaths.add(file.absolutePath)
                            }
                        }
                    } else {
                        // 로컬 파일
                        filePaths.add(uri)
                    }
                }
                
                if (filePaths.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        FileUtils.shareMultipleFiles(context, filePaths)
                        incrementShareCount(document.id)
                    }
                } else {
                    _error.value = "공유할 파일이 없습니다."
                }
            } catch (e: Exception) {
                 _error.value = "공유 준비 중 오류 발생: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
