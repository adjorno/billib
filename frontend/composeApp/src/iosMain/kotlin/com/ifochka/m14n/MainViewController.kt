package com.ifochka.m14n

import androidx.compose.ui.window.ComposeUIViewController
import com.ifochka.auth.FirebaseAuthConfig
import com.ifochka.auth.initFirebase
import com.ifochka.m14n.di.appModule
import org.koin.core.context.startKoin
import platform.UIKit.UIViewController

@Suppress("FunctionName")
fun MainViewController(): UIViewController {
    initFirebase(
        config = FirebaseAuthConfig(
            apiKey = BuildKonfig.FIREBASE_API_KEY,
            projectId = BuildKonfig.FIREBASE_PROJECT_ID,
            appId = BuildKonfig.FIREBASE_APP_ID,
            authDomain = BuildKonfig.FIREBASE_AUTH_DOMAIN,
            googleWebClientId = BuildKonfig.GOOGLE_WEB_CLIENT_ID,
            useEmulator = BuildKonfig.USE_FIREBASE_EMULATOR,
        ),
    )
    runCatching { startKoin { modules(appModule) } }
    return ComposeUIViewController { App() }
}
