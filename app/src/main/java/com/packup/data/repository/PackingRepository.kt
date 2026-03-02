package com.packup.data.repository

import com.packup.data.local.dao.CategoryDao
import com.packup.data.local.dao.FamilyMemberDao
import com.packup.data.local.dao.MorningItemDao
import com.packup.data.local.dao.PackingItemDao
import com.packup.data.local.entity.CategoryEntity
import com.packup.data.local.entity.FamilyMemberEntity
import com.packup.data.local.entity.ItemStatus
import com.packup.data.local.entity.MorningItemEntity
import com.packup.data.local.entity.MorningItemStatus
import com.packup.data.local.entity.PackingItemEntity
import com.packup.data.seed.SeedData
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PackingRepository @Inject constructor(
    private val familyMemberDao: FamilyMemberDao,
    private val packingItemDao: PackingItemDao,
    private val morningItemDao: MorningItemDao,
    private val categoryDao: CategoryDao,
) {
    val allMembers: Flow<List<FamilyMemberEntity>> = familyMemberDao.getAllMembers()
    val allPackingItems: Flow<List<PackingItemEntity>> = packingItemDao.getAllItems()
    val snoozedItems: Flow<List<PackingItemEntity>> = packingItemDao.getSnoozedItems()
    val allMorningItems: Flow<List<MorningItemEntity>> = morningItemDao.getAllItems()
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    fun itemsForMember(memberId: String): Flow<List<PackingItemEntity>> =
        packingItemDao.getItemsForMember(memberId)

    // --- Packing item operations ---

    suspend fun toggleItemDone(itemId: String, currentStatus: ItemStatus) {
        val newStatus = if (currentStatus == ItemStatus.DONE) ItemStatus.TODO else ItemStatus.DONE
        packingItemDao.updateStatus(itemId, newStatus)
    }

    suspend fun snoozeItem(itemId: String) {
        packingItemDao.updateStatus(itemId, ItemStatus.SNOOZED)
    }

    suspend fun unsnoozeItem(itemId: String) {
        packingItemDao.updateStatus(itemId, ItemStatus.TODO)
    }

    suspend fun toggleSnoozedDone(itemId: String, currentStatus: ItemStatus) {
        val newStatus = if (currentStatus == ItemStatus.DONE) ItemStatus.SNOOZED else ItemStatus.DONE
        packingItemDao.updateStatus(itemId, newStatus)
    }

    suspend fun addItem(name: String, category: String, memberId: String) {
        val item = PackingItemEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            category = category,
            memberId = memberId,
            sortOrder = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        )
        packingItemDao.insert(item)
    }

    suspend fun editItem(itemId: String, newName: String) {
        packingItemDao.updateName(itemId, newName)
    }

    suspend fun deleteItem(itemId: String) {
        packingItemDao.delete(itemId)
    }

    suspend fun markAllDoneInCategory(memberId: String, category: String) {
        packingItemDao.markAllDoneInCategory(memberId, category)
    }

    // --- Morning item operations ---

    suspend fun toggleMorningItemDone(itemId: String, currentStatus: MorningItemStatus) {
        val newStatus = if (currentStatus == MorningItemStatus.DONE) MorningItemStatus.TODO else MorningItemStatus.DONE
        morningItemDao.updateStatus(itemId, newStatus)
    }

    suspend fun addMorningItem(name: String, category: String = "General") {
        val item = MorningItemEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            category = category,
            sortOrder = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        )
        morningItemDao.insert(item)
    }

    suspend fun editMorningItem(itemId: String, newName: String) {
        morningItemDao.updateName(itemId, newName)
    }

    suspend fun deleteMorningItem(itemId: String) {
        morningItemDao.delete(itemId)
    }

    // --- Family member operations ---

    suspend fun addMember(name: String, iconKey: String) {
        val avatar = name.take(1).uppercase()
        val member = FamilyMemberEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            avatar = avatar,
            iconKey = iconKey,
            sortOrder = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        )
        familyMemberDao.insert(member)
    }

    suspend fun renameMember(id: String, name: String) {
        val avatar = name.take(1).uppercase()
        familyMemberDao.updateName(id, name, avatar)
    }

    suspend fun setMemberIcon(id: String, iconKey: String) {
        familyMemberDao.updateIcon(id, iconKey)
    }

    suspend fun setMemberPhoto(id: String, photoUri: String) {
        familyMemberDao.updatePhotoUri(id, photoUri)
    }

    suspend fun deleteMember(id: String) {
        familyMemberDao.delete(id)
        packingItemDao.deleteByMember(id)
    }

    // --- Category operations ---

    suspend fun addCategory(name: String, iconKey: String) {
        val entity = CategoryEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            iconKey = iconKey,
            sortOrder = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        )
        categoryDao.insert(entity)
    }

    suspend fun renameCategory(oldName: String, newName: String) {
        categoryDao.rename(oldName, newName)
        packingItemDao.updateCategory(oldName, newName)
        morningItemDao.updateCategory(oldName, newName)
    }

    suspend fun deleteCategory(name: String) {
        categoryDao.delete(name)
        packingItemDao.updateCategory(name, "General")
        morningItemDao.updateCategory(name, "General")
    }

    suspend fun setCategoryIcon(name: String, iconKey: String) {
        categoryDao.setIcon(name, iconKey)
    }

    // --- Seed / reset ---

    suspend fun seedIfEmpty() {
        if (categoryDao.count() == 0) {
            categoryDao.insertAll(SeedData.categories)
            familyMemberDao.insertAll(SeedData.familyMembers)
            packingItemDao.insertAll(SeedData.packingItems)
            morningItemDao.insertAll(SeedData.morningItems)
        }
    }

    suspend fun resetAll() {
        packingItemDao.resetAllStatuses()
        morningItemDao.resetAllStatuses()
    }
}
