package com.ifochka.m14n

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.ifochka.m14n.data.auth.initFirebase
import com.ifochka.m14n.di.appModule
import org.koin.core.context.startKoin

fun main() =
    application {
        initFirebase()

        // Initialize Koin
        startKoin {
            modules(appModule)
        }

        Window(
            onCloseRequest = ::exitApplication,
            title = "M14N",
            state = rememberWindowState(width = 1300.dp, height = 730.dp),
        ) {
            App()
        }
    }
