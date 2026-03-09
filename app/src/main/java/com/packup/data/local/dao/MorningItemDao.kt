package com.packup.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.packup.data.local.entity.MorningItemEntity
import com.packup.data.local.entity.MorningItemStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface MorningItemDao {
    @Query("SELECT * FROM morning_items ORDER BY sortOrder ASC")
    fun getAllItems(): Flow<List<MorningItemEntity>>

    @Query("SELECT * FROM morning_items ORDER BY sortOrder ASC")
    suspend fun getAllItemsSync(): List<MorningItemEntity>

    @Query("SELECT * FROM morning_items WHERE id = :id")
    suspend fun getById(id: String): MorningItemEntity?

    @Query("SELECT * FROM morning_items WHERE category = :category")
    suspend fun getItemsByCategory(category: String): List<MorningItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: MorningItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<MorningItemEntity>)

    @Query("UPDATE morning_items SET status = :status, updatedAt = :now, updatedByDeviceId = :deviceId WHERE id = :itemId")
    suspend fun updateStatus(itemId: String, status: MorningItemStatus, now: Long = System.currentTimeMillis(), deviceId: String = "")

    @Query("UPDATE morning_items SET name = :name, updatedAt = :now, updatedByDeviceId = :deviceId WHERE id = :itemId")
    suspend fun updateName(itemId: String, name: String, now: Long = System.currentTimeMillis(), deviceId: String = "")

    @Query("UPDATE morning_items SET category = :newCategory, updatedAt = :now, updatedByDeviceId = :deviceId WHERE category = :oldCategory")
    suspend fun updateCategory(oldCategory: String, newCategory: String, now: Long = System.currentTimeMillis(), deviceId: String = "")

    @Query("DELETE FROM morning_items WHERE id = :itemId")
    suspend fun delete(itemId: String)

    @Query("UPDATE morning_items SET status = 'TODO', updatedAt = :now, updatedByDeviceId = :deviceId")
    suspend fun resetAllStatuses(now: Long = System.currentTimeMillis(), deviceId: String = "")

    @Query("DELETE FROM morning_items")
    suspend fun deleteAll()
}
