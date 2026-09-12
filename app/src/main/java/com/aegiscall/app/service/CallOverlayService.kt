package com.aegiscall.app.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.aegiscall.app.R

class CallOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }

        val phoneNumber = intent?.getStringExtra("phone_number") ?: "Unknown"
        val displayName = intent?.getStringExtra("display_name") ?: "Caller"
        val spamScore = intent?.getIntExtra("spam_score", 0) ?: 0
        val category = intent?.getStringExtra("category") ?: "General"
        val location = intent?.getStringExtra("location") ?: "Unknown Location"

        showOverlay(phoneNumber, displayName, spamScore, category, location)
        return START_NOT_STICKY
    }

    private fun showOverlay(
        number: String,
        name: String,
        spamScore: Int,
        category: String,
        location: String
    ) {
        removeOverlay()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val inflater = LayoutInflater.from(this)

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 120
        }

        // Minimal layout programmatically created or inflated
        val container = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setBackgroundColor(android.graphics.Color.parseColor("#E6121212"))
            setPadding(40, 40, 40, 40)
            elevation = 24f
        }

        val title = TextView(this).apply {
            text = "??? AegisCall Verified Caller ID"
            setTextColor(android.graphics.Color.parseColor("#00E676"))
            textSize = 14f
        }

        val callerText = TextView(this).apply {
            text = name
            setTextColor(android.graphics.Color.WHITE)
            textSize = 20f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val numberText = TextView(this).apply {
            text = "$number ? $category ? $location"
            setTextColor(android.graphics.Color.LTGRAY)
            textSize = 14f
        }

        val buttonRow = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            setPadding(0, 24, 0, 0)
        }

        val closeBtn = Button(this).apply {
            text = "Dismiss"
            setBackgroundColor(android.graphics.Color.DKGRAY)
            setTextColor(android.graphics.Color.WHITE)
            setOnClickListener { removeOverlay(); stopSelf() }
        }

        val whatsappBtn = Button(this).apply {
            text = "Direct WhatsApp"
            setBackgroundColor(android.graphics.Color.parseColor("#25D366"))
            setTextColor(android.graphics.Color.WHITE)
            setOnClickListener {
                val clean = number.replace("[^0-9]".toRegex(), "")
                val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$clean")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                try {
                    startActivity(waIntent)
                } catch (e: Exception) {}
                removeOverlay()
                stopSelf()
            }
        }

        buttonRow.addView(closeBtn)
        buttonRow.addView(whatsappBtn)

        container.addView(title)
        container.addView(callerText)
        container.addView(numberText)
        container.addView(buttonRow)

        overlayView = container
        windowManager?.addView(overlayView, params)
    }

    private fun removeOverlay() {
        overlayView?.let {
            windowManager?.removeView(it)
            overlayView = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeOverlay()
    }
}
