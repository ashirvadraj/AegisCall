package com.aegiscall.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.aegiscall.app.MainActivity

object NotificationHelper {
    const val CHANNEL_CALLER_ID = "channel_caller_id"
    const val CHANNEL_SPAM_BLOCKED = "channel_spam_blocked"
    const val CHANNEL_OTP_ALERTS = "channel_otp_alerts"

    fun initChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val callerIdChannel = NotificationChannel(
                CHANNEL_CALLER_ID,
                "Caller ID Heads-Up",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time identification of incoming calls"
            }

            val spamChannel = NotificationChannel(
                CHANNEL_SPAM_BLOCKED,
                "Spam Call Shield",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alerts for automatically dropped spam and robocalls"
            }

            val otpChannel = NotificationChannel(
                CHANNEL_OTP_ALERTS,
                "Smart OTP Auto-Copy",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Instant one-tap OTP copy notifications"
            }

            manager.createNotificationChannel(callerIdChannel)
            manager.createNotificationChannel(spamChannel)
            manager.createNotificationChannel(otpChannel)
        }
    }

    fun showSpamBlockedNotification(context: Context, number: String, reason: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_SPAM_BLOCKED)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("??? Aegis Shield Blocked Spam Call")
            .setContentText("Silently dropped call from $number ($reason)")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        manager.notify((System.currentTimeMillis() % 10000).toInt(), notification)
    }

    fun showOtpNotification(context: Context, sender: String, otpCode: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val copyIntent = Intent(context, OtpCopyReceiver::class.java).apply {
            putExtra("otp_code", otpCode)
        }
        val copyPendingIntent = PendingIntent.getBroadcast(
            context, otpCode.hashCode(), copyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_OTP_ALERTS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("?? OTP Code: $otpCode")
            .setContentText("From $sender ? Tap to copy to clipboard")
            .addAction(android.R.drawable.ic_menu_save, "Copy Code ($otpCode)", copyPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(8888, notification)
    }
}
