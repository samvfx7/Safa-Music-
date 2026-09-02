package com.example.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.SafaApplication
import com.example.ui.components.FloatingPillNavigationBar
import com.example.ui.components.MiniPlayer
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.home.HomeViewModel
import com.example.ui.screens.library.LibraryScreen
import com.example.ui.screens.library.LibraryViewModel
import com.example.ui.screens.player.FullPlayerScreen
import com.example.ui.screens.player.PlayerViewModel
import com.example.ui.screens.result.SongAnalysisDetailScreen
import com.example.ui.screens.scanner.ScannerScreen
import com.example.ui.screens.scanner.ScannerViewModel
import com.example.ui.screens.settings.PrivacyScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.settings.SettingsViewModel
import com.example.ui.theme.AmoledBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Library : Screen("library", "Library", Icons.Default.LibraryMusic)
    object Scanner : Screen("scanner", "Scanner", Icons.Default.GraphicEq)
    object Settings : Screen("settings", "Settings", Icons.Default.Tune)
}

@Composable
fun SafaNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val app = SafaApplication.instance
    val repository = app.musicRepository
    val preferencesRepository = app.preferencesRepository
    val scannerManager = app.libraryScannerManager
    val playerManager = app.playerManager

    val homeViewModel: HomeViewModel = viewModel {
        HomeViewModel(repository, preferencesRepository, scannerManager, playerManager)
    }
    val libraryViewModel: LibraryViewModel = viewModel {
        LibraryViewModel(repository, scannerManager, playerManager)
    }
    val scannerViewModel: ScannerViewModel = viewModel {
        ScannerViewModel(repository, preferencesRepository, scannerManager)
    }
    val settingsViewModel: SettingsViewModel = viewModel {
        SettingsViewModel(preferencesRepository, repository, scannerManager)
    }
    val playerViewModel: PlayerViewModel = viewModel {
        PlayerViewModel(playerManager, repository, scannerManager)
    }

    val playerState by playerViewModel.uiState.collectAsStateWithLifecycle()

    val bottomNavScreens = listOf(
        Screen.Home,
        Screen.Library,
        Screen.Scanner,
        Screen.Settings
    )

    val showBottomBar = bottomNavScreens.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (playerState.currentTrack != null) {
                        MiniPlayer(
                            currentTrack = playerState.currentTrack,
                            isPlaying = playerState.isPlaying,
                            playbackProgress = playerState.progressFraction,
                            onTogglePlayPause = { playerViewModel.togglePlayPause() },
                            onPlayNext = { playerViewModel.playNext() },
                            onClickPlayer = { navController.navigate("player") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    FloatingPillNavigationBar(
                        screens = bottomNavScreens,
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
            }
        },
        containerColor = AmoledBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        viewModel = homeViewModel,
                        onNavigateToLibrary = { filterStatus ->
                            if (filterStatus != null) {
                                libraryViewModel.setFilter(filterStatus)
                            } else {
                                libraryViewModel.setFilter("all")
                            }
                            navController.navigate(Screen.Library.route)
                        },
                        onNavigateToScanner = { navController.navigate(Screen.Scanner.route) },
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                        onTrackClick = { trackId -> navController.navigate("detail/$trackId") }
                    )
                }

                composable(Screen.Library.route) {
                    LibraryScreen(
                        viewModel = libraryViewModel,
                        onTrackClick = { trackId -> navController.navigate("detail/$trackId") }
                    )
                }

                composable(Screen.Scanner.route) {
                    ScannerScreen(
                        viewModel = scannerViewModel,
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToPrivacy = { navController.navigate("privacy") }
                    )
                }

                composable("privacy") {
                    PrivacyScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = "detail/{trackId}",
                    arguments = listOf(navArgument("trackId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val trackId = backStackEntry.arguments?.getLong("trackId") ?: 0L
                    SongAnalysisDetailScreen(
                        trackId = trackId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable("player") {
                    FullPlayerScreen(
                        viewModel = playerViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onViewDetail = { trackId -> navController.navigate("detail/$trackId") }
                    )
                }
            }
        }
    }
}
