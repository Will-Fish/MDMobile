package com.example.mdmobile.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mdmobile.R
import com.example.mdmobile.data.model.MarkdownFile
import com.example.mdmobile.data.model.UserPreferences
import com.example.mdmobile.viewmodels.BookmarkViewModel
import com.example.mdmobile.viewmodels.ReadingProgressViewModel
import java.text.SimpleDateFormat
import java.util.Locale

private data class HomeAction(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun HomeScreen(
    userPreferences: UserPreferences,
    onFileClick: (MarkdownFile) -> Unit,
    onBookmarksClick: () -> Unit,
    onRecentFilesClick: () -> Unit,
    onBrowseFilesClick: () -> Unit,
    onQuickNoteClick: () -> Unit
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

    val actions = listOf(
        HomeAction(
            title = stringResource(R.string.browse_files),
            subtitle = "进入你的文档目录，继续整理和阅读",
            icon = Icons.Default.FolderOpen,
            onClick = onBrowseFilesClick
        ),
        HomeAction(
            title = stringResource(R.string.bookmarks),
            subtitle = "快速回到收藏过的重要内容",
            icon = Icons.Default.Bookmark,
            onClick = onBookmarksClick
        ),
        HomeAction(
            title = stringResource(R.string.recent_files),
            subtitle = "从上次中断的地方继续阅读",
            icon = Icons.Default.History,
            onClick = onRecentFilesClick
        ),
        HomeAction(
            title = stringResource(R.string.quick_note),
            subtitle = "立刻新建一篇 Markdown 笔记",
            icon = Icons.AutoMirrored.Filled.NoteAdd,
            onClick = onQuickNoteClick
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            HeroCard(
                userPreferences = userPreferences,
                recentCount = recentFiles.size,
                bookmarkCount = bookmarks.size
            )
        }

        item {
            Text(
                text = "快速操作",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                actions.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEach { action ->
                            ActionCard(
                                action = action,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        item {
            SectionHeader(
                title = "最近打开",
                actionText = if (recentFiles.isNotEmpty()) "查看全部" else null,
                onActionClick = onRecentFilesClick
            )
        }

        if (recentFiles.isEmpty()) {
            item { EmptyCard(text = stringResource(R.string.recent_empty_hint)) }
        } else {
            items(recentFiles.take(3), key = { it.filePath }) { progress ->
                SummaryRow(
                    title = progress.fileName,
                    subtitle = "上次打开 ${formatDate(progress.lastAccessed)} · 进度 ${progress.displayProgress}",
                    icon = Icons.Default.Description,
                    onClick = {
                        onFileClick(
                            MarkdownFile(
                                name = progress.fileName,
                                path = progress.filePath,
                                size = 0,
                                lastModified = progress.lastAccessed,
                                isDirectory = false
                            )
                        )
                    }
                )
            }
        }

        item {
            SectionHeader(
                title = "收藏书签",
                actionText = if (bookmarks.isNotEmpty()) "查看全部" else null,
                onActionClick = onBookmarksClick
            )
        }

        if (bookmarks.isEmpty()) {
            item { EmptyCard(text = stringResource(R.string.bookmarks_empty_hint)) }
        } else {
            items(bookmarks.take(3), key = { it.id }) { bookmark ->
                SummaryRow(
                    title = bookmark.fileName,
                    subtitle = bookmark.note?.takeIf { it.isNotBlank() }
                        ?: "收藏于 ${formatDate(bookmark.createdAt)}",
                    icon = Icons.Default.Bookmark,
                    onClick = {
                        onFileClick(
                            MarkdownFile(
                                name = bookmark.fileName,
                                path = bookmark.filePath,
                                size = 0,
                                lastModified = bookmark.lastAccessed,
                                isDirectory = false
                            )
                        )
                    }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(6.dp)) }
    }
}

@Composable
private fun HeroCard(
    userPreferences: UserPreferences,
    recentCount: Int,
    bookmarkCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0E3570),
                            Color(0xFF164C97),
                            Color(0xFF2E76D6)
                        )
                    )
                )
                .padding(22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "鱼尾猩.md",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "鱼尾猩自用markdown阅读与编辑器",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFD6E7FF),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Row(
                        modifier = Modifier.padding(top = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatPill(title = "字号", value = "${userPreferences.fontSize}px")
                        StatPill(title = "最近", value = recentCount.toString())
                        StatPill(title = "书签", value = bookmarkCount.toString())
                    }
                }

                Image(
                    painter = painterResource(id = R.drawable.brand_monkey),
                    contentDescription = "鱼尾猩",
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .width(108.dp)
                        .size(108.dp)
                )
            }
        }
    }
}

@Composable
private fun StatPill(title: String, value: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFCAE0FF)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun ActionCard(
    action: HomeAction,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = action.onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = action.title,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = action.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 14.dp)
            )
            Text(
                text = action.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionText: String?,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        if (actionText != null) {
            Text(
                text = actionText,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.clickable(onClick = onActionClick)
            )
        }
    }
}

@Composable
private fun EmptyCard(text: String) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(18.dp)
        )
    }
}

@Composable
private fun SummaryRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

private fun formatDate(date: java.util.Date): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(date)
}
