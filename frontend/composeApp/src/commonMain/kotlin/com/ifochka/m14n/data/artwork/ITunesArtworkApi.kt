package com.ifochka.m14n.data.artwork

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

    @Suppress("TooGenericExceptionCaught") // Graceful degradation requires catching all exceptions
    override suspend fun searchArtwork(
        artist: String,
        title: String,
    ): String? {
        println("[ITUNES-API] 🌐 searchArtwork called - artist='$artist', title='$title'")

        return try {
            val searchTerm = "$artist $title"
            println("[ITUNES-API] 📡 Search term: '$searchTerm'")

            fetchWithRetry(searchTerm)
        } catch (e: Exception) {
            // Log error but don't crash - gracefully degrade to no artwork
            println("[ITUNES-API] ❌ Error fetching artwork from iTunes API: ${e::class.simpleName} - ${e.message}")
            null
        }
    }

    /**
     * Fetch artwork with exponential backoff retry logic.
     */
    @Suppress("TooGenericExceptionCaught") // Need to catch all exceptions for retry logic
    private suspend fun fetchWithRetry(searchTerm: String): String? {
        var lastException: Exception? = null

        repeat(MAX_RETRIES) { attempt ->
            try {
                println(
                    "[ITUNES-API] 🔄 Attempt ${attempt + 1}/$MAX_RETRIES - Making HTTP GET to $ITUNES_SEARCH_URL",
                )

                val response = httpClient.get(ITUNES_SEARCH_URL) {
                    parameter("term", searchTerm)
                    parameter("entity", ENTITY_SONG)
                    parameter("limit", LIMIT)
                }

                println("[ITUNES-API] 📥 HTTP Response: ${response.status}")

                // Handle rate limiting with exponential backoff
                if (response.status == HttpStatusCode.Forbidden) {
                    handleRateLimit(attempt)
                    return@repeat // Try again
                }

                // Process successful response
                val artworkUrl = processResponse(response)
                if (artworkUrl != null) {
                    return artworkUrl
                }
            } catch (e: Exception) {
                println("[ITUNES-API] ❌ Exception on attempt ${attempt + 1}: ${e::class.simpleName} - ${e.message}")
                lastException = e
                if (attempt < MAX_RETRIES - 1) {
                    val backoffMs = INITIAL_BACKOFF_MS * (1 shl attempt)
                    println("[ITUNES-API] 🔄 Request failed, retrying in ${backoffMs}ms")
                    delay(backoffMs)
                }
            }
        }

        // All retries exhausted
        println("[ITUNES-API] ❌ Failed to fetch artwork after $MAX_RETRIES attempts: ${lastException?.message}")
        return null
    }

    /**
     * Handle rate limiting with exponential backoff.
     */
    private suspend fun handleRateLimit(attempt: Int) {
        val backoffMs = INITIAL_BACKOFF_MS * (1 shl attempt) // Exponential: 1s, 2s, 4s
        println(
            "[ITUNES-API] ⚠️ Rate limit hit (403), retrying in ${backoffMs}ms (attempt ${attempt + 1}/$MAX_RETRIES)",
        )
        delay(backoffMs)
    }

    /**
     * Process HTTP response and extract artwork URL.
     */
    private suspend fun processResponse(response: io.ktor.client.statement.HttpResponse): String? {
        if (!response.status.isSuccess()) {
            println("[ITUNES-API] ❌ Non-success status: ${response.status}")
            return null
        }

        println("[ITUNES-API] 🔄 Parsing response body...")
        val searchResponse: ITunesSearchResponse = response.body()
        println("[ITUNES-API] 📊 Found ${searchResponse.results.size} results")

        // Get the first result's artwork URL and upgrade to 600x600
        val artworkUrl = searchResponse.results.firstOrNull()?.artworkUrl100?.let { url ->
            println("[ITUNES-API] 🎨 Original artwork URL: $url")
            val upgraded = upgradeArtworkUrl(url)
            println("[ITUNES-API] ✨ Upgraded artwork URL: $upgraded")
            upgraded
        }

        println("[ITUNES-API] ✅ Returning result: $artworkUrl")
        return artworkUrl
    }
}
