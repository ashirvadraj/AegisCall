package com.aegiscall.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_callers")
data class CachedCallerEntity(
    @PrimaryKey
    val phoneNumber: String,
    val resolvedName: String,
    val carrier: String,
    val circleOrCity: String,
    val spamScore: Int = 0,
    val isVerified: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)
