package com.aegiscall.app.engine

import android.content.Context
import android.provider.ContactsContract
import com.aegiscall.app.data.AegisDatabase
import com.aegiscall.app.data.entity.SpamRiskLevel

data class CallerIdentity(
    val phoneNumber: String,
    val displayName: String,
    val isContact: Boolean,
    val spamScore: Int,
    val riskLevel: SpamRiskLevel,
    val category: String,
    val cityOrCarrier: String
)

class NumberLookupEngine(private val context: Context) {
    private val database = AegisDatabase.getDatabase(context)

    suspend fun resolveCaller(phoneNumber: String): CallerIdentity {
        val cleanNumber = phoneNumber.replace("[^0-9+]".toRegex(), "")

        // 1. Check local contacts first (100% private, never uploaded)
        val contactName = getContactDisplayName(cleanNumber)
        if (contactName != null) {
            return CallerIdentity(
                phoneNumber = cleanNumber,
                displayName = contactName,
                isContact = true,
                spamScore = 0,
                riskLevel = SpamRiskLevel.SAFE,
                category = "Personal Contact",
                cityOrCarrier = "Saved Contact"
            )
        }

        // 2. Check offline spam signatures
        val signature = database.spamSignatureDao().findSignature(cleanNumber)
        if (signature != null) {
            return CallerIdentity(
                phoneNumber = cleanNumber,
                displayName = signature.reportedName,
                isContact = false,
                spamScore = when (signature.riskLevel) {
                    SpamRiskLevel.SAFE -> 5
                    SpamRiskLevel.SUSPICIOUS -> 45
                    SpamRiskLevel.HIGH_RISK_SPAM -> 85
                    SpamRiskLevel.FRAUD_SCAM -> 99
                },
                riskLevel = signature.riskLevel,
                category = signature.category,
                cityOrCarrier = "Identified via Offline Shield"
            )
        }

        // 3. Fallback to heuristic rule analysis
        val heuristicRisk = SpamClassifierEngine.analyzePhoneNumber(cleanNumber)
        return CallerIdentity(
            phoneNumber = cleanNumber,
            displayName = if (heuristicRisk.spamScore > 70) "Suspicious Caller" else "Unknown Number",
            isContact = false,
            spamScore = heuristicRisk.spamScore,
            riskLevel = heuristicRisk.riskLevel,
            category = heuristicRisk.category,
            cityOrCarrier = heuristicRisk.locationOrCarrier
        )
    }

    private fun getContactDisplayName(phoneNumber: String): String? {
        val uri = ContactsContract.PhoneLookup.CONTENT_FILTER_URI.buildUpon()
            .appendPath(phoneNumber).build()
        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
        return try {
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                    if (nameIdx >= 0) cursor.getString(nameIdx) else null
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }
}
