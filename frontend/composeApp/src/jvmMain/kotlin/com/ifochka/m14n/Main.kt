package com.ifochka.m14n

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.ifochka.auth.FirebaseAuthConfig
import com.ifochka.auth.initFirebase
import com.ifochka.m14n.di.appModule
import org.koin.core.context.startKoin

fun main() =
    application {
        initFirebase(
            config = FirebaseAuthConfig(
                apiKey = BuildKonfig.FIREBASE_API_KEY,
                projectId = BuildKonfig.FIREBASE_PROJECT_ID,
                appId = BuildKonfig.FIREBASE_APP_ID,
                authDomain = BuildKonfig.FIREBASE_AUTH_DOMAIN,
                googleWebClientId = BuildKonfig.GOOGLE_WEB_CLIENT_ID,
                apiBaseUrl = BuildKonfig.API_BASE_URL,
                appleServiceId = BuildKonfig.APPLE_CLIENT_ID,
                useEmulator = BuildKonfig.USE_FIREBASE_EMULATOR,
            ),
        )

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
