package com.ifochka.m14n.data.artwork

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class AppleMusicArtworkApi(
    private val httpClient: HttpClient,
) : ArtworkApi {
    override suspend fun searchArtwork(
        artist: String,
        title: String,
    ): String? =
        search(
            term = "$artist $title",
            types = "songs",
        ) { it.results.songs?.data?.firstOrNull()?.attributes?.artwork?.url }

    override suspend fun searchArtistArtwork(artistName: String): String? =
        search(
            term = artistName,
            types = "artists",
        ) { it.results.artists?.data?.firstOrNull()?.attributes?.artwork?.url }

    private suspend fun search(
        term: String,
        types: String,
        extract: (AppleMusicSearchResponse) -> String?,
    ): String? =
        runCatching {
            val resp: AppleMusicSearchResponse = httpClient.get("v1/catalog/us/search") {
                parameter("term", term)
                parameter("types", types)
                parameter("limit", 1)
            }.body()
            extract(resp)?.replace("{w}", "600")?.replace("{h}", "600")
        }.getOrNull()
}
