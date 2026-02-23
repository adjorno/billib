package com.ifochka.m14n

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
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
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.ifochka.m14n.navigation.Navigator
import com.ifochka.m14n.navigation.RouteHistory
import com.ifochka.m14n.navigation.RouteHome
import com.ifochka.m14n.navigation.rememberNavigationState
import com.ifochka.m14n.navigation.toEntries
import com.ifochka.m14n.ui.chart.ChartScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            val topLevelRoutes = setOf(RouteHome, RouteHistory)
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
                            selected = navigationState.topLevelRoute == RouteHistory,
                            onClick = { navigator.navigate(RouteHistory) },
                            icon = { Icon(imageVector = Icons.Default.BarChart, contentDescription = "History") },
                            label = { Text("History") },
                        )
                    }
                },
            ) { paddingValues ->
                NavDisplay(
                    entries = navigationState.toEntries(
                        entryProvider {
                            entry<RouteHome> {
                                Text("Home — coming soon")
                            }
                            entry<RouteHistory> {
                                ChartScreen()
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
