package com.example.mdmobile.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.mdmobile.R

enum class BottomNavItem(
    val route: String,
    val labelResId: Int,
    val icon: @Composable () -> Unit
) {
    HOME(
        route = "home",
        labelResId = R.string.home,
        icon = {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = stringResource(R.string.home)
            )
        }
    ),
    FILES(
        route = "files_root",
        labelResId = R.string.files,
        icon = {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = stringResource(R.string.files)
            )
        }
    ),
    BOOKMARKS(
        route = "bookmarks",
        labelResId = R.string.bookmarks,
        icon = {
            Icon(
                imageVector = Icons.Default.Bookmark,
                contentDescription = stringResource(R.string.bookmarks)
            )
        }
    ),
    RECENT(
        route = "recent",
        labelResId = R.string.recent_files,
        icon = {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = stringResource(R.string.recent_files)
            )
        }
    ),
    SETTINGS(
        route = "settings",
        labelResId = R.string.settings,
        icon = {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = stringResource(R.string.settings)
            )
        }
    )
}

@Composable
fun MDMobileBottomNavigation(
    currentRoute: String?,
    onItemClick: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .height(70.dp)
            .padding(horizontal = 8.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 8.dp
    ) {
        BottomNavItem.entries.forEach { item ->
            val isSelected = currentRoute == item.route

            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemClick(item) },
                icon = item.icon,
                label = {
                    Text(
                        text = stringResource(item.labelResId),
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}