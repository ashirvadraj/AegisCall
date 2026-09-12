package com.aegiscall.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.aegiscall.app.data.AegisDatabase
import com.aegiscall.app.data.entity.SmsCategory
import com.aegiscall.app.data.entity.SmsMessageEntity
import com.aegiscall.app.engine.OtpExtractorEngine
import com.aegiscall.app.engine.SpamClassifierEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AegisSmsReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            if (messages.isNullOrEmpty()) return

            val sender = messages[0].displayOriginatingAddress ?: "Unknown"
            val fullBody = messages.joinToString("") { it.displayMessageBody ?: "" }

            val database = AegisDatabase.getDatabase(context)
            val report = SpamClassifierEngine.classifySms(sender, fullBody)
            val extractedOtp = OtpExtractorEngine.extractOtp(fullBody)

            scope.launch {
                database.smsDao().insertMessage(
                    SmsMessageEntity(
                        sender = sender,
                        body = fullBody,
                        category = report.category,
                        extractedOtp = extractedOtp,
                        spamScore = report.spamScore
                    )
                )

                // If OTP detected, notify with instant copy action!
                if (report.category == SmsCategory.OTP_2FA && extractedOtp != null) {
                    NotificationHelper.showOtpNotification(context, sender, extractedOtp)
                }
            }
        }
    }
}
