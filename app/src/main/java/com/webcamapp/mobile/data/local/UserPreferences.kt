package com.webcamapp.mobile.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        private val CURRENT_USER_ID = stringPreferencesKey("current_user_id")
        private val USER_ROLE = stringPreferencesKey("user_role")
        private val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        private val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
        private val QUIET_HOURS_START = stringPreferencesKey("quiet_hours_start")
        private val QUIET_HOURS_END = stringPreferencesKey("quiet_hours_end")
        private val AUTO_START_ENABLED = booleanPreferencesKey("auto_start_enabled")
        private val SCREEN_DIMMING_ENABLED = booleanPreferencesKey("screen_dimming_enabled")
    }

    val currentUserId: Flow<String?> = dataStore.data.map { preferences ->
        preferences[CURRENT_USER_ID]
    }

    val userRole: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_ROLE]
    }

    val isFirstLaunch: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_FIRST_LAUNCH] ?: true
    }

    val notificationEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[NOTIFICATION_ENABLED] ?: true
    }

    val quietHoursStart: Flow<String?> = dataStore.data.map { preferences ->
        preferences[QUIET_HOURS_START]
    }

    val quietHoursEnd: Flow<String?> = dataStore.data.map { preferences ->
        preferences[QUIET_HOURS_END]
    }

    val autoStartEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[AUTO_START_ENABLED] ?: false
    }

    val screenDimmingEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SCREEN_DIMMING_ENABLED] ?: true
    }

    suspend fun getCurrentUserId(): String? {
        return dataStore.data.map { it[CURRENT_USER_ID] }.first()
    }

    suspend fun setCurrentUserId(userId: String) {
        dataStore.edit { preferences ->
            preferences[CURRENT_USER_ID] = userId
        }
    }

    suspend fun clearCurrentUserId() {
        dataStore.edit { preferences ->
            preferences.remove(CURRENT_USER_ID)
        }
    }

    suspend fun setUserRole(role: String) {
        dataStore.edit { preferences ->
            preferences[USER_ROLE] = role
        }
    }

    suspend fun setFirstLaunch(isFirst: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_FIRST_LAUNCH] = isFirst
        }
    }

    suspend fun setNotificationEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATION_ENABLED] = enabled
        }
    }

    suspend fun setQuietHours(start: String?, end: String?) {
        dataStore.edit { preferences ->
            if (start != null) preferences[QUIET_HOURS_START] = start
            if (end != null) preferences[QUIET_HOURS_END] = end
        }
    }

    suspend fun setAutoStartEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[AUTO_START_ENABLED] = enabled
        }
    }

    suspend fun setScreenDimmingEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SCREEN_DIMMING_ENABLED] = enabled
        }
    }

    suspend fun clearAllPreferences() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}