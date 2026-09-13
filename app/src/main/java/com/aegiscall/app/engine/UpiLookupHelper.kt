package com.aegiscall.app.engine

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object UpiLookupHelper {

    /**
     * Launches a UPI verification intent to resolve the real legal bank account name
     * of the person holding this mobile number via Google Pay / PhonePe / Paytm.
     */
    fun verifyBankName(context: Context, rawNumber: String) {
        val cleanNumber = rawNumber.replace("[^0-9]".toRegex(), "")
        val tenDigit = if (cleanNumber.length >= 10) cleanNumber.takeLast(10) else cleanNumber

        // Paytm VPA is default for almost all Indian cellular numbers
        val upiUri = Uri.parse("upi://pay?pa=$tenDigit@paytm&pn=VerifyName&cu=INR")
        val intent = Intent(Intent.ACTION_VIEW, upiUri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to YBL (PhonePe)
            val fallbackUri = Uri.parse("upi://pay?pa=$tenDigit@ybl&pn=VerifyName&cu=INR")
            val fallbackIntent = Intent(Intent.ACTION_VIEW, fallbackUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(fallbackIntent)
            } catch (ex: Exception) {
                Toast.makeText(context, "No UPI app (PhonePe/GPay/Paytm) found on this device", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Launches Truecaller web search directly in browser/custom tab for this number.
     */
    fun openTruecallerWeb(context: Context, rawNumber: String) {
        val cleanNumber = rawNumber.replace("[^0-9]".toRegex(), "")
        val tenDigit = if (cleanNumber.length >= 10) cleanNumber.takeLast(10) else cleanNumber
        val url = "https://www.truecaller.com/search/in/$tenDigit"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open browser", Toast.LENGTH_SHORT).show()
        }
    }
}
