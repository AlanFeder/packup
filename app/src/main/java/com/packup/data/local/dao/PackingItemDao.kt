package com.packup.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.packup.data.local.entity.ItemStatus
import com.packup.data.local.entity.PackingItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PackingItemDao {
    @Query("SELECT * FROM packing_items WHERE memberId = :memberId ORDER BY sortOrder ASC")
    fun getItemsForMember(memberId: String): Flow<List<PackingItemEntity>>

    @Query("SELECT * FROM packing_items ORDER BY sortOrder ASC")
    fun getAllItems(): Flow<List<PackingItemEntity>>

    @Query("SELECT * FROM packing_items ORDER BY sortOrder ASC")
    suspend fun getAllItemsSync(): List<PackingItemEntity>

    @Query("SELECT * FROM packing_items WHERE status = 'SNOOZED' ORDER BY memberId, sortOrder ASC")
    fun getSnoozedItems(): Flow<List<PackingItemEntity>>

    @Query("SELECT * FROM packing_items WHERE id = :id")
    suspend fun getById(id: String): PackingItemEntity?

    @Query("SELECT * FROM packing_items WHERE memberId = :memberId AND category = :category")
    suspend fun getItemsForMemberAndCategory(memberId: String, category: String): List<PackingItemEntity>

    @Query("SELECT * FROM packing_items WHERE category = :category")
    suspend fun getItemsByCategory(category: String): List<PackingItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PackingItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PackingItemEntity>)

    @Query("UPDATE packing_items SET status = :status, updatedAt = :now, updatedByDeviceId = :deviceId WHERE id = :itemId")
    suspend fun updateStatus(itemId: String, status: ItemStatus, now: Long = System.currentTimeMillis(), deviceId: String = "")

    @Query("UPDATE packing_items SET name = :name, updatedAt = :now, updatedByDeviceId = :deviceId WHERE id = :itemId")
    suspend fun updateName(itemId: String, name: String, now: Long = System.currentTimeMillis(), deviceId: String = "")

    @Query("UPDATE packing_items SET category = :newCategory, updatedAt = :now, updatedByDeviceId = :deviceId WHERE category = :oldCategory")
    suspend fun updateCategory(oldCategory: String, newCategory: String, now: Long = System.currentTimeMillis(), deviceId: String = "")

    @Query("UPDATE packing_items SET status = :status, updatedAt = :now, updatedByDeviceId = :deviceId WHERE memberId = :memberId AND category = :category AND status = 'TODO'")
    suspend fun markAllDoneInCategory(memberId: String, category: String, status: ItemStatus = ItemStatus.DONE, now: Long = System.currentTimeMillis(), deviceId: String = "")

    @Query("DELETE FROM packing_items WHERE id = :itemId")
    suspend fun delete(itemId: String)

    @Query("DELETE FROM packing_items WHERE memberId = :memberId")
    suspend fun deleteByMember(memberId: String)

    @Query("UPDATE packing_items SET status = 'TODO', updatedAt = :now, updatedByDeviceId = :deviceId")
    suspend fun resetAllStatuses(now: Long = System.currentTimeMillis(), deviceId: String = "")

    @Query("DELETE FROM packing_items")
    suspend fun deleteAll()
}
