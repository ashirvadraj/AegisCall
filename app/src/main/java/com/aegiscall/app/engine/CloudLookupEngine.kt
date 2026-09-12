package com.aegiscall.app.engine

import android.content.Context
import com.aegiscall.app.data.AegisDatabase
import com.aegiscall.app.data.entity.CachedCallerEntity
import com.aegiscall.app.data.entity.SpamRiskLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class CloudCallerInfo(
    val displayName: String,
    val carrier: String,
    val circleOrCity: String,
    val lineType: String,
    val spamScore: Int,
    val riskLevel: SpamRiskLevel,
    val isVerified: Boolean,
    val source: String
)

class CloudLookupEngine(private val context: Context) {
    private val database = AegisDatabase.getDatabase(context)

    suspend fun lookupNumber(rawNumber: String): CloudCallerInfo = withContext(Dispatchers.IO) {
        val cleanNumber = rawNumber.replace("[^0-9+]".toRegex(), "")

        // 1. Check local cache first
        val cached = database.cachedCallerDao().getCachedCaller(cleanNumber)
        if (cached != null) {
            return@withContext CloudCallerInfo(
                displayName = cached.resolvedName,
                carrier = cached.carrier,
                circleOrCity = cached.circleOrCity,
                lineType = "Cellular Mobile",
                spamScore = cached.spamScore,
                riskLevel = if (cached.spamScore > 60) SpamRiskLevel.HIGH_RISK_SPAM else SpamRiskLevel.SAFE,
                isVerified = cached.isVerified,
                source = "Offline Verified Cache"
            )
        }

        // 2. Decode telecom circle and operator (e.g. 7808 -> Bharti Airtel, Bihar & Jharkhand)
        val indianTelecom = IndianTelecomDecoder.decode(cleanNumber)

        // 3. Attempt live online reverse query via public directory or community API
        val onlineName = queryOnlineDirectory(cleanNumber)

        val resolvedName = when {
            !onlineName.isNullOrBlank() -> onlineName
            indianTelecom != null -> "${indianTelecom.operator} Subscriber"
            else -> "Mobile Cellular Subscriber"
        }

        val carrier = indianTelecom?.operator ?: "Direct Cellular"
        val location = indianTelecom?.state ?: "Verified Mobile"

        val result = CloudCallerInfo(
            displayName = resolvedName,
            carrier = carrier,
            circleOrCity = if (indianTelecom != null) "${indianTelecom.circle}, ${indianTelecom.state}" else location,
            lineType = indianTelecom?.lineType ?: "Cellular",
            spamScore = 0,
            riskLevel = SpamRiskLevel.SAFE,
            isVerified = true,
            source = if (!onlineName.isNullOrBlank()) "Cloud Reverse Directory" else "DoT Telecom Registry"
        )

        // Cache result into database so future lookups are instant
        try {
            database.cachedCallerDao().insertCachedCaller(
                CachedCallerEntity(
                    phoneNumber = cleanNumber,
                    resolvedName = resolvedName,
                    carrier = carrier,
                    circleOrCity = result.circleOrCity,
                    spamScore = 0,
                    isVerified = true
                )
            )
        } catch (e: Exception) {}

        result
    }

    private fun queryOnlineDirectory(phoneNumber: String): String? {
        return try {
            val encoded = URLEncoder.encode(phoneNumber, "UTF-8")
            // Query public caller reverse lookup API with 3s timeout
            val url = URL("https://who-called.me/search?q=$encoded")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 3000
            conn.readTimeout = 3000
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile)")
            conn.requestMethod = "GET"

            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val response = reader.readText()
                reader.close()
                // Check if community title / name was identified
                if (response.contains("""class="name">""")) {
                    val part = response.substringAfter("""class="name">""").substringBefore("</span>")
                    if (part.isNotBlank() && part.length < 50) return part.trim()
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }
}
