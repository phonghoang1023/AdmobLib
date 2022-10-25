package com.smartmobile.example

import android.app.Application
import com.smartmobile.admob.Admob

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        Admob.init(this, true, true)
        registerActivityLifecycleCallbacks(ActivityLifecycleListener)
    }

    companion object {
        lateinit var instance: MainApplication
            private set
    }
}