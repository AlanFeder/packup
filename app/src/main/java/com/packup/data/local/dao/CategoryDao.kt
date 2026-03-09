package com.packup.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.packup.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY sortOrder ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY sortOrder ASC")
    suspend fun getAllCategoriesSync(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: String): CategoryEntity?

    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Query("UPDATE categories SET name = :newName, updatedAt = :now, updatedByDeviceId = :deviceId WHERE name = :oldName")
    suspend fun rename(oldName: String, newName: String, now: Long = System.currentTimeMillis(), deviceId: String = "")

    @Query("UPDATE categories SET iconKey = :iconKey, updatedAt = :now, updatedByDeviceId = :deviceId WHERE name = :name")
    suspend fun setIcon(name: String, iconKey: String, now: Long = System.currentTimeMillis(), deviceId: String = "")

    @Query("DELETE FROM categories WHERE name = :name")
    suspend fun delete(name: String)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM categories")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int
}
