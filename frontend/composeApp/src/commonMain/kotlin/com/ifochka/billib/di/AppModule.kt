package com.ifochka.billib.di

import com.ifochka.billib.BuildKonfig
import com.ifochka.billib.data.api.BillibApi
import com.ifochka.billib.data.api.KtorBillibApi
import com.ifochka.billib.data.db.ChartDatabaseRepository
import com.ifochka.billib.data.db.SqlDelightChartDatabase
import com.ifochka.billib.data.db.createDatabaseDriver
import com.ifochka.billib.data.repository.ChartRepository
import com.ifochka.billib.db.BillibDatabase
import com.ifochka.billib.ui.chart.ChartViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    },
                )
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
    singleOf(::ChartRepository)
    factoryOf(::ChartViewModel)
}
