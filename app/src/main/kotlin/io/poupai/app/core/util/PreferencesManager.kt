package io.poupai.app.core.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "poupai_prefs")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    companion object {
        private val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_FIRST_NAME = stringPreferencesKey("first_name")
        private val KEY_PROFILE_IMAGE_URL = stringPreferencesKey("profile_image_url")
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val KEY_BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
    }

    // ─── Auth Token ───
    val authToken: Flow<String?> = context.dataStore.data.map { it[KEY_AUTH_TOKEN] }

    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { it[KEY_AUTH_TOKEN] = token }
    }

    suspend fun getAuthTokenSync(): String? =
        context.dataStore.data.first()[KEY_AUTH_TOKEN]

    // ─── User ID ───
    val userId: Flow<String?> = context.dataStore.data.map { it[KEY_USER_ID] }

    suspend fun saveUserId(id: String) {
        context.dataStore.edit { it[KEY_USER_ID] = id }
    }

    suspend fun getUserIdSync(): String? =
        context.dataStore.data.first()[KEY_USER_ID]

    // ─── First Name ───
    suspend fun saveFirstName(name: String) {
        context.dataStore.edit { it[KEY_FIRST_NAME] = name }
    }

    suspend fun getFirstNameSync(): String? =
        context.dataStore.data.first()[KEY_FIRST_NAME]

    // ─── Profile Image URL ───
    suspend fun saveProfileImageUrl(url: String) {
        context.dataStore.edit { it[KEY_PROFILE_IMAGE_URL] = url }
    }

    suspend fun getProfileImageUrlSync(): String? =
        context.dataStore.data.first()[KEY_PROFILE_IMAGE_URL]

    // ─── Onboarding ───
    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_ONBOARDING_COMPLETED] ?: false
    }

    suspend fun setOnboardingCompleted() {
        context.dataStore.edit { it[KEY_ONBOARDING_COMPLETED] = true }
    }

    // ─── Biometric ───
    val isBiometricEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_BIOMETRIC_ENABLED] ?: false
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_BIOMETRIC_ENABLED] = enabled }
    }

    // ─── Logout ───
    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}