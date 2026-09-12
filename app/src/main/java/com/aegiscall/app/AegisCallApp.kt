package com.aegiscall.app

import android.app.Application
import com.aegiscall.app.data.AegisDatabase
import com.aegiscall.app.service.NotificationHelper

class AegisCallApp : Application() {
    val database: AegisDatabase by lazy { AegisDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.initChannels(this)
    }
}
