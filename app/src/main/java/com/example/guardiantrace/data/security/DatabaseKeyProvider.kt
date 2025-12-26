package com.example.guardiantrace.data.security

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

class DatabaseKeyProvider(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "guardian_trace_db_keys"
        private const val DB_PASSPHRASE_KEY = "db_passphrase"
        private const val DB_SALT_KEY = "db_salt"
        private const val DB_KEY_VERSION_KEY = "db_key_version"
        private const val CURRENT_KEY_VERSION = 1
        private const val PBKDF2_ITERATIONS = 100000
        private const val SALT_LENGTH = 32
    }

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val encryptedPrefs by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getDatabasePassphrase(): String {
        val storedVersion = encryptedPrefs.getInt(DB_KEY_VERSION_KEY, 0)

        // Rotate key if version changed
        if (storedVersion != CURRENT_KEY_VERSION) {
            generateNewDatabaseKey()
        }

        return encryptedPrefs.getString(DB_PASSPHRASE_KEY, "")
            ?: generateNewDatabaseKey()
    }

    private fun generateNewDatabaseKey(): String {
        // Generate or retrieve salt
        val salt = if (encryptedPrefs.contains(DB_SALT_KEY)) {
            encryptedPrefs.getString(DB_SALT_KEY, "") ?: generateSalt()
        } else {
            generateSalt()
        }

        // Derive passphrase from device fingerprint and salt using PBKDF2
        val passphrase = derivePassphrase(salt)

        // Store in encrypted shared preferences
        encryptedPrefs.edit().apply {
            putString(DB_PASSPHRASE_KEY, passphrase)
            putString(DB_SALT_KEY, salt)
            putInt(DB_KEY_VERSION_KEY, CURRENT_KEY_VERSION)
            apply()
        }

        return passphrase
    }

    private fun generateSalt(): String {
        val random = SecureRandom()
        val saltBytes = ByteArray(SALT_LENGTH)
        random.nextBytes(saltBytes)
        return saltBytes.joinToString("") { "%02x".format(it) }
    }

    private fun derivePassphrase(salt: String): String {
        val deviceId = getOrCreateDeviceId()
        val input = "$deviceId:$salt"

        // Use PBKDF2 with SHA-256
        val spec = javax.crypto.spec.PBEKeySpec(
            input.toCharArray(),
            salt.toByteArray(Charsets.UTF_8),
            PBKDF2_ITERATIONS,
            256
        )

        val factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val key = factory.generateSecret(spec)
        val bytes = key.encoded

        // Convert bytes to hex string
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun getOrCreateDeviceId(): String {
        val deviceIdKey = "device_id"
        val storedId = encryptedPrefs.getString(deviceIdKey, null)

        return if (storedId != null) {
            storedId
        } else {
            val newId = generateSecureDeviceId()
            encryptedPrefs.edit { putString(deviceIdKey, newId) }
            newId
        }
    }

    private fun generateSecureDeviceId(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

