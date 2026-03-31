package com.example.mdmobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mdmobile.data.model.MarkdownFile
import com.example.mdmobile.viewmodels.BookmarkViewModel
import com.example.mdmobile.viewmodels.ReadingProgressViewModel

data class QuickAction(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val route: String,
    val color: Color
)

@Composable
fun HomeScreen(
    onFileClick: (MarkdownFile) -> Unit = {},
    onBookmarksClick: () -> Unit = {},
    onRecentFilesClick: () -> Unit = {},
    onBrowseFilesClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val bookmarkViewModel: BookmarkViewModel = viewModel(
        factory = BookmarkViewModel.provideFactory(context)
    )
    val progressViewModel: ReadingProgressViewModel = viewModel(
        factory = ReadingProgressViewModel.provideFactory(context)
    )

    val bookmarks by bookmarkViewModel.bookmarks.collectAsState(initial = emptyList())
    val recentFiles by progressViewModel.recentFiles.collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        bookmarkViewModel.loadBookmarks()
        progressViewModel.loadRecentFiles()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            // Welcome header
            Column {
                Text(
                    text = "欢迎使用",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    text = "MDMobile Markdown阅读器",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = "简洁高效的文档管理工具",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        item {
            // Quick actions
            Text(
                text = "快速操作",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        item {
            QuickActionsGrid(
                onBookmarksClick = onBookmarksClick,
                onRecentFilesClick = onRecentFilesClick,
                onBrowseFilesClick = onBrowseFilesClick
            )
        }

        // Recent files section
        if (recentFiles.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "最近打开",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "查看更多",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(onClick = onRecentFilesClick)
                    )
                }
            }

            items(
                items = recentFiles.take(3),
                key = { it.filePath }
            ) { progress ->
                RecentFileItem(
                    progress = progress,
                    onClick = { onFileClick(
                        MarkdownFile(
                            name = progress.fileName,
                            path = progress.filePath,
                            size = 0,
                            lastModified = progress.lastAccessed,
                            isDirectory = false
                        )
                    ) }
                )
            }
        }

        // Bookmarks section
        if (bookmarks.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "收藏的书签",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "查看全部",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(onClick = onBookmarksClick)
                    )
                }
            }

            items(
                items = bookmarks.take(3),
                key = { it.id }
            ) { bookmark ->
                BookmarkPreviewItem(
                    bookmark = bookmark,
                    onClick = { onFileClick(
                        MarkdownFile(
                            name = bookmark.fileName,
                            path = bookmark.filePath,
                            size = 0,
                            lastModified = bookmark.createdAt,
                            isDirectory = false
                        )
                    ) }
                )
            }
        }
    }
}

@Composable
fun QuickActionsGrid(
    onBookmarksClick: () -> Unit,
    onRecentFilesClick: () -> Unit,
    onBrowseFilesClick: () -> Unit
) {
    val quickActions = listOf(
        QuickAction(
            title = "浏览文件",
            description = "查看和管理文档",
            icon = Icons.Default.Folder,
            route = "files_root",
            color = MaterialTheme.colorScheme.primary
        ),
        QuickAction(
            title = "我的书签",
            description = "收藏的重要文档",
            icon = Icons.Default.Bookmark,
            route = "bookmarks",
            color = MaterialTheme.colorScheme.secondary
        ),
        QuickAction(
            title = "最近文件",
            description = "快速继续阅读",
            icon = Icons.Default.History,
            route = "recent",
            color = MaterialTheme.colorScheme.tertiary
        ),
        QuickAction(
            title = "快速笔记",
            description = "新建Markdown",
            icon = Icons.Default.InsertDriveFile,
            route = "new_note",
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            quickActions.take(2).forEach { action ->
                QuickActionCard(
                    action = action,
                    onClick = when (action.title) {
                        "浏览文件" -> onBrowseFilesClick
                        "我的书签" -> onBookmarksClick
                        "最近文件" -> onRecentFilesClick
                        else -> { {} }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            quickActions.drop(2).forEach { action ->
                QuickActionCard(
                    action = action,
                    onClick = when (action.title) {
                        "浏览文件" -> onBrowseFilesClick
                        "我的书签" -> onBookmarksClick
                        "最近文件" -> onRecentFilesClick
                        else -> { {} }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun QuickActionCard(
    action: QuickAction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = action.color.copy(alpha = 0.1f),
            contentColor = action.color
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column {
                Icon(
                    imageVector = action.icon,
                    contentDescription = action.title,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = action.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = action.description,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
fun RecentFileItem(
    progress: com.example.mdmobile.data.model.ReadingProgress,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.InsertDriveFile,
                contentDescription = "文件",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = progress.fileName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "上次访问: ${formatDate(progress.lastAccessed)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            if (progress.percentage > 0) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${(progress.percentage * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun BookmarkPreviewItem(
    bookmark: com.example.mdmobile.data.model.Bookmark,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "书签",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(24.dp)
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = bookmark.fileName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                bookmark.note?.let { note ->
                    if (note.isNotBlank()) {
                        Text(
                            text = note,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                Text(
                    text = "创建于: ${formatDate(bookmark.createdAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

private fun formatDate(date: java.util.Date): String {
    val now = java.util.Date()
    val diff = now.time - date.time
    val minutes = diff / (1000 * 60)
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 1 -> "刚刚"
        hours < 1 -> "${minutes}分钟前"
        days < 1 -> "${hours}小时前"
        days < 7 -> "${days}天前"
        else -> {
            val sdf = java.text.SimpleDateFormat("MM-dd", java.util.Locale.getDefault())
            sdf.format(date)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(
            onFileClick = {},
            onBookmarksClick = {},
            onRecentFilesClick = {}
        )
    }
}