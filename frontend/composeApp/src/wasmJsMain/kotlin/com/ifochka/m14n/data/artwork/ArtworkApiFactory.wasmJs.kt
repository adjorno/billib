package com.ifochka.m14n.data.artwork

import io.ktor.client.HttpClient

/**
 * wasmJs implementation uses JSONP to bypass CORS.
 * HttpClient is not used for JSONP, but kept for API compatibility.
 */
actual fun createArtworkApi(httpClient: HttpClient): ArtworkApi {
    println("[ARTWORK-FACTORY] Creating ITunesJsonpApi for wasmJs platform (JSONP to bypass CORS)")
    return ITunesJsonpApi()
}
