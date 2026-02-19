package com.ifochka.m14n.di

import com.ifochka.m14n.BuildKonfig
import com.ifochka.m14n.data.api.KtorM14nApi
import com.ifochka.m14n.data.api.M14nApi
import com.ifochka.m14n.data.artwork.ArtworkApi
import com.ifochka.m14n.data.artwork.ArtworkRepository
import com.ifochka.m14n.data.artwork.CachedArtworkRepository
import com.ifochka.m14n.data.artwork.createArtworkApi
import com.ifochka.m14n.data.db.ChartDatabaseRepository
import com.ifochka.m14n.data.db.SqlDelightChartDatabase
import com.ifochka.m14n.data.db.createDatabaseDriver
import com.ifochka.m14n.data.repository.CachedChartRepository
import com.ifochka.m14n.data.repository.ChartRepository
import com.ifochka.m14n.data.repository.NetworkChartRepository
import com.ifochka.m14n.db.M14nDatabase
import com.ifochka.m14n.share.ShareManager
import com.ifochka.m14n.share.createShareManager
import com.ifochka.m14n.ui.chart.ChartViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    single {
        HttpClient {
            install(ContentNegotiation) {
                // iTunes API returns text/javascript instead of application/json
                val jsonConfig =
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    }
                json(jsonConfig, contentType = ContentType.Application.Json)
                json(jsonConfig, contentType = ContentType.Text.JavaScript)
            }
            defaultRequest {
                url(BuildKonfig.API_BASE_URL)
            }
        }
    }

    single { createDatabaseDriver() }
    single { M14nDatabase(get()) }
    single<ChartDatabaseRepository> { SqlDelightChartDatabase(get()) }
    single<M14nApi> { KtorM14nApi(get()) }

    // Artwork dependencies (platform-specific: JVM uses HTTP, wasmJs uses JSONP)
    single<ArtworkApi> { createArtworkApi(get()) }
    single<ArtworkRepository> { CachedArtworkRepository(get()) }

    // Share (platform-specific: Android has native share sheet)
    single<ShareManager> { createShareManager() }

    // Repositories
    singleOf(::NetworkChartRepository)
    singleOf(::CachedChartRepository)
    singleOf(::ChartRepository)

    // ViewModels
    factoryOf(::ChartViewModel)
}
