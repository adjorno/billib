package com.ifochka.m14n

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.ifochka.m14n.data.auth.initFirebase
import com.ifochka.m14n.data.db.initializeDatabaseDriver
import com.ifochka.m14n.di.appModule
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    MainScope().launch {
        initFirebase()
        initializeDatabaseDriver()

        startKoin {
            modules(appModule)
        }

        ComposeViewport {
            App()
        }
    }
}
