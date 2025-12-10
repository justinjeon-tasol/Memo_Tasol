package com.fileshare.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fileshare.app.data.local.AppDatabase
import com.fileshare.app.data.repository.DocumentRepository
import com.fileshare.app.domain.model.Document
import com.fileshare.app.util.FileUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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
                // 문서 추가 후 목록 새로고침
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
                // 문서 수정 후 목록 및 캐시 새로고침
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
                // Delete files from storage (로컬 파일만 삭제)
                document.imageUris.forEach { uri ->
                    if (!uri.startsWith("http://") && !uri.startsWith("https://")) {
                        FileUtils.deleteFile(uri)
                    }
                }
                // Delete from database
                repository.deleteDocument(document)
                _error.value = null
                // 문서 삭제 후 목록 새로고침
                refreshDocuments()
            } catch (e: Exception) {
                _error.value = e.message ?: "문서 삭제 실패"
            } finally {
                _isLoading.value = false
            }
        }
    }
    // 문서 상세 캐시 - recomposition 시 API 중복 호출 방지
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
                // Log error but don't show to user
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
