package com.aegiscall.app.engine

data class TelecomIdentity(
    val operator: String,
    val circle: String,
    val state: String,
    val lineType: String = "Cellular GSM Mobile"
)

object IndianTelecomDecoder {

    /**
     * Decodes 10-digit Indian numbers (or +91...) into Operator and Telecom Circle.
     * Covers 7808 (Bharti Airtel, Bihar & Jharkhand) and major Indian series.
     */
    fun decode(cleanNumber: String): TelecomIdentity? {
        val number = if (cleanNumber.startsWith("+91")) {
            cleanNumber.substring(3)
        } else if (cleanNumber.startsWith("91") && cleanNumber.length == 12) {
            cleanNumber.substring(2)
        } else if (cleanNumber.startsWith("0") && cleanNumber.length == 11) {
            cleanNumber.substring(1)
        } else {
            cleanNumber
        }

        if (number.length != 10) return null

        val prefix4 = number.substring(0, 4)
        val prefix2 = number.substring(0, 2)

        // Specific 4-digit series mappings
        return when (prefix4) {
            "7808" -> TelecomIdentity("Bharti Airtel", "Bihar & Jharkhand", "Bihar, India")
            "7800", "7809" -> TelecomIdentity("Bharti Airtel", "UP East", "Uttar Pradesh, India")
            "7814" -> TelecomIdentity("Bharti Airtel", "Punjab", "Punjab, India")
            "7827" -> TelecomIdentity("Reliance Jio", "Delhi NCR", "Delhi, India")
            "7838" -> TelecomIdentity("Vodafone Idea", "Delhi NCR", "Delhi, India")
            "7877" -> TelecomIdentity("Bharti Airtel", "Rajasthan", "Rajasthan, India")
            "7894" -> TelecomIdentity("Bharti Airtel", "Odisha", "Odisha, India")
            "7898" -> TelecomIdentity("Bharti Airtel", "MP & Chhattisgarh", "Madhya Pradesh, India")
            "9810" -> TelecomIdentity("Bharti Airtel", "Delhi NCR", "Delhi, India")
            "9811" -> TelecomIdentity("Vodafone Idea", "Delhi NCR", "Delhi, India")
            "9820" -> TelecomIdentity("Vodafone Idea", "Mumbai", "Maharashtra, India")
            "9821" -> TelecomIdentity("Bharti Airtel", "Mumbai", "Maharashtra, India")
            "9822" -> TelecomIdentity("Idea Cellular", "Maharashtra & Goa", "Maharashtra, India")
            "9830" -> TelecomIdentity("Vodafone Idea", "Kolkata", "West Bengal, India")
            "9831" -> TelecomIdentity("Bharti Airtel", "Kolkata", "West Bengal, India")
            "9840" -> TelecomIdentity("Bharti Airtel", "Chennai", "Tamil Nadu, India")
            "9841" -> TelecomIdentity("Vodafone Idea", "Chennai", "Tamil Nadu, India")
            "9845" -> TelecomIdentity("Bharti Airtel", "Karnataka", "Karnataka, India")
            "9848" -> TelecomIdentity("Bharti Airtel", "Andhra Pradesh & Telangana", "Hyderabad, India")
            "9849" -> TelecomIdentity("Vodafone Idea", "Andhra Pradesh & Telangana", "Hyderabad, India")
            "9896" -> TelecomIdentity("Bharti Airtel", "Haryana", "Haryana, India")
            "9876" -> TelecomIdentity("Bharti Airtel", "Punjab", "Punjab, India")
            "9829" -> TelecomIdentity("Bharti Airtel", "Rajasthan", "Rajasthan, India")
            "9839" -> TelecomIdentity("Vodafone Idea", "UP East", "Uttar Pradesh, India")
            "9415" -> TelecomIdentity("BSNL Mobile", "UP East", "Uttar Pradesh, India")
            "9431" -> TelecomIdentity("BSNL Mobile", "Bihar & Jharkhand", "Bihar, India")
            "9440" -> TelecomIdentity("BSNL Mobile", "Andhra Pradesh", "Andhra Pradesh, India")
            else -> {
                // Heuristic based on 2-digit series
                val op = when (prefix2) {
                    "70", "79", "89", "99" -> "Reliance Jio / Airtel"
                    "78", "88", "98" -> "Bharti Airtel"
                    "77", "87", "97" -> "Vodafone Idea (Vi)"
                    "94", "93" -> "BSNL / MTNL"
                    "62", "63" -> "Reliance Jio"
                    "82", "83" -> "Bharti Airtel"
                    else -> "Indian Cellular Subscriber"
                }
                TelecomIdentity(op, "National Telecom Circle", "India ????")
            }
        }
    }
}
