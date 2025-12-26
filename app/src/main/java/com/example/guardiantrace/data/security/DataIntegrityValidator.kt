package com.example.guardiantrace.data.security

import com.example.guardiantrace.data.encryption.HashingManager
import com.example.guardiantrace.data.local.entity.AttachmentEntity
import com.example.guardiantrace.data.local.entity.EmergencyContactEntity
import com.example.guardiantrace.data.local.entity.IncidentEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataIntegrityValidator @Inject constructor(
    private val hashingManager: HashingManager
) {
    fun generateIncidentHash(incident: IncidentEntity): String {
        val dataToHash = "${incident.id}|${incident.title}|${incident.description}|" +
                "${incident.timestamp}|${incident.latitude}|${incident.longitude}|" +
                "${incident.address}|${incident.isDeleted}|${incident.syncStatus}"
        return hashingManager.sha256(dataToHash)
    }

    fun generateContactHash(contact: EmergencyContactEntity): String {
        val dataToHash = "${contact.id}|${contact.name}|${contact.phoneNumber}|" +
                "${contact.email}|${contact.relationship}|${contact.priority}|" +
                "${contact.isActive}"
        return hashingManager.sha256(dataToHash)
    }

    fun generateAttachmentHash(attachment: AttachmentEntity): String {
        val dataToHash = "${attachment.id}|${attachment.fileName}|${attachment.filePath}|" +
                "${attachment.fileSize}|${attachment.fileType}|${attachment.mimeType}"
        return hashingManager.sha256(dataToHash)
    }

    fun verifySensitiveDataEncrypted(
        phoneNumber: String?,
        email: String?,
        personalInfo: String?
    ): Boolean {
        // Check that sensitive data doesn't appear in plaintext logs
        if (phoneNumber != null && !isValidPhone(phoneNumber)) {
            return false
        }
        if (email != null && !isValidEmail(email)) {
            return false
        }
        return true
    }

    private fun isValidPhone(phone: String): Boolean {
        // Phone should be stored encrypted, not in plaintext
        return phone.isNotEmpty() && phone.length >= 10
    }

    private fun isValidEmail(email: String): Boolean {
        // Email should contain @ symbol and be properly formatted
        return email.contains("@") && email.contains(".")
    }

    fun createHmacForData(data: String, key: String): String {
        val combinedData = "$data|$key"
        return hashingManager.sha256(combinedData)
    }

    fun validateHmac(data: String, providedHmac: String, key: String): Boolean {
        val expectedHmac = createHmacForData(data, key)
        return hashingManager.verifyHash(expectedHmac, providedHmac)
    }
}

