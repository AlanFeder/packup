package com.packup.data.sync

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.packup.data.local.DevicePreferences
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoStorageManager @Inject constructor(
    private val storage: FirebaseStorage,
    private val devicePreferences: DevicePreferences,
) {

    /**
     * Uploads a member photo to Firebase Storage and returns the download URL.
     * On failure (e.g. Storage not enabled), returns local path so the app doesn't crash.
     */
    suspend fun uploadMemberPhoto(memberId: String, localFilePath: String): String {
        val familyId = devicePreferences.getFamilyId() ?: return localFilePath
        val file = File(localFilePath)
        if (!file.exists()) throw IllegalArgumentException("File not found: $localFilePath")

        val ref = storage.reference
            .child("families").child(familyId)
            .child("member_photos").child("$memberId.jpg")

        return try {
            file.inputStream().use { ref.putStream(it).await() }
            ref.getDownloadUrl().await().toString()
        } catch (e: StorageException) {
            localFilePath
        }
    }
}
