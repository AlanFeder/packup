package com.packup.data.sync

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ConcurrentHashMap
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
    private val listeners = ConcurrentHashMap<String, ListenerRegistration>()
    private var familyId: String? = null
    private var localDeviceId: String = ""

    private val snapshotMutex = Mutex()
    private val listenerRetryDelays = ConcurrentHashMap<String, Long>()

    fun startListening(familyId: String) {
        stopListening()
        this.familyId = familyId
        listenerRetryDelays.clear()
        scope.launch {
            localDeviceId = devicePreferences.getDeviceId()
            listenToPackingItems(familyId)
            listenToCategories(familyId)
            listenToFamilyMembers(familyId)
            listenToMorningItems(familyId)
        }
    }

    fun stopListening() {
        listeners.values.forEach { it.remove() }
        listeners.clear()
    }

    // --- Push helpers ---

    private suspend fun pushWithRetry(
        description: String,
        maxRetries: Int = 3,
        block: suspend () -> Unit,
    ) {
        var attempt = 0
        while (true) {
            try {
                block()
                return
            } catch (e: Exception) {
                attempt++
                if (attempt > maxRetries) {
                    Log.e(TAG, "Push failed after $maxRetries retries: $description", e)
                    return
                }
                val delayMs = (1000L * (1 shl (attempt - 1))).coerceAtMost(4000L)
                Log.w(TAG, "Push attempt $attempt/$maxRetries failed: $description – retrying in ${delayMs}ms", e)
                delay(delayMs)
            }
        }
    }

    private fun requireFamilyId(caller: String): String? {
        val fid = familyId
        if (fid == null) Log.w(TAG, "$caller dropped: familyId is null")
        return fid
    }

    // --- Push methods ---

    fun pushPackingItem(item: PackingItemEntity) {
        val fid = requireFamilyId("pushPackingItem(${item.id})") ?: return
        scope.launch {
            pushWithRetry("packing_items/${item.id}") {
                firestore.collection("families").document(fid)
                    .collection("packing_items").document(item.id)
                    .set(packingItemData(item), SetOptions.merge())
                    .await()
            }
        }
    }

    fun pushCategory(item: CategoryEntity) {
        val fid = requireFamilyId("pushCategory(${item.id})") ?: return
        scope.launch {
            pushWithRetry("categories/${item.id}") {
                firestore.collection("families").document(fid)
                    .collection("categories").document(item.id)
                    .set(categoryData(item), SetOptions.merge())
                    .await()
            }
        }
    }

    fun pushFamilyMember(item: FamilyMemberEntity) {
        val fid = requireFamilyId("pushFamilyMember(${item.id})") ?: return
        scope.launch {
            pushWithRetry("family_members/${item.id}") {
                firestore.collection("families").document(fid)
                    .collection("family_members").document(item.id)
                    .set(familyMemberData(item), SetOptions.merge())
                    .await()
            }
        }
    }

    suspend fun pushFamilyMemberAndAwait(item: FamilyMemberEntity) {
        val fid = requireFamilyId("pushFamilyMemberAndAwait(${item.id})") ?: return
        pushWithRetry("family_members/${item.id} (await)") {
            firestore.collection("families").document(fid)
                .collection("family_members").document(item.id)
                .set(familyMemberData(item), SetOptions.merge())
                .await()
        }
    }

    fun pushMorningItem(item: MorningItemEntity) {
        val fid = requireFamilyId("pushMorningItem(${item.id})") ?: return
        scope.launch {
            pushWithRetry("morning_items/${item.id}") {
                firestore.collection("families").document(fid)
                    .collection("morning_items").document(item.id)
                    .set(morningItemData(item), SetOptions.merge())
                    .await()
            }
        }
    }

    fun pushDelete(collection: String, id: String) {
        val fid = requireFamilyId("pushDelete($collection/$id)") ?: return
        scope.launch {
            pushWithRetry("delete $collection/$id") {
                val data = mapOf(
                    "deleted" to true,
                    "updatedAt" to System.currentTimeMillis(),
                    "updatedByDeviceId" to localDeviceId,
                    "serverUpdatedAt" to FieldValue.serverTimestamp(),
                )
                firestore.collection("families").document(fid)
                    .collection(collection).document(id)
                    .set(data, SetOptions.merge())
                    .await()
            }
        }
    }

    suspend fun pushAllSeedData(
        categories: List<CategoryEntity>,
        familyMembers: List<FamilyMemberEntity>,
        packingItems: List<PackingItemEntity>,
        morningItems: List<MorningItemEntity>,
    ) {
        val fid = requireFamilyId("pushAllSeedData") ?: return
        val familyRef = firestore.collection("families").document(fid)

        data class BatchEntry(val collection: String, val docId: String, val data: Map<String, Any?>)

        val entries = mutableListOf<BatchEntry>()
        categories.forEach {
            entries.add(BatchEntry("categories", it.id, categoryData(it) + ("deleted" to false)))
        }
        familyMembers.forEach {
            entries.add(BatchEntry("family_members", it.id, familyMemberData(it) + ("deleted" to false)))
        }
        packingItems.forEach {
            entries.add(BatchEntry("packing_items", it.id, packingItemData(it) + ("deleted" to false)))
        }
        morningItems.forEach {
            entries.add(BatchEntry("morning_items", it.id, morningItemData(it) + ("deleted" to false)))
        }

        for (chunk in entries.chunked(FIRESTORE_BATCH_LIMIT)) {
            pushWithRetry("seed batch (${chunk.size} docs)") {
                val batch = firestore.batch()
                for (entry in chunk) {
                    batch.set(
                        familyRef.collection(entry.collection).document(entry.docId),
                        entry.data,
                        SetOptions.merge()
                    )
                }
                batch.commit().await()
            }
        }
    }

    // --- Data map builders ---
    // Risk 9: 'deleted' is intentionally omitted so merges cannot resurrect a soft-deleted doc.
    // Risk 10: serverUpdatedAt lets future conflict resolution use the Firestore server clock.

    private fun packingItemData(item: PackingItemEntity): Map<String, Any?> = mapOf(
        "name" to item.name,
        "category" to item.category,
        "status" to item.status.name,
        "memberId" to item.memberId,
        "sortOrder" to item.sortOrder,
        "createdAt" to item.createdAt,
        "updatedAt" to item.updatedAt,
        "updatedByDeviceId" to item.updatedByDeviceId,
        "serverUpdatedAt" to FieldValue.serverTimestamp(),
    )

    private fun categoryData(item: CategoryEntity): Map<String, Any?> = mapOf(
        "name" to item.name,
        "iconKey" to item.iconKey,
        "sortOrder" to item.sortOrder,
        "createdAt" to item.createdAt,
        "updatedAt" to item.updatedAt,
        "updatedByDeviceId" to item.updatedByDeviceId,
        "serverUpdatedAt" to FieldValue.serverTimestamp(),
    )

    private fun familyMemberData(item: FamilyMemberEntity): Map<String, Any?> = mapOf(
        "name" to item.name,
        "avatar" to item.avatar,
        "iconKey" to item.iconKey,
        "photoUri" to item.photoUri,
        "sortOrder" to item.sortOrder,
        "createdAt" to item.createdAt,
        "updatedAt" to item.updatedAt,
        "updatedByDeviceId" to item.updatedByDeviceId,
        "serverUpdatedAt" to FieldValue.serverTimestamp(),
    )

    private fun morningItemData(item: MorningItemEntity): Map<String, Any?> = mapOf(
        "name" to item.name,
        "category" to item.category,
        "status" to item.status.name,
        "sortOrder" to item.sortOrder,
        "createdAt" to item.createdAt,
        "updatedAt" to item.updatedAt,
        "updatedByDeviceId" to item.updatedByDeviceId,
        "serverUpdatedAt" to FieldValue.serverTimestamp(),
    )

    // --- Listeners ---

    private fun listenToPackingItems(familyId: String) {
        val collection = "packing_items"
        listeners[collection]?.remove()
        val reg = firestore.collection("families").document(familyId)
            .collection(collection)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleListenerError(collection, familyId, error)
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener
                listenerRetryDelays.remove(collection)

                scope.launch {
                    snapshotMutex.withLock {
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
            }
        listeners[collection] = reg
    }

    private fun listenToCategories(familyId: String) {
        val collection = "categories"
        listeners[collection]?.remove()
        val reg = firestore.collection("families").document(familyId)
            .collection(collection)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleListenerError(collection, familyId, error)
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener
                listenerRetryDelays.remove(collection)

                scope.launch {
                    snapshotMutex.withLock {
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
            }
        listeners[collection] = reg
    }

    private fun listenToFamilyMembers(familyId: String) {
        val collection = "family_members"
        listeners[collection]?.remove()
        val reg = firestore.collection("families").document(familyId)
            .collection(collection)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleListenerError(collection, familyId, error)
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener
                listenerRetryDelays.remove(collection)

                scope.launch {
                    snapshotMutex.withLock {
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
                                    (entity.photoUri.isNotEmpty() && entity.photoUri != local?.photoUri)
                                if (apply) {
                                    if (local == null) {
                                        familyMemberDao.insert(entity)
                                    } else {
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
            }
        listeners[collection] = reg
    }

    private fun listenToMorningItems(familyId: String) {
        val collection = "morning_items"
        listeners[collection]?.remove()
        val reg = firestore.collection("families").document(familyId)
            .collection(collection)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleListenerError(collection, familyId, error)
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener
                listenerRetryDelays.remove(collection)

                scope.launch {
                    snapshotMutex.withLock {
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
            }
        listeners[collection] = reg
    }

    private fun handleListenerError(collection: String, familyId: String, error: Exception) {
        Log.e(TAG, "$collection listener error", error)

        if (error is FirebaseFirestoreException &&
            error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
        ) {
            Log.e(TAG, "$collection listener permanently failed: PERMISSION_DENIED")
            return
        }

        val currentDelay = listenerRetryDelays.getOrDefault(collection, INITIAL_RETRY_DELAY_MS)
        listenerRetryDelays[collection] = (currentDelay * 2).coerceAtMost(MAX_RETRY_DELAY_MS)

        Log.w(TAG, "Scheduling $collection listener re-registration in ${currentDelay}ms")
        scope.launch {
            delay(currentDelay)
            if (this@FirestoreSyncManager.familyId == familyId) {
                when (collection) {
                    "packing_items" -> listenToPackingItems(familyId)
                    "categories" -> listenToCategories(familyId)
                    "family_members" -> listenToFamilyMembers(familyId)
                    "morning_items" -> listenToMorningItems(familyId)
                }
            }
        }
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
        private const val INITIAL_RETRY_DELAY_MS = 5_000L
        private const val MAX_RETRY_DELAY_MS = 60_000L
        private const val FIRESTORE_BATCH_LIMIT = 500
    }
}
