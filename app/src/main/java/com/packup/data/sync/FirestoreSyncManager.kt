package com.packup.data.sync

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.packup.data.local.DevicePreferences
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreSyncManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val packingItemDao: PackingItemDao,
    private val categoryDao: CategoryDao,
    private val familyMemberDao: FamilyMemberDao,
    private val morningItemDao: MorningItemDao,
    private val devicePreferences: DevicePreferences,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val listeners = mutableListOf<ListenerRegistration>()
    private var familyId: String? = null
    private var localDeviceId: String = ""

    fun startListening(familyId: String) {
        stopListening()
        this.familyId = familyId
        scope.launch {
            localDeviceId = devicePreferences.getDeviceId()
            listenToPackingItems(familyId)
            listenToCategories(familyId)
            listenToFamilyMembers(familyId)
            listenToMorningItems(familyId)
        }
    }

    fun stopListening() {
        listeners.forEach { it.remove() }
        listeners.clear()
    }

    // --- Push methods ---

    fun pushPackingItem(item: PackingItemEntity) {
        val fid = familyId ?: return
        scope.launch {
            val data = mapOf(
                "name" to item.name,
                "category" to item.category,
                "status" to item.status.name,
                "memberId" to item.memberId,
                "sortOrder" to item.sortOrder,
                "createdAt" to item.createdAt,
                "updatedAt" to item.updatedAt,
                "updatedByDeviceId" to item.updatedByDeviceId,
                "deleted" to false,
            )
            firestore.collection("families").document(fid)
                .collection("packing_items").document(item.id)
                .set(data, SetOptions.merge())
        }
    }

    fun pushCategory(item: CategoryEntity) {
        val fid = familyId ?: return
        scope.launch {
            val data = mapOf(
                "name" to item.name,
                "iconKey" to item.iconKey,
                "sortOrder" to item.sortOrder,
                "createdAt" to item.createdAt,
                "updatedAt" to item.updatedAt,
                "updatedByDeviceId" to item.updatedByDeviceId,
                "deleted" to false,
            )
            firestore.collection("families").document(fid)
                .collection("categories").document(item.id)
                .set(data, SetOptions.merge())
        }
    }

    fun pushFamilyMember(item: FamilyMemberEntity) {
        val fid = familyId ?: return
        scope.launch {
            val data = mapOf(
                "name" to item.name,
                "avatar" to item.avatar,
                "iconKey" to item.iconKey,
                "photoUri" to item.photoUri,
                "sortOrder" to item.sortOrder,
                "createdAt" to item.createdAt,
                "updatedAt" to item.updatedAt,
                "updatedByDeviceId" to item.updatedByDeviceId,
                "deleted" to false,
            )
            firestore.collection("families").document(fid)
                .collection("family_members").document(item.id)
                .set(data, SetOptions.merge())
        }
    }

    /** Suspend version so caller can ensure the write completes (e.g. after photo upload). */
    suspend fun pushFamilyMemberAndAwait(item: FamilyMemberEntity) {
        val fid = familyId ?: return
        val data = mapOf(
            "name" to item.name,
            "avatar" to item.avatar,
            "iconKey" to item.iconKey,
            "photoUri" to item.photoUri,
            "sortOrder" to item.sortOrder,
            "createdAt" to item.createdAt,
            "updatedAt" to item.updatedAt,
            "updatedByDeviceId" to item.updatedByDeviceId,
            "deleted" to false,
        )
        firestore.collection("families").document(fid)
            .collection("family_members").document(item.id)
            .set(data, SetOptions.merge())
            .await()
    }

    fun pushMorningItem(item: MorningItemEntity) {
        val fid = familyId ?: return
        scope.launch {
            val data = mapOf(
                "name" to item.name,
                "category" to item.category,
                "status" to item.status.name,
                "sortOrder" to item.sortOrder,
                "createdAt" to item.createdAt,
                "updatedAt" to item.updatedAt,
                "updatedByDeviceId" to item.updatedByDeviceId,
                "deleted" to false,
            )
            firestore.collection("families").document(fid)
                .collection("morning_items").document(item.id)
                .set(data, SetOptions.merge())
        }
    }

    fun pushDelete(collection: String, id: String) {
        val fid = familyId ?: return
        scope.launch {
            val data = mapOf(
                "deleted" to true,
                "updatedAt" to System.currentTimeMillis(),
                "updatedByDeviceId" to localDeviceId,
            )
            firestore.collection("families").document(fid)
                .collection(collection).document(id)
                .set(data, SetOptions.merge())
        }
    }

    fun pushAllSeedData(
        categories: List<CategoryEntity>,
        familyMembers: List<FamilyMemberEntity>,
        packingItems: List<PackingItemEntity>,
        morningItems: List<MorningItemEntity>,
    ) {
        val fid = familyId ?: return
        scope.launch {
            categories.forEach { pushCategory(it) }
            familyMembers.forEach { pushFamilyMember(it) }
            packingItems.forEach { pushPackingItem(it) }
            morningItems.forEach { pushMorningItem(it) }
        }
    }

    // --- Listeners ---

    private fun listenToPackingItems(familyId: String) {
        val reg = firestore.collection("families").document(familyId)
            .collection("packing_items")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "packing_items listener error", error)
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener

                scope.launch {
                    for (change in snapshot.documentChanges) {
                        val doc = change.document
                        if (doc.metadata.hasPendingWrites()) continue

                        val deleted = doc.getBoolean("deleted") ?: false
                        if (deleted) {
                            packingItemDao.delete(doc.id)
                        } else {
                            val entity = doc.toPackingItem() ?: continue
                            if (shouldApplyRemote(
                                    remoteUpdatedAt = entity.updatedAt,
                                    remoteDeviceId = entity.updatedByDeviceId,
                                    localUpdatedAt = packingItemDao.getById(doc.id)?.updatedAt,
                                    localDeviceId = localDeviceId
                                )
                            ) {
                                packingItemDao.insert(entity)
                            }
                        }
                    }
                }
            }
        listeners.add(reg)
    }

    private fun listenToCategories(familyId: String) {
        val reg = firestore.collection("families").document(familyId)
            .collection("categories")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "categories listener error", error)
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener

                scope.launch {
                    for (change in snapshot.documentChanges) {
                        val doc = change.document
                        if (doc.metadata.hasPendingWrites()) continue

                        val deleted = doc.getBoolean("deleted") ?: false
                        if (deleted) {
                            categoryDao.deleteById(doc.id)
                        } else {
                            val entity = doc.toCategory() ?: continue
                            if (shouldApplyRemote(
                                    remoteUpdatedAt = entity.updatedAt,
                                    remoteDeviceId = entity.updatedByDeviceId,
                                    localUpdatedAt = categoryDao.getById(doc.id)?.updatedAt,
                                    localDeviceId = localDeviceId
                                )
                            ) {
                                categoryDao.insert(entity)
                            }
                        }
                    }
                }
            }
        listeners.add(reg)
    }

    private fun listenToFamilyMembers(familyId: String) {
        val reg = firestore.collection("families").document(familyId)
            .collection("family_members")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "family_members listener error", error)
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener

                scope.launch {
                    for (change in snapshot.documentChanges) {
                        val doc = change.document
                        if (doc.metadata.hasPendingWrites()) continue

                        val deleted = doc.getBoolean("deleted") ?: false
                        if (deleted) {
                            familyMemberDao.delete(doc.id)
                        } else {
                            val entity = doc.toFamilyMember() ?: continue
                            val local = familyMemberDao.getById(doc.id)
                            val apply = shouldApplyRemote(
                                    remoteUpdatedAt = entity.updatedAt,
                                    remoteDeviceId = entity.updatedByDeviceId,
                                    localUpdatedAt = local?.updatedAt,
                                    localDeviceId = localDeviceId
                                ) ||
                                // Always apply when remote has a different photo (sync photo across devices)
                                (entity.photoUri.isNotEmpty() && entity.photoUri != local?.photoUri)
                            if (apply) {
                                if (local == null) {
                                    familyMemberDao.insert(entity)
                                } else {
                                    // Update in place so we don't trigger FK CASCADE and wipe packing_items
                                    familyMemberDao.update(
                                        id = entity.id,
                                        name = entity.name,
                                        avatar = entity.avatar,
                                        iconKey = entity.iconKey,
                                        photoUri = entity.photoUri,
                                        sortOrder = entity.sortOrder,
                                        createdAt = entity.createdAt,
                                        updatedAt = entity.updatedAt,
                                        updatedByDeviceId = entity.updatedByDeviceId
                                    )
                                }
                            }
                        }
                    }
                }
            }
        listeners.add(reg)
    }

    private fun listenToMorningItems(familyId: String) {
        val reg = firestore.collection("families").document(familyId)
            .collection("morning_items")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "morning_items listener error", error)
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener

                scope.launch {
                    for (change in snapshot.documentChanges) {
                        val doc = change.document
                        if (doc.metadata.hasPendingWrites()) continue

                        val deleted = doc.getBoolean("deleted") ?: false
                        if (deleted) {
                            morningItemDao.delete(doc.id)
                        } else {
                            val entity = doc.toMorningItem() ?: continue
                            if (shouldApplyRemote(
                                    remoteUpdatedAt = entity.updatedAt,
                                    remoteDeviceId = entity.updatedByDeviceId,
                                    localUpdatedAt = morningItemDao.getById(doc.id)?.updatedAt,
                                    localDeviceId = localDeviceId
                                )
                            ) {
                                morningItemDao.insert(entity)
                            }
                        }
                    }
                }
            }
        listeners.add(reg)
    }

    // --- Helpers ---

    private fun shouldApplyRemote(
        remoteUpdatedAt: Long,
        remoteDeviceId: String,
        localUpdatedAt: Long?,
        localDeviceId: String,
    ): Boolean {
        if (localUpdatedAt == null) return true
        if (remoteUpdatedAt > localUpdatedAt) return true
        if (remoteUpdatedAt == localUpdatedAt && remoteDeviceId > localDeviceId) return true
        return false
    }

    private fun DocumentSnapshot.toPackingItem(): PackingItemEntity? = try {
        PackingItemEntity(
            id = id,
            name = getString("name") ?: "",
            category = getString("category") ?: "General",
            status = try { ItemStatus.valueOf(getString("status") ?: "TODO") } catch (_: Exception) { ItemStatus.TODO },
            memberId = getString("memberId") ?: "",
            sortOrder = (getLong("sortOrder") ?: 0).toInt(),
            createdAt = getLong("createdAt") ?: System.currentTimeMillis(),
            updatedAt = getLong("updatedAt") ?: 0L,
            updatedByDeviceId = getString("updatedByDeviceId") ?: "",
        )
    } catch (e: Exception) {
        Log.e(TAG, "Failed to parse packing item $id", e)
        null
    }

    private fun DocumentSnapshot.toCategory(): CategoryEntity? = try {
        CategoryEntity(
            id = id,
            name = getString("name") ?: "",
            iconKey = getString("iconKey") ?: "package",
            sortOrder = (getLong("sortOrder") ?: 0).toInt(),
            createdAt = getLong("createdAt") ?: System.currentTimeMillis(),
            updatedAt = getLong("updatedAt") ?: 0L,
            updatedByDeviceId = getString("updatedByDeviceId") ?: "",
        )
    } catch (e: Exception) {
        Log.e(TAG, "Failed to parse category $id", e)
        null
    }

    private fun DocumentSnapshot.toFamilyMember(): FamilyMemberEntity? = try {
        FamilyMemberEntity(
            id = id,
            name = getString("name") ?: "",
            avatar = getString("avatar") ?: "",
            iconKey = getString("iconKey") ?: "",
            photoUri = getString("photoUri") ?: "",
            sortOrder = (getLong("sortOrder") ?: 0).toInt(),
            createdAt = getLong("createdAt") ?: System.currentTimeMillis(),
            updatedAt = getLong("updatedAt") ?: 0L,
            updatedByDeviceId = getString("updatedByDeviceId") ?: "",
        )
    } catch (e: Exception) {
        Log.e(TAG, "Failed to parse family member $id", e)
        null
    }

    private fun DocumentSnapshot.toMorningItem(): MorningItemEntity? = try {
        MorningItemEntity(
            id = id,
            name = getString("name") ?: "",
            category = getString("category") ?: "General",
            status = try { MorningItemStatus.valueOf(getString("status") ?: "TODO") } catch (_: Exception) { MorningItemStatus.TODO },
            sortOrder = (getLong("sortOrder") ?: 0).toInt(),
            createdAt = getLong("createdAt") ?: System.currentTimeMillis(),
            updatedAt = getLong("updatedAt") ?: 0L,
            updatedByDeviceId = getString("updatedByDeviceId") ?: "",
        )
    } catch (e: Exception) {
        Log.e(TAG, "Failed to parse morning item $id", e)
        null
    }

    companion object {
        private const val TAG = "FirestoreSync"
    }
}
