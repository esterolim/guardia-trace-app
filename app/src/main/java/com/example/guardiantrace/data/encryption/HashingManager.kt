package com.example.guardiantrace.data.encryption

import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HashingManager @Inject constructor() {

    fun sha256(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(data)
        return hash.joinToString("") { "%02x".format(it) }
    }

    fun sha256(data: String): String {
        return sha256(data.toByteArray(Charsets.UTF_8))
    }

    fun verifyHash(data: ByteArray, expectedHash: String): Boolean {
        val actualHash = sha256(data)
        return constantTimeEquals(actualHash, expectedHash)
    }

    /**
     * Verifies hash using constant-time comparison to prevent timing attacks
     */
    fun verifyHash(data: String, expectedHash: String): Boolean {
        val actualHash = sha256(data)
        return constantTimeEquals(actualHash, expectedHash)
    }

    fun hashFile(fileBytes: ByteArray): String {
        return sha256(fileBytes)
    }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        // Convert to byte arrays for comparison
        val aBytes = a.toByteArray()
        val bBytes = b.toByteArray()

        // If lengths differ, still compare all to avoid timing leak
        var result = aBytes.size == bBytes.size

        for (i in 0 until aBytes.size.coerceAtMost(bBytes.size)) {
            result = result and (aBytes[i] == bBytes[i])
        }

        return result
    }
}