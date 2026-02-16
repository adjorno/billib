package com.ifochka.billib

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.ifochka.billib.di.appModule
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // Initialize Koin
    startKoin {
        modules(appModule)
    }

    ComposeViewport {
        App()
    }
}
