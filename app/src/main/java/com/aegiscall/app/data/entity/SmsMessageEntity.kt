package com.aegiscall.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SmsCategory {
    PERSONAL,
    TRANSACTIONAL,
    OTP_2FA,
    PROMOTION,
    SPAM_QUARANTINE
}

@Entity(tableName = "sms_messages")
data class SmsMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis(),
    val category: SmsCategory,
    val extractedOtp: String? = null,
    val spamScore: Int = 0,
    val isRead: Boolean = false
)
