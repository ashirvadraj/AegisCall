package com.aegiscall.app.engine

import android.content.Context
import android.database.Cursor
import android.net.Uri
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

data class SearchResultItem(
    val phoneNumber: String,
    val displayName: String,
    val isContact: Boolean,
    val spamScore: Int,
    val riskLevel: SpamRiskLevel,
    val category: String,
    val cityOrCarrier: String,
    val callCount: Int = 0,
    val lastCallTimestamp: Long? = null,
    val matchSource: String
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

    suspend fun searchNumberOrName(query: String): List<SearchResultItem> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()

        val results = mutableMapOf<String, SearchResultItem>()

        // A. Search device contacts by name or number
        try {
            val uri: Uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            )
            val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ? OR ${ContactsContract.CommonDataKinds.Phone.NUMBER} LIKE ?"
            val selectionArgs = arrayOf("%$trimmed%", "%$trimmed%")

            context.contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
                val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while (cursor.moveToNext()) {
                    val name = if (nameIdx >= 0) cursor.getString(nameIdx) else "Contact"
                    val rawNum = if (numIdx >= 0) cursor.getString(numIdx) else ""
                    val cleanNum = rawNum.replace("[^0-9+]".toRegex(), "")
                    if (cleanNum.isNotEmpty() && !results.containsKey(cleanNum)) {
                        results[cleanNum] = SearchResultItem(
                            phoneNumber = cleanNum,
                            displayName = name,
                            isContact = true,
                            spamScore = 0,
                            riskLevel = SpamRiskLevel.SAFE,
                            category = "Saved Contact",
                            cityOrCarrier = "Device Phonebook",
                            matchSource = "Contacts"
                        )
                    }
                }
            }
        } catch (e: Exception) {}

        // B. Search spam database by name or number
        try {
            val spamMatches = database.spamSignatureDao().searchSpamSignatures(trimmed)
            for (sig in spamMatches) {
                if (!results.containsKey(sig.patternOrNumber)) {
                    results[sig.patternOrNumber] = SearchResultItem(
                        phoneNumber = sig.patternOrNumber,
                        displayName = sig.reportedName,
                        isContact = false,
                        spamScore = when (sig.riskLevel) {
                            SpamRiskLevel.SAFE -> 5
                            SpamRiskLevel.SUSPICIOUS -> 45
                            SpamRiskLevel.HIGH_RISK_SPAM -> 85
                            SpamRiskLevel.FRAUD_SCAM -> 99
                        },
                        riskLevel = sig.riskLevel,
                        category = sig.category,
                        cityOrCarrier = "Reported ${sig.totalReports} times",
                        matchSource = "Offline Spam Shield"
                    )
                }
            }
        } catch (e: Exception) {}

        // C. If query has digits (potential phone number lookup), perform reverse heuristic check
        val digitsOnly = trimmed.replace("[^0-9+]".toRegex(), "")
        if (digitsOnly.length >= 4 && !results.containsKey(digitsOnly)) {
            val identity = resolveCaller(digitsOnly)
            results[digitsOnly] = SearchResultItem(
                phoneNumber = digitsOnly,
                displayName = identity.displayName,
                isContact = identity.isContact,
                spamScore = identity.spamScore,
                riskLevel = identity.riskLevel,
                category = identity.category,
                cityOrCarrier = identity.cityOrCarrier,
                matchSource = if (identity.isContact) "Contacts" else "Heuristic Reverse Lookup"
            )
        }

        // Enrich results with call history counts
        val enrichedList = mutableListOf<SearchResultItem>()
        for (item in results.values) {
            val callLogs = database.callLogDao().getCallsForNumber(item.phoneNumber)
            val callCount = callLogs.size
            val lastCall = callLogs.firstOrNull()?.timestamp
            enrichedList.add(item.copy(callCount = callCount, lastCallTimestamp = lastCall))
        }

        return enrichedList
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
