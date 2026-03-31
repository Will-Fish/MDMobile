package com.example.mdmobile

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mdmobile.ui.screens.BookmarksScreen
import com.example.mdmobile.ui.screens.FileBrowserScreen
import com.example.mdmobile.ui.screens.HomeScreen
import com.example.mdmobile.ui.screens.MainScreen
import com.example.mdmobile.ui.screens.ReaderScreen
import com.example.mdmobile.ui.screens.RecentFilesScreen
import com.example.mdmobile.ui.screens.SplashScreen
import com.example.mdmobile.utils.Permissions

@Composable
fun MDMobileApp() {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(false) }
    var showSplash by remember { mutableStateOf(true) }

    // 检查权限（在后台进行，不影响启动画面显示）
    LaunchedEffect(Unit) {
        hasPermission = Permissions.hasStoragePermission(context)
    }

    if (showSplash) {
        SplashScreen(
            onSplashComplete = { showSplash = false },
            splashDuration = 1500L // 1.5秒
        )
    } else {
        if (!hasPermission) {
            PermissionRequestScreen {
                // Permission granted callback
                hasPermission = Permissions.hasStoragePermission(context)
            }
        } else {
            MainScreen()
        }
    }
}

@Composable
fun PermissionRequestScreen(onPermissionGranted: () -> Unit) {
    val context = LocalContext.current

    // For Android 11+ (API 30+), we need MANAGE_EXTERNAL_STORAGE permission
    // which is granted through system settings, not runtime permissions
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "需要文件管理权限",
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "MDMobile需要文件管理权限以访问您的所有Markdown文档\n\n" +
                      "对于Android 11及以上版本，请点击下方按钮跳转到系统设置页面，然后开启\"允许访问所有文件\"选项",
                modifier = Modifier.padding(vertical = 16.dp),
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
            )

            Button(
                onClick = {
                    Permissions.openStorageSettings(context)
                }
            ) {
                Text(text = "打开系统设置")
            }

            // Button to check permission after returning from settings
            Button(
                onClick = {
                    if (Permissions.hasStoragePermission(context)) {
                        onPermissionGranted()
                    }
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(text = "我已授予权限")
            }
        }
    } else {
        // For Android 10 and below, use runtime permissions
        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.values.all { it }
            if (allGranted) {
                onPermissionGranted()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "需要存储权限",
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "MDMobile需要访问您的文件以显示和管理Markdown文档",
                modifier = Modifier.padding(vertical = 16.dp),
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
            )

            Button(
                onClick = {
                    permissionLauncher.launch(Permissions.getRequiredPermissions())
                }
            ) {
                Text(text = "授予权限")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MDMobileAppPreview() {
    MDMobileApp()
}