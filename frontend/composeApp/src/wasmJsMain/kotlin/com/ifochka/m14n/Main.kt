package com.ifochka.m14n

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.ifochka.auth.AuthRepository
import com.ifochka.auth.FirebaseAuthConfig
import com.ifochka.auth.initFirebase
import com.ifochka.m14n.data.db.initializeDatabaseDriver
import com.ifochka.m14n.di.appModule
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    MainScope().launch {
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
        initializeDatabaseDriver()

        startKoin {
            modules(appModule)
        }
        // createdAtStart = true does not eagerly initialize on wasmJs — force it explicitly.
        GlobalContext.get().get<AuthRepository>()

        ComposeViewport {
            App()
        }
    }
}
