package com.fileshare.app.domain.model

data class Category(
    val id: Long = 0,
    val name: String,
    val displayOrder: Int,
    val isDefault: Boolean = false
)
