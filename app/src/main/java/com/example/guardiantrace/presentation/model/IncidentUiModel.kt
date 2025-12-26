package com.example.guardiantrace.presentation.model

import java.time.LocalDateTime

data class IncidentUiModel(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val type: String = "HARASSMENT",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val attachments: List<AttachmentUiModel> = emptyList(),
    val isEncrypted: Boolean = true,
    val status: IncidentStatus = IncidentStatus.RECORDED
)

enum class IncidentStatus {
    DRAFT, RECORDED, EXPORTED, ARCHIVED
}