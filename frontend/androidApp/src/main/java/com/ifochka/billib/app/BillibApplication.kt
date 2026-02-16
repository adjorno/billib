package com.ifochka.billib.app

import android.app.Application
import com.ifochka.billib.di.appModule
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
