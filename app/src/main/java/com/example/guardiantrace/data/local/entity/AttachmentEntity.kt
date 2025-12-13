package com.example.guardiantrace.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "attachments",
    foreignKeys = [
        ForeignKey(
            entity = IncidentEntity::class,
            parentColumns = ["id"],
            childColumns = ["incident_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["incident_id"])]
)
data class AttachmentEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "incident_id")
    val incidentId: Long,

    @ColumnInfo(name = "file_name")
    val fileName: String,

    @ColumnInfo(name = "file_type")
    val fileType: String, // IMAGE, VIDEO, AUDIO, DOCUMENT

    @ColumnInfo(name = "file_size")
    val fileSize: Long,

    @ColumnInfo(name = "file_path")
    val filePath: String, // Encrypted file path

    @ColumnInfo(name = "mime_type")
    val mimeType: String,

    @ColumnInfo(name = "sha256_hash")
    val sha256Hash: String, // For integrity verification

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "metadata")
    val metadata: String? = null // JSON metadata
)