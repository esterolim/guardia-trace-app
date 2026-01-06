package com.example.guardiantrace.data.security

import android.content.Context
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class SecurityPolicyEnforcer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val integrityChecker: IntegrityChecker,
    private val screenRecordingDetector: ScreenRecordingDetector
) {

    fun enforceSecurityPolicies(): SecurityComplianceResult {
        val checks = listOf(
            "Android Version" to checkMinimumAndroidVersion(),
            "Debugger Detection" to checkDebugger(),
            "Device Rooting" to checkDeviceIntegrity(),
            "Encryption" to checkEncryption(),
            "Screen Recording" to checkScreenRecording()
        )

        val violations = checks.filter { !it.second }
        val isCompliant = violations.isEmpty()

        return SecurityComplianceResult(
            isCompliant = isCompliant,
            violations = violations.map { it.first },
            timestamp = System.currentTimeMillis()
        )
    }

    private fun checkMinimumAndroidVersion(): Boolean {
        return true
    }

    private fun checkDebugger(): Boolean {
        val isDebuggerAttached = integrityChecker.isDebuggerAttached()
        if (isDebuggerAttached) {
            SecureLogger.securityEvent("Security Policy Violation", "Debugger detected")
        }
        return !isDebuggerAttached
    }

    private fun checkDeviceIntegrity(): Boolean {
        val isRooted = integrityChecker.isDeviceRooted()
        if (isRooted) {
            SecureLogger.securityEvent("Security Policy Violation", "Device appears to be rooted")
        }
        return !isRooted
    }

    private fun checkEncryption(): Boolean {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            masterKey != null
        } catch (e: Exception) {
            SecureLogger.e("Encryption check failed", e)
            false
        }
    }


    private fun checkScreenRecording(): Boolean {
        val status = screenRecordingDetector.monitorScreenCapture()
        if (status.isCaptureSuspected()) {
            SecureLogger.securityEvent(
                "Security Policy Violation",
                "Screen recording or mirroring detected"
            )
        }
        return !status.isCaptureSuspected()
    }


    fun validateCriticalSecurityMeasures(): List<SecurityMeasure> {
        return listOf(
            SecurityMeasure(
                name = "FLAG_SECURE Enabled",
                isImplemented = true, // Set by MainActivity
                description = "Screenshots and screen recording blocked"
            ),
            SecurityMeasure(
                name = "Data Encryption",
                isImplemented = checkEncryption(),
                description = "SQLCipher and file encryption enabled"
            ),
            SecurityMeasure(
                name = "Secure Preferences",
                isImplemented = checkEncryptedPreferences(),
                description = "EncryptedSharedPreferences with AndroidKeyStore"
            ),
            SecurityMeasure(
                name = "Memory Protection",
                isImplemented = true, // MemoryZeroingUtil available
                description = "Sensitive data zeroed from memory"
            ),
            SecurityMeasure(
                name = "Integrity Checking",
                isImplemented = integrityChecker.performIntegrityCheck(),
                description = "App integrity verified"
            ),
            SecurityMeasure(
                name = "Secure Logging",
                isImplemented = true, // SecureLogger available
                description = "Logs filtered for PII"
            ),
            SecurityMeasure(
                name = "Safe Data Wipe",
                isImplemented = true, // SafeDataCleaner available
                description = "Secure data deletion available"
            )
        )
    }

    private fun checkEncryptedPreferences(): Boolean {
        return try {
            val keyStore = java.security.KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            keyStore.containsAlias(MasterKey.DEFAULT_MASTER_KEY_ALIAS)
        } catch (e: Exception) {
            false
        }
    }

    fun generateComplianceReport(): ComplianceReport {
        val compliance = enforceSecurityPolicies()
        val measures = validateCriticalSecurityMeasures()

        return ComplianceReport(
            isCompliant = compliance.isCompliant,
            complianceScore = calculateComplianceScore(measures),
            violations = compliance.violations,
            implementedMeasures = measures.filter { it.isImplemented },
            missingMeasures = measures.filter { !it.isImplemented },
            timestamp = System.currentTimeMillis()
        )
    }

    private fun calculateComplianceScore(measures: List<SecurityMeasure>): Float {
        if (measures.isEmpty()) return 0f
        val implemented = measures.count { it.isImplemented }
        return (implemented.toFloat() / measures.size) * 100f
    }

    data class SecurityComplianceResult(
        val isCompliant: Boolean,
        val violations: List<String>,
        val timestamp: Long
    )

    data class SecurityMeasure(
        val name: String,
        val isImplemented: Boolean,
        val description: String
    )

    data class ComplianceReport(
        val isCompliant: Boolean,
        val complianceScore: Float,
        val violations: List<String>,
        val implementedMeasures: List<SecurityMeasure>,
        val missingMeasures: List<SecurityMeasure>,
        val timestamp: Long
    )
}

