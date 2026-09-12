package com.aegiscall.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CallType {
    INCOMING,
    OUTGOING,
    MISSED,
    BLOCKED_SPAM
}

enum class StirShakenStatus {
    VERIFIED_CARRIER,
    UNVERIFIED,
    FAILED_ATTESTATION,
    UNKNOWN
}

@Entity(tableName = "call_logs")
data class CallLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumber: String,
    val contactName: String? = null,
    val callType: CallType,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0,
    val spamScore: Int = 0, // 0 to 100
    val spamCategory: String? = null,
    val cityOrRegion: String? = null,
    val carrierName: String? = null,
    val stirShakenStatus: StirShakenStatus = StirShakenStatus.UNKNOWN,
    val userNote: String? = null
)
