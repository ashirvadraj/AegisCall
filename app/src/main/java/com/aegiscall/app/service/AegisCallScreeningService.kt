package com.aegiscall.app.service

import android.content.Intent
import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import androidx.annotation.RequiresApi
import com.aegiscall.app.data.AegisDatabase
import com.aegiscall.app.data.entity.CallLogEntity
import com.aegiscall.app.data.entity.CallType
import com.aegiscall.app.data.entity.SpamRiskLevel
import com.aegiscall.app.engine.NumberLookupEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.Q)
class AegisCallScreeningService : CallScreeningService() {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onScreenCall(callDetails: Call.Details) {
        val rawNumber = callDetails.handle?.schemeSpecificPart ?: return
        val database = AegisDatabase.getDatabase(applicationContext)
        val lookupEngine = NumberLookupEngine(applicationContext)

        scope.launch {
            val isRuleBlocked = database.blockedNumberDao().findMatchingBlockRule(rawNumber)
            val callerIdentity = lookupEngine.resolveCaller(rawNumber)

            val isSpam = isRuleBlocked != null ||
                    callerIdentity.riskLevel == SpamRiskLevel.HIGH_RISK_SPAM ||
                    callerIdentity.riskLevel == SpamRiskLevel.FRAUD_SCAM

            if (isSpam) {
                // Drop and reject spam silently
                val response = CallResponse.Builder()
                    .setDisallowCall(true)
                    .setRejectCall(true)
                    .setSkipCallLog(false)
                    .setSkipNotification(true)
                    .build()

                respondToCall(callDetails, response)

                // Log as blocked spam
                database.callLogDao().insertCallLog(
                    CallLogEntity(
                        phoneNumber = rawNumber,
                        contactName = callerIdentity.displayName,
                        callType = CallType.BLOCKED_SPAM,
                        spamScore = callerIdentity.spamScore,
                        spamCategory = callerIdentity.category,
                        cityOrRegion = callerIdentity.cityOrCarrier
                    )
                )

                // Notify user discreetly
                NotificationHelper.showSpamBlockedNotification(
                    applicationContext,
                    rawNumber,
                    callerIdentity.category
                )
            } else {
                // Legitimate call: allow and start HUD overlay
                val response = CallResponse.Builder()
                    .setDisallowCall(false)
                    .setRejectCall(false)
                    .build()

                respondToCall(callDetails, response)

                // Trigger Floating HUD Caller ID Overlay
                val overlayIntent = Intent(applicationContext, CallOverlayService::class.java).apply {
                    putExtra("phone_number", rawNumber)
                    putExtra("display_name", callerIdentity.displayName)
                    putExtra("spam_score", callerIdentity.spamScore)
                    putExtra("category", callerIdentity.category)
                    putExtra("location", callerIdentity.cityOrCarrier)
                }
                applicationContext.startService(overlayIntent)
            }
        }
    }
}
