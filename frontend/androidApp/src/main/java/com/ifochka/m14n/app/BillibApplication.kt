package com.ifochka.m14n.app

import android.app.Application
import com.ifochka.m14n.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class BillibApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@BillibApplication)
            modules(appModule)
        }
    }
}
