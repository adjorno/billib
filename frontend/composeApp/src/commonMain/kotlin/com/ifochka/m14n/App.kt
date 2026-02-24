package com.ifochka.m14n

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.ifochka.m14n.navigation.Navigator
import com.ifochka.m14n.navigation.RouteBestSongs
import com.ifochka.m14n.navigation.RouteHistory
import com.ifochka.m14n.navigation.RouteHome
import com.ifochka.m14n.navigation.RouteSearch
import com.ifochka.m14n.navigation.rememberNavigationState
import com.ifochka.m14n.navigation.toEntries
import com.ifochka.m14n.ui.bestsongs.BestSongsScreen
import com.ifochka.m14n.ui.chart.ChartScreen
import com.ifochka.m14n.ui.home.HomeScreen
import com.ifochka.m14n.ui.search.SearchScreen

@Composable
@Preview
fun App() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            val topLevelRoutes = remember { setOf(RouteHome, RouteBestSongs, RouteHistory, RouteSearch) }
            val navigationState = rememberNavigationState(
                startRoute = RouteHome,
                topLevelRoutes = topLevelRoutes,
            )
            val navigator = remember { Navigator(navigationState) }

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            selected = navigationState.topLevelRoute == RouteHome,
                            onClick = { navigator.navigate(RouteHome) },
                            icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Home") },
                        )
                        NavigationBarItem(
                            selected = navigationState.topLevelRoute == RouteBestSongs,
                            onClick = { navigator.navigate(RouteBestSongs) },
                            icon = { Icon(imageVector = Icons.Default.Star, contentDescription = "Best Songs") },
                            label = { Text("Best") },
                        )
                        NavigationBarItem(
                            selected = navigationState.topLevelRoute == RouteHistory,
                            onClick = { navigator.navigate(RouteHistory) },
                            icon = { Icon(imageVector = Icons.Default.BarChart, contentDescription = "History") },
                            label = { Text("History") },
                        )
                        NavigationBarItem(
                            selected = navigationState.topLevelRoute == RouteSearch,
                            onClick = { navigator.navigate(RouteSearch) },
                            icon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
                            label = { Text("Search") },
                        )
                    }
                },
            ) { paddingValues ->
                NavDisplay(
                    entries = navigationState.toEntries(
                        remember {
                            entryProvider {
                                entry<RouteHome> {
                                    HomeScreen()
                                }
                                entry<RouteBestSongs> {
                                    BestSongsScreen()
                                }
                                entry<RouteHistory> {
                                    ChartScreen()
                                }
                                entry<RouteSearch> {
                                    SearchScreen()
                                }
                            }
                        },
                    ),
                    onBack = { navigator.goBack() },
                    modifier = Modifier.padding(paddingValues),
                )
            }
        }
    }
}
