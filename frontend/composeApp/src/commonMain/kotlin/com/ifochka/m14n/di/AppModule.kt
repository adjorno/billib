package com.ifochka.m14n.di

import com.ifochka.auth.AuthRepository
import com.ifochka.auth.createFirebaseAuthRepository
import com.ifochka.auth.getFirebaseToken
import com.ifochka.core.share.ShareManager
import com.ifochka.core.share.createShareManager
import com.ifochka.m14n.BuildKonfig
import com.ifochka.m14n.LogFlags
import com.ifochka.m14n.data.api.KtorM14nApi
import com.ifochka.m14n.data.api.M14nApi
import com.ifochka.m14n.data.artwork.AppleMusicArtworkApi
import com.ifochka.m14n.data.artwork.ArtworkApi
import com.ifochka.m14n.data.artwork.ArtworkRepository
import com.ifochka.m14n.data.artwork.ArtworkUrlPersistence
import com.ifochka.m14n.data.artwork.CachedArtworkRepository
import com.ifochka.m14n.data.artwork.createArtworkUrlPersistence
import com.ifochka.m14n.data.db.ChartDatabaseRepository
import com.ifochka.m14n.data.db.SqlDelightChartDatabase
import com.ifochka.m14n.data.db.createDatabaseDriver
import com.ifochka.m14n.data.repository.CachedChartRepository
import com.ifochka.m14n.data.repository.ChartRepository
import com.ifochka.m14n.data.repository.NetworkChartRepository
import com.ifochka.m14n.db.M14nDatabase
import com.ifochka.m14n.ui.artistdetails.ArtistDetailsViewModel
import com.ifochka.m14n.ui.auth.AuthViewModel
import com.ifochka.m14n.ui.auth.SignInViewModel
import com.ifochka.m14n.ui.bestsongs.BestSongsViewModel
import com.ifochka.m14n.ui.chart.ChartViewModel
import com.ifochka.m14n.ui.home.HomeViewModel
import com.ifochka.m14n.ui.search.SearchViewModel
import com.ifochka.m14n.ui.trackdetails.TrackDetailsViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.plugin
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
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
        }.also { client ->
            client.plugin(HttpSend).intercept { request ->
                val token = getFirebaseToken()
                val authSource = if (token != null) "FirebaseJWT(len=${token.length})" else "API_KEY_FALLBACK"
                val path = request.url.pathSegments.joinToString("/")
                if (LogFlags.HTTP) println("[HTTP] → ${request.method.value} $path auth=$authSource")
                request.headers[HttpHeaders.Authorization] = "Bearer ${token ?: BuildKonfig.FIREBASE_API_KEY}"
                val call = execute(request)
                if (LogFlags.HTTP) println("[HTTP] ← ${call.response.status.value} $path")
                call
            }
        }
    }

    single(named("appleMusicClient")) {
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
                url("https://api.music.apple.com/")
                headers.append(HttpHeaders.Authorization, "Bearer ${BuildKonfig.APPLE_MUSIC_TOKEN}")
            }
        }
    }

    single { createDatabaseDriver() }
    single { M14nDatabase(get()) }
    single<ChartDatabaseRepository> { SqlDelightChartDatabase(get()) }
    single<M14nApi> { KtorM14nApi(get()) }
    single { CoroutineScope(SupervisorJob() + Dispatchers.Main) }
    single<AuthRepository>(createdAtStart = true) {
        val api: M14nApi = get()
        createFirebaseAuthRepository(onUserSync = { api.syncUser() }, scope = get())
    }

    // Artwork dependencies
    single<ArtworkApi> { AppleMusicArtworkApi(get(named("appleMusicClient"))) }
    single<ArtworkUrlPersistence> { createArtworkUrlPersistence(get()) }
    single<ArtworkRepository> { CachedArtworkRepository(artworkApi = get(), persistence = get()) }

    // Share (platform-specific: Android has native share sheet)
    single<ShareManager> { createShareManager() }

    // Repositories
    singleOf(::NetworkChartRepository)
    singleOf(::CachedChartRepository)
    singleOf(::ChartRepository)

    // ViewModels
    factoryOf(::AuthViewModel)
    factoryOf(::SignInViewModel)
    factoryOf(::BestSongsViewModel)
    factoryOf(::ChartViewModel)
    factoryOf(::HomeViewModel)
    factoryOf(::SearchViewModel)
    factory { params -> TrackDetailsViewModel(get(), get(), params.get<Long>()) }
    factory { params -> ArtistDetailsViewModel(get(), get(), params.get<Long>()) }
}
