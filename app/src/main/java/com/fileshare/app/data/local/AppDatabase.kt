package com.fileshare.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fileshare.app.data.local.dao.CategoryDao
import com.fileshare.app.data.local.dao.DocumentDao
import com.fileshare.app.data.local.dao.DocumentImageDao
import com.fileshare.app.data.local.entity.CategoryEntity
import com.fileshare.app.data.local.entity.DocumentEntity
import com.fileshare.app.data.local.entity.DocumentImageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        DocumentEntity::class,
        CategoryEntity::class,
        DocumentImageEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun documentDao(): DocumentDao
    abstract fun categoryDao(): CategoryDao
    abstract fun documentImageDao(): DocumentImageDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fileshare_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Initialize default categories in background thread
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    initializeDefaultCategories(database.categoryDao())
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
        
        private suspend fun initializeDefaultCategories(categoryDao: CategoryDao) {
            // Check if categories already exist
            if (categoryDao.getCategoryCount() == 0) {
                val defaultCategories = listOf(
                    CategoryEntity(
                        name = "통장사본",
                        displayOrder = 0,
                        isDefault = true,
                        createdAt = System.currentTimeMillis()
                    ),
                    CategoryEntity(
                        name = "사업자등록증",
                        displayOrder = 1,
                        isDefault = true,
                        createdAt = System.currentTimeMillis()
                    ),
                    CategoryEntity(
                        name = "제품사진",
                        displayOrder = 2,
                        isDefault = true,
                        createdAt = System.currentTimeMillis()
                    ),
                    CategoryEntity(
                        name = "기타",
                        displayOrder = 3,
                        isDefault = true,
                        createdAt = System.currentTimeMillis()
                    )
                )
                categoryDao.insertCategories(defaultCategories)
            }
        }
    }
}
