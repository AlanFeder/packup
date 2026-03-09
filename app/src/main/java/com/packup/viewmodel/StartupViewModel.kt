package com.packup.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.packup.data.auth.AuthManager
import com.packup.data.local.DevicePreferences
import com.packup.data.repository.PackingRepository
import com.packup.data.sync.FirestoreSyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AppStartupState {
    data object Loading : AppStartupState()
    data object NeedsFamilySetup : AppStartupState()
    data object Ready : AppStartupState()
    data class Error(val message: String) : AppStartupState()
}

@HiltViewModel
class StartupViewModel @Inject constructor(
    private val authManager: AuthManager,
    private val devicePreferences: DevicePreferences,
    private val syncManager: FirestoreSyncManager,
    private val repository: PackingRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<AppStartupState>(AppStartupState.Loading)
    val state: StateFlow<AppStartupState> = _state.asStateFlow()

    init {
        initialize()
    }

    private fun initialize() {
        viewModelScope.launch {
            try {
                authManager.ensureSignedIn()

                val familyId = devicePreferences.getFamilyId()
                if (familyId == null) {
                    _state.value = AppStartupState.NeedsFamilySetup
                } else {
                    syncManager.startListening(familyId)
                    repository.seedIfEmpty()
                    _state.value = AppStartupState.Ready
                }
            } catch (e: Exception) {
                _state.value = AppStartupState.Error(e.message ?: "Startup failed")
            }
        }
    }

    fun onFamilySetupComplete() {
        _state.value = AppStartupState.Ready
    }

    fun retry() {
        _state.value = AppStartupState.Loading
        initialize()
    }
}
