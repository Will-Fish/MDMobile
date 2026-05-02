package com.example.mdmobile.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mdmobile.ExternalOpenRequest
import com.example.mdmobile.data.model.MarkdownFile
import com.example.mdmobile.data.model.ThemeMode
import com.example.mdmobile.data.model.UserPreferences
import com.example.mdmobile.ui.components.BottomNavItem
import com.example.mdmobile.ui.components.MDMobileBottomNavigation
import com.example.mdmobile.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URLDecoder
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MainScreen(
    userPreferences: UserPreferences,
    onUpdateThemeMode: (ThemeMode) -> Unit,
    onUpdateFontSize: (Int) -> Unit,
    onUpdateDefaultFolder: (String?) -> Unit,
    externalOpenRequest: ExternalOpenRequest? = null,
    onExternalOpenHandled: (Long) -> Unit = {},
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val selectedRoute = when {
        currentRoute == "files/{path}" -> BottomNavItem.FILES.route
        else -> currentRoute
    }

    val bottomNavRoutes = setOf(
        BottomNavItem.HOME.route,
        BottomNavItem.FILES.route,
        BottomNavItem.BOOKMARKS.route,
        BottomNavItem.RECENT.route,
        BottomNavItem.SETTINGS.route
    )

    LaunchedEffect(externalOpenRequest?.id) {
        val request = externalOpenRequest ?: return@LaunchedEffect
        val importedPath = withContext(Dispatchers.IO) {
            FileUtils.importMarkdownFromUri(context, request.uri)
        }
        if (importedPath == null) {
            Toast.makeText(context, "无法打开该 Markdown 文件", Toast.LENGTH_SHORT).show()
        } else {
            navController.navigate(readerRoute(importedPath)) {
                launchSingleTop = true
            }
        }
        onExternalOpenHandled(request.id)
    }

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomNavRoutes || currentRoute == "files/{path}") {
                MDMobileBottomNavigation(
                    currentRoute = selectedRoute,
                    onItemClick = { item ->
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
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
            MainNavHost(
                navController = navController,
                userPreferences = userPreferences,
                onUpdateThemeMode = onUpdateThemeMode,
                onUpdateFontSize = onUpdateFontSize,
                onUpdateDefaultFolder = onUpdateDefaultFolder
            )
        }
    }
}

@Composable
private fun MainNavHost(
    navController: NavHostController,
    userPreferences: UserPreferences,
    onUpdateThemeMode: (ThemeMode) -> Unit,
    onUpdateFontSize: (Int) -> Unit,
    onUpdateDefaultFolder: (String?) -> Unit
) {
    val appDocumentFolder = FileUtils.getAppDocumentDirectory(LocalContext.current).absolutePath

    NavHost(
        navController = navController,
        startDestination = BottomNavItem.HOME.route
    ) {
        composable(BottomNavItem.HOME.route) {
            HomeScreen(
                userPreferences = userPreferences,
                onFileClick = { file -> openMarkdownTarget(navController, file) },
                onBookmarksClick = { navController.navigate(BottomNavItem.BOOKMARKS.route) },
                onRecentFilesClick = { navController.navigate(BottomNavItem.RECENT.route) },
                onBrowseFilesClick = {
                    val target = userPreferences.defaultFolder
                    if (target.isNullOrBlank()) {
                        navController.navigate(BottomNavItem.FILES.route)
                    } else {
                        navController.navigate(fileRoute(target))
                    }
                },
                onQuickNoteClick = {
                    navController.navigate(
                        readerRoute(
                            path = buildDraftPath(userPreferences.defaultFolder, appDocumentFolder),
                            isNewFile = true
                        )
                    )
                }
            )
        }

        composable(BottomNavItem.FILES.route) {
            FileBrowserScreen(
                currentPath = userPreferences.defaultFolder,
                defaultFolder = userPreferences.defaultFolder ?: appDocumentFolder,
                onFileClick = { file -> openMarkdownTarget(navController, file) },
                onNavigateUp = {
                    navController.navigate(BottomNavItem.HOME.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onBookmarksClick = { navController.navigate(BottomNavItem.BOOKMARKS.route) },
                onRecentFilesClick = { navController.navigate(BottomNavItem.RECENT.route) },
                onCreateFile = { filePath ->
                    navController.navigate(
                        readerRoute(
                            path = filePath,
                            isNewFile = true
                        )
                    )
                }
            )
        }

        composable(
            route = "files/{path}",
            arguments = listOf(navArgument("path") { type = NavType.StringType })
        ) { backStackEntry ->
            val path = backStackEntry.arguments?.getString("path")?.decode()
            FileBrowserScreen(
                currentPath = path,
                defaultFolder = userPreferences.defaultFolder ?: appDocumentFolder,
                onFileClick = { file -> openMarkdownTarget(navController, file) },
                onNavigateUp = { navController.navigateUp() },
                onBookmarksClick = { navController.navigate(BottomNavItem.BOOKMARKS.route) },
                onRecentFilesClick = { navController.navigate(BottomNavItem.RECENT.route) },
                onCreateFile = { filePath ->
                    navController.navigate(
                        readerRoute(
                            path = filePath,
                            isNewFile = true
                        )
                    )
                }
            )
        }

        composable(BottomNavItem.BOOKMARKS.route) {
            BookmarksScreen(
                onBookmarkClick = { bookmark ->
                    navController.navigate(readerRoute(bookmark.filePath))
                }
            )
        }

        composable(BottomNavItem.RECENT.route) {
            RecentFilesScreen(
                onFileClick = { progress ->
                    navController.navigate(readerRoute(progress.filePath))
                }
            )
        }

        composable(
            route = "reader/{path}?new={new}",
            arguments = listOf(
                navArgument("path") { type = NavType.StringType },
                navArgument("new") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val path = backStackEntry.arguments?.getString("path")?.decode()
            val isNewFile = backStackEntry.arguments?.getBoolean("new") == true
            ReaderScreen(
                filePath = path,
                userPreferences = userPreferences,
                isNewFile = isNewFile,
                onBack = { navController.navigateUp() },
                onFontSizeChange = onUpdateFontSize
            )
        }

        composable(BottomNavItem.SETTINGS.route) {
            SettingsScreen(
                userPreferences = userPreferences,
                onUpdateThemeMode = onUpdateThemeMode,
                onUpdateFontSize = onUpdateFontSize,
                onUpdateDefaultFolder = onUpdateDefaultFolder,
                onPrivacyPolicyClick = { navController.navigate("privacy_policy") }
            )
        }

        composable("privacy_policy") {
            PrivacyPolicyScreen(navController = navController)
        }
    }
}

private fun openMarkdownTarget(navController: NavHostController, file: MarkdownFile) {
    if (file.isDirectory) {
        navController.navigate(fileRoute(file.path))
    } else {
        navController.navigate(readerRoute(file.path))
    }
}

private fun fileRoute(path: String): String {
    return "files/${path.encode()}"
}

internal fun readerRoute(path: String, isNewFile: Boolean = false): String {
    return "reader/${path.encode()}?new=$isNewFile"
}

private fun buildDraftPath(preferredDirectory: String?, fallbackDirectory: String): String {
    val directory = preferredDirectory
        ?.takeIf { it.isNotBlank() }
        ?: fallbackDirectory
    val timestamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date())
    return File(directory, "note-$timestamp.md").absolutePath
}

private fun String.encode(): String = URLEncoder.encode(this, "UTF-8")

private fun String.decode(): String = URLDecoder.decode(this, "UTF-8")
