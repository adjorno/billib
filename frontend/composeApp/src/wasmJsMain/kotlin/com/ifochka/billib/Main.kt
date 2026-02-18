package com.ifochka.billib

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.ifochka.billib.data.db.initializeDatabaseDriver
import com.ifochka.billib.di.appModule
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    MainScope().launch {
        initializeDatabaseDriver()

        startKoin {
            modules(appModule)
        }

        ComposeViewport {
            App()
        }
    }
}
