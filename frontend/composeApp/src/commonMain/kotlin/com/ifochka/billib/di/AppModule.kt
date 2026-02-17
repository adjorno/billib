package com.ifochka.billib.di

import com.ifochka.billib.BuildKonfig
import com.ifochka.billib.data.api.BillibApi
import com.ifochka.billib.data.api.KtorBillibApi
import com.ifochka.billib.data.artwork.ArtworkApi
import com.ifochka.billib.data.artwork.ArtworkRepository
import com.ifochka.billib.data.artwork.CachedArtworkRepository
import com.ifochka.billib.data.artwork.createArtworkApi
import com.ifochka.billib.data.db.ChartDatabaseRepository
import com.ifochka.billib.data.db.SqlDelightChartDatabase
import com.ifochka.billib.data.db.createDatabaseDriver
import com.ifochka.billib.data.repository.CachedChartRepository
import com.ifochka.billib.data.repository.ChartRepository
import com.ifochka.billib.data.repository.NetworkChartRepository
import com.ifochka.billib.db.BillibDatabase
import com.ifochka.billib.ui.chart.ChartViewModel
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
    single { BillibDatabase(get()) }
    single<ChartDatabaseRepository> { SqlDelightChartDatabase(get()) }
    single<BillibApi> { KtorBillibApi(get()) }

    // Artwork dependencies (platform-specific: JVM uses HTTP, wasmJs uses JSONP)
    single<ArtworkApi> { createArtworkApi(get()) }
    single<ArtworkRepository> { CachedArtworkRepository(get()) }

    // Repositories
    singleOf(::NetworkChartRepository)
    singleOf(::CachedChartRepository)
    singleOf(::ChartRepository)

    // ViewModels
    factoryOf(::ChartViewModel)
}
