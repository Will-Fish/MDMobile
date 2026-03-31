package com.example.mdmobile.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mdmobile.ui.components.BottomNavItem
import com.example.mdmobile.ui.components.MDMobileBottomNavigation

@Composable
fun MainScreen(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Bottom navigation routes
    val bottomNavRoutes = listOf(
        BottomNavItem.HOME.route,
        BottomNavItem.FILES.route,
        BottomNavItem.BOOKMARKS.route,
        BottomNavItem.RECENT.route,
        BottomNavItem.SETTINGS.route
    )

    // Check if current route is a bottom nav item
    val isBottomNavVisible = currentRoute in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (isBottomNavVisible) {
                MDMobileBottomNavigation(
                    currentRoute = currentRoute,
                    onItemClick = { item ->
                        if (item.route != currentRoute) {
                            navController.navigate(item.route) {
                                // Clear back stack to avoid deep navigation chains
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            MainNavHost(navController = navController)
        }
    }
}

@Composable
fun MainNavHost(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.HOME.route
    ) {
        // Home screen with quick access
        composable(BottomNavItem.HOME.route) {
            HomeScreen(
                onFileClick = { file ->
                    if (file.isDirectory) {
                        navController.navigate("files/${java.net.URLEncoder.encode(file.path, "UTF-8")}")
                    } else {
                        navController.navigate("reader/${java.net.URLEncoder.encode(file.path, "UTF-8")}")
                    }
                },
                onBookmarksClick = {
                    navController.navigate(BottomNavItem.BOOKMARKS.route)
                },
                onRecentFilesClick = {
                    navController.navigate(BottomNavItem.RECENT.route)
                },
                onBrowseFilesClick = {
                    navController.navigate(BottomNavItem.FILES.route)
                }
            )
        }

        // Files browser root
        composable(BottomNavItem.FILES.route) {
            FileBrowserScreen(
                onFileClick = { file ->
                    if (file.isDirectory) {
                        navController.navigate("files/${java.net.URLEncoder.encode(file.path, "UTF-8")}")
                    } else {
                        navController.navigate("reader/${java.net.URLEncoder.encode(file.path, "UTF-8")}")
                    }
                },
                onBookmarksClick = {
                    navController.navigate(BottomNavItem.BOOKMARKS.route)
                },
                onRecentFilesClick = {
                    navController.navigate(BottomNavItem.RECENT.route)
                }
            )
        }

        // Nested files navigation
        composable(
            "files/{path}",
            arguments = listOf(androidx.navigation.navArgument("path") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val path = backStackEntry.arguments?.getString("path")?.let { java.net.URLDecoder.decode(it, "UTF-8") }
            FileBrowserScreen(
                currentPath = path,
                onFileClick = { file ->
                    if (file.isDirectory) {
                        navController.navigate("files/${java.net.URLEncoder.encode(file.path, "UTF-8")}")
                    } else {
                        navController.navigate("reader/${java.net.URLEncoder.encode(file.path, "UTF-8")}")
                    }
                },
                onNavigateUp = {
                    navController.navigateUp()
                },
                onBookmarksClick = {
                    navController.navigate(BottomNavItem.BOOKMARKS.route)
                },
                onRecentFilesClick = {
                    navController.navigate(BottomNavItem.RECENT.route)
                }
            )
        }

        // Bookmarks screen
        composable(BottomNavItem.BOOKMARKS.route) {
            BookmarksScreen(
                onBookmarkClick = { bookmark ->
                    navController.navigate("reader/${java.net.URLEncoder.encode(bookmark.filePath, "UTF-8")}")
                },
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        // Recent files screen
        composable(BottomNavItem.RECENT.route) {
            RecentFilesScreen(
                onFileClick = { progress ->
                    navController.navigate("reader/${java.net.URLEncoder.encode(progress.filePath, "UTF-8")}")
                },
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        // Reader screen
        composable(
            "reader/{path}",
            arguments = listOf(androidx.navigation.navArgument("path") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val path = backStackEntry.arguments?.getString("path")?.let { java.net.URLDecoder.decode(it, "UTF-8") }
            ReaderScreen(
                filePath = path,
                onBack = { navController.navigateUp() }
            )
        }

        // Settings screen
        composable(BottomNavItem.SETTINGS.route) {
            SettingsScreen(navController = navController)
        }

        // Privacy policy screen
        composable("privacy_policy") {
            PrivacyPolicyScreen(navController = navController)
        }
    }
}