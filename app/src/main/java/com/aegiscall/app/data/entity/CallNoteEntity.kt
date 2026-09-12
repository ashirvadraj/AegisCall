package com.aegiscall.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_notes")
data class CallNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumber: String,
    val noteTitle: String,
    val noteContent: String,
    val createdAt: Long = System.currentTimeMillis(),
    val reminderTimestamp: Long? = null
)
