package com.fileshare.app.util

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "security_prefs")

object SecurityUtils {
    
    private val LOCK_ENABLED_KEY = booleanPreferencesKey("lock_enabled")
    private val PIN_HASH_KEY = stringPreferencesKey("pin_hash")
    private val BIOMETRIC_ENABLED_KEY = booleanPreferencesKey("biometric_enabled")
    
    /**
     * Hash PIN using SHA-256
     */
    fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(pin.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Check if app lock is enabled
     */
    fun isLockEnabled(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[LOCK_ENABLED_KEY] ?: false
        }
    }
    
    /**
     * Set app lock enabled state
     */
    suspend fun setLockEnabled(context: Context, enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[LOCK_ENABLED_KEY] = enabled
        }
    }
    
    /**
     * Save PIN hash
     */
    suspend fun savePinHash(context: Context, pinHash: String) {
        context.dataStore.edit { preferences ->
            preferences[PIN_HASH_KEY] = pinHash
        }
    }
    
    /**
     * Get saved PIN hash
     */
    fun getPinHash(context: Context): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[PIN_HASH_KEY]
        }
    }
    
    /**
     * Verify PIN
     */
    suspend fun verifyPin(context: Context, pin: String): Boolean {
        val savedHash = context.dataStore.data.map { it[PIN_HASH_KEY] }
        var result = false
        savedHash.collect { hash ->
            result = hash == hashPin(pin)
        }
        return result
    }
    
    /**
     * Check if biometric is enabled
     */
    fun isBiometricEnabled(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[BIOMETRIC_ENABLED_KEY] ?: false
        }
    }
    
    /**
     * Set biometric enabled state
     */
    suspend fun setBiometricEnabled(context: Context, enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BIOMETRIC_ENABLED_KEY] = enabled
        }
    }
    
    /**
     * Check if biometric authentication is available
     */
    fun isBiometricAvailable(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.BIOMETRIC_WEAK
        )) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }
    
    /**
     * Show biometric authentication prompt
     */
    fun showBiometricPrompt(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onError: () -> Unit,
        onFailed: () -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError()
                }
                
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }
                
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFailed()
                }
            }
        )
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("PIN 사용")
            .build()
        
        biometricPrompt.authenticate(promptInfo)
    }
    
    /**
     * Clear all security data
     */
    suspend fun clearSecurityData(context: Context) {
        context.dataStore.edit { preferences ->
            preferences.remove(LOCK_ENABLED_KEY)
            preferences.remove(PIN_HASH_KEY)
            preferences.remove(BIOMETRIC_ENABLED_KEY)
        }
    }
}
