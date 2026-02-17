package com.ifochka.billib.data.artwork

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.isSuccess

/**
 * iTunes Search API implementation for fetching album artwork.
 *
 * API Documentation: https://developer.apple.com/library/archive/documentation/AudioVideo/Conceptual/iTuneSearchAPI/
 * Rate Limit: ~20 requests/minute per IP
 */
class ITunesArtworkApi(
    private val httpClient: HttpClient,
) : ArtworkApi {
    companion object {
        private const val ITUNES_SEARCH_URL = "https://itunes.apple.com/search"
        private const val ENTITY_SONG = "song"
        private const val LIMIT = 1

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
            val response =
                httpClient.get(ITUNES_SEARCH_URL) {
                    parameter("term", searchTerm)
                    parameter("entity", ENTITY_SONG)
                    parameter("limit", LIMIT)
                }

            if (!response.status.isSuccess()) {
                return null
            }

            val searchResponse: ITunesSearchResponse = response.body()

            // Get the first result's artwork URL and upgrade to 600x600
            searchResponse.results.firstOrNull()?.artworkUrl100?.let { url ->
                upgradeArtworkUrl(url)
            }
        } catch (e: Exception) {
            // Log error but don't crash - gracefully degrade to no artwork
            println("Error fetching artwork from iTunes API: ${e.message}")
            null
        }
    }
}
