package com.packup.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.packup.data.auth.AuthManager
import com.packup.data.local.DevicePreferences
import com.packup.data.repository.PackingRepository
import com.packup.data.sync.FirestoreSyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.SecureRandom
import javax.inject.Inject

sealed class FamilySetupState {
    data object Idle : FamilySetupState()
    data object Loading : FamilySetupState()
    data class Created(val familyId: String) : FamilySetupState()
    data object Joined : FamilySetupState()
    data class Error(val message: String) : FamilySetupState()
}

@HiltViewModel
class FamilySetupViewModel @Inject constructor(
    private val authManager: AuthManager,
    private val devicePreferences: DevicePreferences,
    private val firestore: FirebaseFirestore,
    private val syncManager: FirestoreSyncManager,
    private val repository: PackingRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<FamilySetupState>(FamilySetupState.Idle)
    val state: StateFlow<FamilySetupState> = _state.asStateFlow()

    fun createFamily() {
        viewModelScope.launch {
            _state.value = FamilySetupState.Loading
            try {
                val user = authManager.ensureSignedIn()
                val familyId = generateFamilyId()

                val familyRef = firestore.collection("families").document(familyId)
                val batch = firestore.batch()
                batch.set(familyRef, mapOf(
                    "seededAt" to System.currentTimeMillis(),
                    "schemaVersion" to 1,
                ))
                batch.set(familyRef.collection("members").document(user.uid), mapOf(
                    "createdAt" to System.currentTimeMillis(),
                    "deviceName" to android.os.Build.MODEL,
                ))
                batch.commit().await()

                devicePreferences.setFamilyId(familyId)
                syncManager.startListening(familyId)
                repository.seedAndPush()

                _state.value = FamilySetupState.Created(familyId)
            } catch (e: Exception) {
                _state.value = FamilySetupState.Error(e.message ?: "Failed to create family")
            }
        }
    }

    fun joinFamily(familyId: String) {
        val trimmed = familyId.trim().uppercase()
        if (trimmed.length < 20) {
            _state.value = FamilySetupState.Error("Family code is too short")
            return
        }
        viewModelScope.launch {
            _state.value = FamilySetupState.Loading
            try {
                val user = authManager.ensureSignedIn()

                val metaDoc = firestore.collection("families").document(trimmed)
                    .get().await()
                if (!metaDoc.exists()) {
                    _state.value = FamilySetupState.Error("Family not found. Check the code and try again.")
                    return@launch
                }

                firestore.collection("families").document(trimmed)
                    .collection("members").document(user.uid)
                    .set(mapOf(
                        "createdAt" to System.currentTimeMillis(),
                        "deviceName" to android.os.Build.MODEL,
                    )).await()

                devicePreferences.setFamilyId(trimmed)
                syncManager.startListening(trimmed)

                _state.value = FamilySetupState.Joined
            } catch (e: Exception) {
                _state.value = FamilySetupState.Error(e.message ?: "Failed to join family")
            }
        }
    }

    fun clearError() {
        _state.value = FamilySetupState.Idle
    }

    private fun generateFamilyId(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02X".format(it) }
    }
}
