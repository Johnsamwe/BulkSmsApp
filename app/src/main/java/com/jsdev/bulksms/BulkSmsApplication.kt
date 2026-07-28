package com.jsdev.bulksms

import android.app.Application

class BulkSmsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashHandler.install(this)
    }
}
