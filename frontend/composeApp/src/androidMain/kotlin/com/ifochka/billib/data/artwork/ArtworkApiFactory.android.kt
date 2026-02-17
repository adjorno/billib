package com.ifochka.billib.data.artwork

import io.ktor.client.HttpClient

/**
 * Android implementation uses regular HTTP client.
 * No CORS restrictions on mobile.
 */
actual fun createArtworkApi(httpClient: HttpClient): ArtworkApi {
    println("[ARTWORK-FACTORY] Creating ITunesArtworkApi for Android platform")
    return ITunesArtworkApi(httpClient)
}
