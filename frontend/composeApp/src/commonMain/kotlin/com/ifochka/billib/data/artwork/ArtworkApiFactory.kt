package com.ifochka.billib.data.artwork

import io.ktor.client.HttpClient

/**
 * Platform-specific factory for creating ArtworkApi instances.
 * - JVM: Uses regular HTTP client (no CORS restrictions)
 * - wasmJs: Uses JSONP to bypass CORS restrictions
 */
expect fun createArtworkApi(httpClient: HttpClient): ArtworkApi
