package com.ifochka.billib

import io.ktor.client.HttpClient

interface Platform {
    val name: String

    companion object {
        const val ANDROID = "android"
        const val JS = "web/js"
        const val WASM = "web/wasm"
        const val DESKTOP = "desktop"
    }
}

expect fun getPlatform(): Platform

expect fun createHttpClient(): HttpClient
