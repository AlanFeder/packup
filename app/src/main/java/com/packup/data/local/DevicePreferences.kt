package com.packup.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DevicePreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val deviceIdKey = stringPreferencesKey("device_id")
    private val familyIdKey = stringPreferencesKey("family_id")

    val familyIdFlow: Flow<String?> = dataStore.data.map { it[familyIdKey] }

    suspend fun getDeviceId(): String {
        val existing = dataStore.data.first()[deviceIdKey]
        if (existing != null) return existing
        val newId = UUID.randomUUID().toString()
        dataStore.edit { it[deviceIdKey] = newId }
        return newId
    }

    suspend fun getFamilyId(): String? = dataStore.data.first()[familyIdKey]

    suspend fun setFamilyId(familyId: String) {
        dataStore.edit { it[familyIdKey] = familyId }
    }

    suspend fun clearFamilyId() {
        dataStore.edit { it.remove(familyIdKey) }
    }
}
