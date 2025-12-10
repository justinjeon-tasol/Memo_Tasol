package com.fileshare.app.data.repository

import com.fileshare.app.data.local.dao.CategoryDao
import com.fileshare.app.data.local.entity.CategoryEntity
import com.fileshare.app.domain.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepository(private val categoryDao: CategoryDao) {
    
    fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    suspend fun getCategoryById(id: Long): Category? {
        return categoryDao.getCategoryById(id)?.toDomain()
    }
    
    fun getCategoryByIdFlow(id: Long): Flow<Category?> {
        return categoryDao.getCategoryByIdFlow(id).map { it?.toDomain() }
    }
    
    suspend fun insertCategory(category: Category): Long {
        val entity = CategoryEntity.fromDomain(category)
        return categoryDao.insertCategory(entity)
    }
    
    suspend fun updateCategory(category: Category) {
        val entity = CategoryEntity.fromDomain(category)
        categoryDao.updateCategory(entity)
    }
    
    suspend fun deleteCategory(category: Category) {
        if (!category.isDefault) {
            val entity = CategoryEntity.fromDomain(category)
            categoryDao.deleteCategory(entity)
        }
    }
    
    suspend fun deleteCategoryById(id: Long) {
        categoryDao.deleteCategoryById(id)
    }
    
    suspend fun updateDisplayOrders(categories: List<Category>) {
        val entities = categories.map { CategoryEntity.fromDomain(it) }
        categoryDao.updateDisplayOrders(entities)
    }
    
    suspend fun getCategoryCount(): Int {
        return categoryDao.getCategoryCount()
    }
}
