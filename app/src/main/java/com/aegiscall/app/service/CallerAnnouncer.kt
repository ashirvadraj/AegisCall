package com.aegiscall.app.service

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

object CallerAnnouncer {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var pendingSpeech: String? = null

    fun init(context: Context) {
        if (tts == null) {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale.getDefault()
                    isInitialized = true
                    pendingSpeech?.let { speech ->
                        speak(speech)
                        pendingSpeech = null
                    }
                }
            }
        }
    }

    fun announce(context: Context, callerName: String, isSpam: Boolean) {
        init(context)
        val textToSpeak = if (isSpam) {
            "Warning: Suspected spam call from $callerName"
        } else {
            "Incoming call from $callerName"
        }

        if (isInitialized) {
            speak(textToSpeak)
        } else {
            pendingSpeech = textToSpeak
        }
    }

    private fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "CallerAnnouncement")
    }

    fun stop() {
        tts?.stop()
    }
}
