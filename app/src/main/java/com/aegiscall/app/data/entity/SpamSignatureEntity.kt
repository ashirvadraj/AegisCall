package com.aegiscall.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SpamRiskLevel {
    SAFE,
    SUSPICIOUS,
    HIGH_RISK_SPAM,
    FRAUD_SCAM
}

@Entity(tableName = "spam_signatures")
data class SpamSignatureEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patternOrNumber: String,
    val reportedName: String,
    val riskLevel: SpamRiskLevel,
    val category: String, // Telemarketing, Debt Collector, Phishing, Robocall
    val totalReports: Int = 1,
    val lastReportedTimestamp: Long = System.currentTimeMillis()
)
