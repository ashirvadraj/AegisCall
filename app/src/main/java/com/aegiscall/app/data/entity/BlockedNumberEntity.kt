package com.aegiscall.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_numbers")
data class BlockedNumberEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumberOrPattern: String,
    val reason: String = "Manual Block",
    val isWildcard: Boolean = false,
    val blockedCallsCount: Int = 0,
    val addedAt: Long = System.currentTimeMillis()
)
