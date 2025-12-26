package com.example.guardiantrace.data.security

import android.content.Context
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CredentialManager @Inject constructor(
    private val context: Context,
    private val securePrefs: SecureSharedPreferencesWrapper
) {

    companion object {
        private const val PIN_LENGTH_MIN = 4
        private const val PIN_LENGTH_MAX = 8
        private const val PBKDF2_ITERATIONS = 100000
        private const val HASH_ALGORITHM = "SHA-256"
    }

    fun setPIN(pin: String): Boolean {
        // Validate PIN format
        if (!isValidPin(pin)) {
            return false
        }

        // Hash PIN using PBKDF2 with SHA-256
        val pinHash = hashCredential(pin)
        securePrefs.setPinHash(pinHash)

        return true
    }

    fun validatePin(pin: String): Boolean {
        if (!isValidPin(pin)) {
            return false
        }

        val storedHash = securePrefs.getPinHash() ?: return false
        val providedHash = hashCredential(pin)

        // Use constant-time comparison to prevent timing attacks
        return constantTimeEquals(storedHash, providedHash)
    }

    private fun isValidPin(pin: String): Boolean {
        return pin.length in PIN_LENGTH_MIN..PIN_LENGTH_MAX && pin.all { it.isDigit() }
    }

    private fun hashCredential(credential: String): String {
        val salt = generateSalt(32)
        val iterations = PBKDF2_ITERATIONS

        val spec = javax.crypto.spec.PBEKeySpec(
            credential.toCharArray(),
            salt,
            iterations,
            256
        )

        val factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded

        // Format: algorithm$iterations$salt$hash
        val saltHex = salt.joinToString("") { "%02x".format(it) }
        val hashHex = hash.joinToString("") { "%02x".format(it) }

        return "pbkdf2$100000$$saltHex$$hashHex"
    }

    private fun generateSalt(length: Int): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(length)
        random.nextBytes(salt)
        return salt
    }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) {
            return false
        }

        var result = 0
        for (i in a.indices) {
            result = result or (a[i].code xor b[i].code)
        }

        return result == 0
    }

    fun hasCredentialSet(): Boolean {
        return securePrefs.getPinHash() != null
    }
}

