package com.ifochka.m14n

import androidx.compose.ui.window.ComposeUIViewController
import com.ifochka.m14n.data.auth.initFirebase
import com.ifochka.m14n.di.appModule
import org.koin.core.context.startKoin
import platform.UIKit.UIViewController

@Suppress("FunctionName")
fun MainViewController(): UIViewController {
    initFirebase()
    runCatching { startKoin { modules(appModule) } }
    return ComposeUIViewController { App() }
}
