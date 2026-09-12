package com.aegiscall.app.engine

import com.aegiscall.app.data.entity.SmsCategory
import com.aegiscall.app.data.entity.SpamRiskLevel
import java.util.regex.Pattern

data class PhoneSpamReport(
    val spamScore: Int,
    val riskLevel: SpamRiskLevel,
    val category: String,
    val locationOrCarrier: String
)

data class SmsClassificationReport(
    val category: SmsCategory,
    val spamScore: Int,
    val isPhishing: Boolean,
    val reasons: List<String>
)

object SpamClassifierEngine {

    // Phone heuristics
    fun analyzePhoneNumber(phoneNumber: String): PhoneSpamReport {
        val clean = phoneNumber.replace("[^0-9+]".toRegex(), "")

        return when {
            // Toll-free or Premium rate spam lines
            clean.startsWith("+1800") || clean.startsWith("+1888") || clean.startsWith("+1877") -> {
                PhoneSpamReport(
                    spamScore = 75,
                    riskLevel = SpamRiskLevel.HIGH_RISK_SPAM,
                    category = "Toll-Free Telemarketer",
                    locationOrCarrier = "Toll-Free Gateway"
                )
            }
            clean.startsWith("+1900") -> {
                PhoneSpamReport(
                    spamScore = 95,
                    riskLevel = SpamRiskLevel.FRAUD_SCAM,
                    category = "Premium Rate Robocall",
                    locationOrCarrier = "High Risk Line"
                )
            }
            // Irregular short numbers
            clean.length in 4..6 && !clean.startsWith("+") -> {
                PhoneSpamReport(
                    spamScore = 30,
                    riskLevel = SpamRiskLevel.SUSPICIOUS,
                    category = "Commercial Shortcode",
                    locationOrCarrier = "Automated Gateway"
                )
            }
            // Standard benign default
            else -> {
                PhoneSpamReport(
                    spamScore = 0,
                    riskLevel = SpamRiskLevel.SAFE,
                    category = "Standard Cellular",
                    locationOrCarrier = "Direct Mobile"
                )
            }
        }
    }

    // SMS heuristics & NLP rule patterns
    private val PHISHING_KEYWORDS = listOf(
        "urgent: your bank account has been locked",
        "verify your identity immediately",
        "unauthorized transaction detected",
        "you won a lottery",
        "irs final warning",
        "arrest warrant issued",
        "crypto giveaway",
        "claim your free cash",
        "account suspended click here"
    )

    private val BANKING_KEYWORDS = listOf(
        "credited with", "debited with", "available balance", "txn id",
        "atm withdrawal", "acct ending", "upi transaction", "statement available"
    )

    private val PROMO_KEYWORDS = listOf(
        "flat % off", "exclusive offer", "hurry discount ends",
        "coupon code", "promo valid until", "shop now", "free delivery on order"
    )

    private val SHORT_URL_PATTERN = Pattern.compile("(?i)(https?://)?(bit\\.ly|tinyurl\\.com|is\\.gd|cutt\\.ly|t\\.co|ow\\.ly)/[a-zA-Z0-9]+")

    fun classifySms(sender: String, body: String): SmsClassificationReport {
        val lowerBody = body.lowercase()
        val detectedReasons = mutableListOf<String>()
        var spamScore = 0

        // 1. Check for OTP first
        val otp = OtpExtractorEngine.extractOtp(body)
        if (otp != null && (lowerBody.contains("otp") || lowerBody.contains("code") || lowerBody.contains("verification"))) {
            return SmsClassificationReport(
                category = SmsCategory.OTP_2FA,
                spamScore = 0,
                isPhishing = false,
                reasons = listOf("Contains valid 2FA verification code")
            )
        }

        // 2. Check for malicious phishing triggers
        for (keyword in PHISHING_KEYWORDS) {
            if (lowerBody.contains(keyword)) {
                spamScore += 40
                detectedReasons.add("Matched phishing phrase: '$keyword'")
            }
        }

        // Check for suspicious URL shorteners
        if (SHORT_URL_PATTERN.matcher(body).find()) {
            spamScore += 35
            detectedReasons.add("Contains anonymized URL shortener")
        }

        if (spamScore >= 50) {
            return SmsClassificationReport(
                category = SmsCategory.SPAM_QUARANTINE,
                spamScore = spamScore.coerceAtMost(100),
                isPhishing = true,
                reasons = detectedReasons
            )
        }

        // 3. Check for Banking / Transactional
        for (keyword in BANKING_KEYWORDS) {
            if (lowerBody.contains(keyword)) {
                return SmsClassificationReport(
                    category = SmsCategory.TRANSACTIONAL,
                    spamScore = 0,
                    isPhishing = false,
                    reasons = listOf("Verified transactional message pattern")
                )
            }
        }

        // 4. Check for Promotions
        for (keyword in PROMO_KEYWORDS) {
            if (lowerBody.contains(keyword)) {
                return SmsClassificationReport(
                    category = SmsCategory.PROMOTION,
                    spamScore = 15,
                    isPhishing = false,
                    reasons = listOf("Marketing promotional keywords detected")
                )
            }
        }

        // 5. Default to Personal
        return SmsClassificationReport(
            category = SmsCategory.PERSONAL,
            spamScore = 0,
            isPhishing = false,
            reasons = emptyList()
        )
    }
}
