package com.ifochka.m14n.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(
                NotificationChannel("m14n_updates", "M14N Updates", NotificationManager.IMPORTANCE_DEFAULT),
            )
        }
    }
}
