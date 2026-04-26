package com.example.mdmobile.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.mdmobile.R

enum class BottomNavItem(
    val route: String,
    val labelResId: Int,
    val icon: @Composable () -> Unit
) {
    HOME("home", R.string.home, { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.home)) }),
    FILES("files_root", R.string.files, { Icon(Icons.Default.Folder, contentDescription = stringResource(R.string.files)) }),
    BOOKMARKS("bookmarks", R.string.bookmarks, { Icon(Icons.Default.Bookmark, contentDescription = stringResource(R.string.bookmarks)) }),
    RECENT("recent", R.string.recent_files, { Icon(Icons.Default.History, contentDescription = stringResource(R.string.recent_files)) }),
    SETTINGS("settings", R.string.settings, { Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings)) })
}

@Composable
fun MDMobileBottomNavigation(
    currentRoute: String?,
    onItemClick: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 10.dp,
        shadowElevation = 8.dp
    ) {
        NavigationBar(
            modifier = Modifier
                .height(74.dp)
                .clip(RoundedCornerShape(28.dp)),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            BottomNavItem.entries.forEach { item ->
                NavigationBarItem(
                    selected = currentRoute == item.route,
                    onClick = { onItemClick(item) },
                    icon = item.icon,
                    label = {
                        Text(
                            text = stringResource(item.labelResId),
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}
