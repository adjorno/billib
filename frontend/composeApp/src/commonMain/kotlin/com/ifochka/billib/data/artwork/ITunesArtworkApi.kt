package com.ifochka.billib.data.artwork

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.coroutines.delay

/**
 * iTunes Search API implementation for fetching album artwork.
 *
 * API Documentation: https://developer.apple.com/library/archive/documentation/AudioVideo/Conceptual/iTuneSearchAPI/
 * Rate Limit: ~20 requests/minute per IP (handles 403 with exponential backoff)
 */
class ITunesArtworkApi(
    private val httpClient: HttpClient,
) : ArtworkApi {
    companion object {
        private const val ITUNES_SEARCH_URL = "https://itunes.apple.com/search"
        private const val ENTITY_SONG = "song"
        private const val LIMIT = 1
        private const val MAX_RETRIES = 3
        private const val INITIAL_BACKOFF_MS = 1000L

        /**
         * Upgrade artwork URL to higher resolution.
         * iTunes returns 100x100 images, but we can get larger sizes by modifying the URL.
         * Example: 100x100-75.jpg -> 600x600.jpg
         */
        private fun upgradeArtworkUrl(
            url: String,
            targetSize: Int = 600,
        ): String =
            url.replace(
                regex = Regex("""\d+x\d+-?\d*\.jpg"""),
                replacement = "${targetSize}x$targetSize.jpg",
            )
    }

    override suspend fun searchArtwork(
        artist: String,
        title: String,
    ): String? {
        return try {
            val searchTerm = "$artist $title"
            var lastException: Exception? = null

            // Retry with exponential backoff on 403 (rate limit)
            repeat(MAX_RETRIES) { attempt ->
                try {
                    val response =
                        httpClient.get(ITUNES_SEARCH_URL) {
                            parameter("term", searchTerm)
                            parameter("entity", ENTITY_SONG)
                            parameter("limit", LIMIT)
                        }

                    // Handle 403 Forbidden (rate limit exceeded)
                    if (response.status == HttpStatusCode.Forbidden) {
                        val backoffMs = INITIAL_BACKOFF_MS * (1 shl attempt) // Exponential: 1s, 2s, 4s
                        println("[ARTWORK] Rate limit hit (403), retrying in ${backoffMs}ms (attempt ${attempt + 1}/$MAX_RETRIES)")
                        delay(backoffMs)
                        return@repeat // Try again
                    }

                    if (!response.status.isSuccess()) {
                        return null
                    }

                    val searchResponse: ITunesSearchResponse = response.body()

                    // Get the first result's artwork URL and upgrade to 600x600
                    return searchResponse.results.firstOrNull()?.artworkUrl100?.let { url ->
                        upgradeArtworkUrl(url)
                    }
                } catch (e: Exception) {
                    lastException = e
                    if (attempt < MAX_RETRIES - 1) {
                        val backoffMs = INITIAL_BACKOFF_MS * (1 shl attempt)
                        println("[ARTWORK] Request failed, retrying in ${backoffMs}ms: ${e.message}")
                        delay(backoffMs)
                    }
                }
            }

            // All retries exhausted
            println("[ARTWORK] Failed to fetch artwork after $MAX_RETRIES attempts: ${lastException?.message}")
            null
        } catch (e: Exception) {
            // Log error but don't crash - gracefully degrade to no artwork
            println("[ARTWORK] Error fetching artwork from iTunes API: ${e.message}")
            null
        }
    }
}
