package com.aegiscall.app.engine

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class TruecallerProfile(
    val name: String?,
    val carrier: String?,
    val city: String?,
    val spamScore: Int,
    val isSpam: Boolean,
    val isVerified: Boolean
)

object TruecallerLookupClient {
    private const val PREFS_NAME = "aegis_truecaller_prefs"
    private const val KEY_AUTH_TOKEN = "truecaller_auth_token"
    private const val KEY_PROXY_URL = "truecaller_proxy_url"

    private const val DEFAULT_SEARCH_API = "https://search5-noneu.truecaller.com/v2/search"

    fun getAuthToken(context: Context): String? {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    fun setAuthToken(context: Context, token: String?) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_AUTH_TOKEN, token?.trim()).apply()
    }

    suspend fun queryTruecaller(context: Context, phoneNumber: String, countryCode: String = "IN"): TruecallerProfile? = withContext(Dispatchers.IO) {
        val token = getAuthToken(context)
        val cleanNumber = phoneNumber.replace("[^0-9+]".toRegex(), "")

        // If no user token configured, return null gracefully to allow other providers
        if (token.isNullOrBlank()) {
            return@withContext null
        }

        try {
            val queryUrl = "$DEFAULT_SEARCH_API?q=${URLEncoder.encode(cleanNumber, "UTF-8")}&countryCode=$countryCode&type=4&placement=SEARCHRESULTS&encoding=json"
            val url = URL(queryUrl)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 4000
            conn.readTimeout = 4000
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.setRequestProperty("User-Agent", "Truecaller/14.0 (Android; 14)")
            conn.setRequestProperty("Accept", "application/json")

            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val jsonStr = reader.readText()
                reader.close()

                val root = JSONObject(jsonStr)
                val dataArray = root.optJSONArray("data")
                if (dataArray != null && dataArray.length() > 0) {
                    val firstItem = dataArray.getJSONObject(0)
                    val name = if (firstItem.has("name")) firstItem.getString("name") else null
                    val spamScore = firstItem.optInt("spamScore", 0)
                    val badges = firstItem.optJSONArray("badges")
                    val isVerified = badges != null && badges.length() > 0

                    val phones = firstItem.optJSONArray("phones")
                    var carrier: String? = null
                    var city: String? = null
                    if (phones != null && phones.length() > 0) {
                        val phoneObj = phones.getJSONObject(0)
                        carrier = if (phoneObj.has("carrier")) phoneObj.getString("carrier") else null
                        city = if (phoneObj.has("city")) phoneObj.getString("city") else null
                    }

                    return@withContext TruecallerProfile(
                        name = name,
                        carrier = carrier,
                        city = city,
                        spamScore = spamScore,
                        isSpam = spamScore >= 70,
                        isVerified = isVerified
                    )
                }
            }
        } catch (e: Exception) {
            // Network or token expired
        }
        null
    }
}
