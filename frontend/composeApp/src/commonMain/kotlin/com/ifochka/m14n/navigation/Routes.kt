package com.ifochka.m14n.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Serializable
data object RouteHome : NavKey

@Serializable
data object RouteHistory : NavKey

@Serializable
data object RouteBestSongs : NavKey

@Serializable
data object RouteSearch : NavKey

@Serializable
data class RouteTrackDetails(
    val trackId: Long,
) : NavKey

val navSavedStateConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(RouteHome::class, RouteHome.serializer())
            subclass(RouteHistory::class, RouteHistory.serializer())
            subclass(RouteBestSongs::class, RouteBestSongs.serializer())
            subclass(RouteSearch::class, RouteSearch.serializer())
            subclass(RouteTrackDetails::class, RouteTrackDetails.serializer())
        }
    }
}
