package com.example.guardiantrace.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.guardiantrace.data.local.entity.AttachmentEntity
import com.example.guardiantrace.data.local.entity.IncidentEntity

data class IncidentWithAttachments(
    @Embedded
    val incident: IncidentEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "incident_id"
    )
    val attachmentIds: List<AttachmentEntity>
)