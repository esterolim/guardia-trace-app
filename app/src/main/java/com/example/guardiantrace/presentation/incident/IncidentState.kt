package com.example.guardiantrace.presentation.incident

import com.example.guardiantrace.presentation.model.AttachmentUiModel
import com.example.guardiantrace.presentation.model.IncidentUiModel


// Sealed class representing all possible states for incident management

sealed class IncidentState {
    data object Idle : IncidentState()
    data object Loading : IncidentState()
    data class IncidentListLoaded(val incidents: List<IncidentUiModel>) : IncidentState()
    data class IncidentDetailLoaded(val incident: IncidentUiModel) : IncidentState()
    data class Success(val message: String) : IncidentState()
    data class Error(val message: String, val throwable: Throwable? = null) : IncidentState()
}

// Sealed class for incident-related events
sealed class IncidentEvent {
    data class OnIncidentCreated(val incidentId: String) : IncidentEvent()
    data class OnIncidentUpdated(val incidentId: String) : IncidentEvent()
    data class OnIncidentDeleted(val incidentId: String) : IncidentEvent()
    data class OnAttachmentAdded(val attachmentId: String) : IncidentEvent()
    data object OnNavigateBack : IncidentEvent()
}

/**
 * UI model for incident list/detail screens
 */
data class IncidentListUiModel(
    val incidents: List<IncidentUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedIncidentId: String? = null
)

/**
 * UI model for incident creation
 */
data class CreateIncidentUiModel(
    val title: String = "",
    val description: String = "",
    val attachments: List<AttachmentUiModel> = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val incidentType: IncidentType = IncidentType.HARASSMENT
)

enum class IncidentType {
    HARASSMENT, STALKING, THREAT, OTHER
}

