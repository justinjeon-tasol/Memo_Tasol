package com.fileshare.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fileshare.app.data.local.AppDatabase
import com.fileshare.app.data.repository.CategoryRepository
import com.fileshare.app.domain.model.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CategoryViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: CategoryRepository
    
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()
    
    init {
        val database = AppDatabase.getInstance(application)
        repository = CategoryRepository(database.categoryDao())
        loadCategories()
    }
    
    private fun loadCategories() {
        viewModelScope.launch {
            repository.getAllCategories().collect { categories ->
                _categories.value = categories
            }
        }
    }
    
    fun addCategory(name: String) {
        viewModelScope.launch {
            val maxOrder = _categories.value.maxOfOrNull { it.displayOrder } ?: 0
            val newCategory = Category(
                name = name,
                displayOrder = maxOrder + 1,
                isDefault = false
            )
            repository.insertCategory(newCategory)
        }
    }
    
    fun updateCategory(category: Category) {
        viewModelScope.launch {
            repository.updateCategory(category)
        }
    }
    
    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            if (!category.isDefault) {
                repository.deleteCategory(category)
            }
        }
    }
    
    fun updateCategoryOrder(categories: List<Category>) {
        viewModelScope.launch {
            val reorderedCategories = categories.mapIndexed { index, category ->
                category.copy(displayOrder = index)
            }
            repository.updateDisplayOrders(reorderedCategories)
            _categories.value = reorderedCategories.sortedBy { it.displayOrder }
        }
    }
    
    fun getCategoryById(id: Long): Category? {
        return _categories.value.find { it.id == id }
    }
}
