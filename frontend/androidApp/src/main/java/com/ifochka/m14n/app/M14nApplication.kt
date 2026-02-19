package com.ifochka.m14n.app

import android.app.Application
import com.google.firebase.messaging.FirebaseMessaging
import com.ifochka.m14n.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class M14nApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@M14nApplication)
            modules(appModule)
        }

        FirebaseMessaging.getInstance().subscribeToTopic("new-chart")
        FirebaseMessaging.getInstance().subscribeToTopic("track-of-day")
    }
}
