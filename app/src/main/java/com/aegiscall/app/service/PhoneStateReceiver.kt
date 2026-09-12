package com.aegiscall.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager

class PhoneStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)

            if (state == TelephonyManager.EXTRA_STATE_IDLE) {
                CallerAnnouncer.stop()
                val stopServiceIntent = Intent(context, CallOverlayService::class.java)
                context.stopService(stopServiceIntent)
            }
        }
    }
}
