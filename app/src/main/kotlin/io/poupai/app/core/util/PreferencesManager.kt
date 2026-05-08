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

        // ─── Settings ───
        val KEY_THEME = stringPreferencesKey("app_theme")       // "light" | "dark" | "system"
        val KEY_HIDE_VALUES = booleanPreferencesKey("hide_values")
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

    // ─── Theme ───
    val appTheme: Flow<String> = context.dataStore.data.map {
        it[KEY_THEME] ?: "system"
    }

    suspend fun saveTheme(theme: String) {
        context.dataStore.edit { it[KEY_THEME] = theme }
    }

    suspend fun getThemeSync(): String =
        context.dataStore.data.first()[KEY_THEME] ?: "system"

    // ─── Hide Values ───
    val hideValues: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_HIDE_VALUES] ?: false
    }

    suspend fun saveHideValues(hide: Boolean) {
        context.dataStore.edit { it[KEY_HIDE_VALUES] = hide }
    }

    suspend fun getHideValuesSync(): Boolean =
        context.dataStore.data.first()[KEY_HIDE_VALUES] ?: false

    // ─── Logout ───
    suspend fun clearAll() {
        context.dataStore.edit { prefs ->
            // Preserva apenas as preferências de UI ao fazer logout
            val theme = prefs[KEY_THEME]
            val hideValues = prefs[KEY_HIDE_VALUES]
            prefs.clear()
            theme?.let { prefs[KEY_THEME] = it }
            hideValues?.let { prefs[KEY_HIDE_VALUES] = it }
        }
    }
}