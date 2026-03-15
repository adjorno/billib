package com.ifochka.m14n

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.ifochka.m14n.navigation.ListDetailScene
import com.ifochka.m14n.navigation.Navigator
import com.ifochka.m14n.navigation.RouteArtistDetails
import com.ifochka.m14n.navigation.RouteBestSongs
import com.ifochka.m14n.navigation.RouteHistory
import com.ifochka.m14n.navigation.RouteHome
import com.ifochka.m14n.navigation.RouteSearch
import com.ifochka.m14n.navigation.RouteTrackDetails
import com.ifochka.m14n.navigation.rememberListDetailSceneStrategy
import com.ifochka.m14n.navigation.rememberNavigationState
import com.ifochka.m14n.navigation.toEntries
import com.ifochka.m14n.ui.artistdetails.ArtistDetailsScreen
import com.ifochka.m14n.ui.auth.AuthViewModel
import com.ifochka.m14n.ui.auth.SignInBottomSheet
import com.ifochka.m14n.ui.bestsongs.BestSongsScreen
import com.ifochka.m14n.ui.chart.ChartScreen
import com.ifochka.m14n.ui.home.HomeScreen
import com.ifochka.m14n.ui.search.SearchScreen
import com.ifochka.m14n.ui.theme.M14nTheme
import com.ifochka.m14n.ui.trackdetails.TrackDetailsScreen
import com.ifochka.m14n.ui.user.UserWidget
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private data class NavItem(
    val route: NavKey,
    val icon: ImageVector,
    val label: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
    M14nTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
        ) {
            val authViewModel = koinViewModel<AuthViewModel>()
            val authState by authViewModel.authState.collectAsState()
            var showSignInSheet by remember { mutableStateOf(false) }
            val topLevelRoutes = remember { setOf(RouteHome, RouteBestSongs, RouteHistory, RouteSearch) }
            val navigationState = rememberNavigationState(
                startRoute = RouteHome,
                topLevelRoutes = topLevelRoutes,
            )
            val navigator = remember { Navigator(navigationState) }

            val navItems = remember {
                listOf(
                    NavItem(RouteHome, Icons.Default.Home, "Home"),
                    NavItem(RouteBestSongs, Icons.Default.Star, "Best"),
                    NavItem(RouteHistory, Icons.Default.BarChart, "History"),
                    NavItem(RouteSearch, Icons.Default.Search, "Search"),
                )
            }

            val provider: (NavKey) -> NavEntry<NavKey> = remember {
                entryProvider {
                    entry<RouteHome>(metadata = ListDetailScene.listPane()) {
                        HomeScreen(onTrackClick = { id -> navigator.navigate(RouteTrackDetails(id)) })
                    }
                    entry<RouteBestSongs>(metadata = ListDetailScene.listPane()) {
                        BestSongsScreen(onTrackClick = { id -> navigator.navigate(RouteTrackDetails(id)) })
                    }
                    entry<RouteHistory>(metadata = ListDetailScene.listPane()) {
                        ChartScreen(onTrackClick = { id -> navigator.navigate(RouteTrackDetails(id)) })
                    }
                    entry<RouteSearch>(metadata = ListDetailScene.listPane()) {
                        SearchScreen(
                            onTrackClick = { id -> navigator.navigate(RouteTrackDetails(id)) },
                            onArtistClick = { id -> navigator.navigate(RouteArtistDetails(id)) },
                        )
                    }
                    entry<RouteTrackDetails>(metadata = ListDetailScene.detailPane()) { route ->
                        TrackDetailsScreen(
                            viewModel = koinViewModel(parameters = { parametersOf(route.trackId) }),
                            onBack = { navigator.goBack() },
                            onArtistClick = { id -> navigator.navigate(RouteArtistDetails(id)) },
                        )
                    }
                    entry<RouteArtistDetails>(metadata = ListDetailScene.detailPane()) { route ->
                        ArtistDetailsScreen(
                            viewModel = koinViewModel(parameters = { parametersOf(route.artistId) }),
                            onBack = { navigator.goBack() },
                            onTrackClick = { id -> navigator.navigate(RouteTrackDetails(id)) },
                            onArtistClick = { id -> navigator.navigate(RouteArtistDetails(id)) },
                        )
                    }
                }
            }

            if (showSignInSheet) {
                SignInBottomSheet(
                    onDismiss = { showSignInSheet = false },
                    onSuccess = { showSignInSheet = false },
                )
            }

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val isWide = maxWidth >= 840.dp
                if (isWide) {
                    PermanentNavigationDrawer(
                        drawerContent = {
                            PermanentDrawerSheet {
                                Spacer(modifier = Modifier.height(12.dp))
                                navItems.forEach { item ->
                                    NavigationDrawerItem(
                                        icon = { Icon(item.icon, contentDescription = item.label) },
                                        label = { Text(item.label) },
                                        selected = navigationState.topLevelRoute == item.route,
                                        onClick = { navigator.navigate(item.route) },
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                    )
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                Column(
                                    horizontalAlignment = Alignment.Start,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                ) {
                                    UserWidget(
                                        authState = authState,
                                        onSignInClick = { showSignInSheet = true },
                                    )
                                    Text(
                                        text = BuildKonfig.VERSION_NAME,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        },
                    ) {
                        NavDisplay(
                            entries = navigationState.toEntries(provider),
                            onBack = { navigator.goBack() },
                            sceneStrategy = rememberListDetailSceneStrategy(),
                            modifier = Modifier.fillMaxSize(),
                            transitionSpec = {
                                slideInHorizontally(initialOffsetX = { it }) togetherWith ExitTransition.None
                            },
                            popTransitionSpec = {
                                EnterTransition.None togetherWith slideOutHorizontally(targetOffsetX = { it })
                            },
                            predictivePopTransitionSpec = {
                                EnterTransition.None togetherWith slideOutHorizontally(targetOffsetX = { it })
                            },
                        )
                    }
                } else {
                    val drawerState = rememberDrawerState(DrawerValue.Closed)
                    val scope = rememberCoroutineScope()
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            ModalDrawerSheet {
                                Spacer(Modifier.height(24.dp))
                                UserWidget(
                                    authState = authState,
                                    onSignInClick = {
                                        scope.launch { drawerState.close() }
                                        showSignInSheet = true
                                    },
                                    modifier = Modifier.padding(horizontal = 20.dp),
                                )
                                Spacer(Modifier.weight(1f))
                                Text(
                                    text = BuildKonfig.VERSION_NAME,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                )
                            }
                        },
                    ) {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            topBar = {
                                TopAppBar(
                                    title = {},
                                    navigationIcon = {
                                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Open menu")
                                        }
                                    },
                                )
                            },
                            bottomBar = {
                                NavigationBar {
                                    navItems.forEach { item ->
                                        NavigationBarItem(
                                            selected = navigationState.topLevelRoute == item.route,
                                            onClick = { navigator.navigate(item.route) },
                                            icon = { Icon(item.icon, contentDescription = item.label) },
                                            label = { Text(item.label) },
                                        )
                                    }
                                }
                            },
                        ) { paddingValues ->
                            NavDisplay(
                                entries = navigationState.toEntries(provider),
                                onBack = { navigator.goBack() },
                                modifier = Modifier.padding(paddingValues),
                                transitionSpec = {
                                    slideInHorizontally(initialOffsetX = { it }) togetherWith ExitTransition.None
                                },
                                popTransitionSpec = {
                                    EnterTransition.None togetherWith slideOutHorizontally(targetOffsetX = { it })
                                },
                                predictivePopTransitionSpec = {
                                    EnterTransition.None togetherWith slideOutHorizontally(targetOffsetX = { it })
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
