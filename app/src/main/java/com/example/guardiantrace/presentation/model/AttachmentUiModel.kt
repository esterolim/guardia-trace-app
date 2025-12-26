package com.example.guardiantrace.presentation.model

data class AttachmentUiModel(
    val id: String = "",
    val incidentId: String = "",
    val fileName: String = "",
    val fileSize: Long = 0,
    val mimeType: String = "",
    val createdAt: java.time.LocalDateTime = java.time.LocalDateTime.now(),
    val filePath: String = "",
    val isEncrypted: Boolean = true,
    val attachmentType: AttachmentType = AttachmentType.IMAGE
)

enum class AttachmentType {
    IMAGE, VIDEO, AUDIO, DOCUMENT
}
