package com.aegiscall.app.engine

import java.util.regex.Pattern

object OtpExtractorEngine {
    private val OTP_PATTERNS = listOf(
        Pattern.compile("(?i)(?:code|otp|pin|verification|one[- ]time[- ]password)[\\s]*[:\\-]?[\\s]*([0-9]{4,8})"),
        Pattern.compile("(?i)\\b([0-9]{4,8})\\b[\\s]+(?:is[\\s]+your[\\s]+(?:verification|otp|code|security))"),
        Pattern.compile("(?i)(?:google|microsoft|apple|amazon)[\\s]*(?:code)?[\\s]*[:\\-]?[\\s]*([a-z0-9]{5,8})")
    )

    fun extractOtp(messageBody: String): String? {
        for (pattern in OTP_PATTERNS) {
            val matcher = pattern.matcher(messageBody)
            if (matcher.find()) {
                val code = matcher.group(1)
                if (code != null && code.length in 4..8) {
                    return code
                }
            }
        }
        return null
    }
}
