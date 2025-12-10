package com.fileshare.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fileshare.app.domain.model.Category

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val displayOrder: Int,
    val isDefault: Boolean = false,
    val createdAt: Long
) {
    fun toDomain(): Category {
        return Category(
            id = id,
            name = name,
            displayOrder = displayOrder,
            isDefault = isDefault
        )
    }

    companion object {
        fun fromDomain(category: Category): CategoryEntity {
            return CategoryEntity(
                id = category.id,
                name = category.name,
                displayOrder = category.displayOrder,
                isDefault = category.isDefault,
                createdAt = System.currentTimeMillis()
            )
        }
    }
}
