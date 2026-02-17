package com.ifochka.billib.data.artwork

import io.ktor.client.HttpClient

/**
 * JVM implementation uses regular HTTP client.
 * No CORS restrictions on desktop.
 */
actual fun createArtworkApi(httpClient: HttpClient): ArtworkApi {
    println("[ARTWORK-FACTORY] Creating ITunesArtworkApi for JVM platform")
    return ITunesArtworkApi(httpClient)
}
