package com.ifochka.billib.data.artwork

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import kotlin.coroutines.resume

/**
 * External declaration for static iTunes JSONP dispatcher (defined in index.html).
 * Calls the top-level searchItunesInternal() JavaScript function which wraps
 * window.itunesDispatcher.search().
 */
private external fun searchItunesInternal(
    term: String,
    callback: (String?) -> Unit,
)

/**
 * iTunes Search API implementation using JSONP for wasmJs target.
 * JSONP bypasses CORS restrictions by loading data as a script tag.
 *
 * Uses the "Static Bridge" pattern: static JavaScript dispatcher (in index.html)
 * handles dynamic callback registration, avoiding Kotlin/Wasm js() restrictions.
 *
 * API Documentation: https://developer.apple.com/library/archive/documentation/AudioVideo/Conceptual/iTuneSearchAPI/
 * Rate Limit: ~20 requests/minute per IP
 */
class ITunesJsonpApi : ArtworkApi {
    companion object {
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

    private val json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

    override suspend fun searchArtwork(
        artist: String,
        title: String,
    ): String? {
        println("[ITUNES-JSONP] 🌐 searchArtwork called - artist='$artist', title='$title'")

        return try {
            val searchTerm = "$artist $title"
            println("[ITUNES-JSONP] 📡 Search term: '$searchTerm'")

            val responseJson = fetchJsonp(searchTerm)

            if (responseJson == null) {
                println("[ITUNES-JSONP] ❌ No response from iTunes API")
                return null
            }

            println("[ITUNES-JSONP] 📄 Response JSON: ${responseJson.take(200)}...")

            val response = json.decodeFromString<ITunesSearchResponse>(responseJson)
            println("[ITUNES-JSONP] 📊 Found ${response.results.size} results")

            // Get the first result's artwork URL and upgrade to 600x600
            val result =
                response.results.firstOrNull()?.artworkUrl100?.let { url ->
                    println("[ITUNES-JSONP] 🎨 Original artwork URL: $url")
                    val upgraded = upgradeArtworkUrl(url)
                    println("[ITUNES-JSONP] ✨ Upgraded artwork URL: $upgraded")
                    upgraded
                }

            println("[ITUNES-JSONP] ✅ Returning result: $result")
            result
        } catch (e: Exception) {
            println("[ITUNES-JSONP] ❌ Error fetching artwork: ${e::class.simpleName} - ${e.message}")
            e.printStackTrace()
            null
        }
    }

    /**
     * Performs a JSONP request using the static iTunes dispatcher bridge.
     * Returns the JSON response as a String, or null on error.
     */
    private suspend fun fetchJsonp(searchTerm: String): String? =
        suspendCancellableCoroutine { continuation ->
            println("[ITUNES-JSONP] 🔧 Calling static iTunes dispatcher...")

            searchItunesInternal(searchTerm) { jsonString ->
                if (jsonString != null) {
                    println("[ITUNES-JSONP] ✅ Received response from bridge")
                } else {
                    println("[ITUNES-JSONP] ❌ Bridge returned null (error)")
                }
                continuation.resume(jsonString)
            }

            continuation.invokeOnCancellation {
                println("[ITUNES-JSONP] 🗑️  Request cancelled")
            }
        }
}
