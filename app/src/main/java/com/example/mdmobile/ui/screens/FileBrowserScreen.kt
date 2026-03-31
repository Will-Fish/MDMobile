package com.example.mdmobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mdmobile.R
import com.example.mdmobile.data.model.MarkdownFile
import com.example.mdmobile.ui.components.FileItem
import com.example.mdmobile.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

enum class SortOption {
    NAME_ASC,
    NAME_DESC,
    DATE_ASC,
    DATE_DESC
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileBrowserScreen(
    currentPath: String? = null,
    onFileClick: (MarkdownFile) -> Unit = {},
    onNavigateUp: () -> Unit = {},
    onBookmarksClick: () -> Unit = {},
    onRecentFilesClick: () -> Unit = {}
) {
    var files by remember { mutableStateOf<List<MarkdownFile>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf(SortOption.NAME_ASC) }
    var showSortMenu by remember { mutableStateOf(false) }

    val isPreviewMode = LocalInspectionMode.current

    LaunchedEffect(currentPath) {
        isLoading = true
        files = if (isPreviewMode) {
            // Mock data for preview
            listOf(
                MarkdownFile(
                    name = "文档",
                    path = "/sdcard/Documents",
                    size = 0,
                    lastModified = Date(),
                    isDirectory = true
                ),
                MarkdownFile(
                    name = "README.md",
                    path = "/sdcard/Documents/README.md",
                    size = 2048,
                    lastModified = Date(),
                    isDirectory = false
                ),
                MarkdownFile(
                    name = "笔记",
                    path = "/sdcard/Notes",
                    size = 0,
                    lastModified = Date(),
                    isDirectory = true
                ),
                MarkdownFile(
                    name = "项目计划.md",
                    path = "/sdcard/Documents/项目计划.md",
                    size = 4096,
                    lastModified = Date(),
                    isDirectory = false
                )
            )
        } else {
            withContext(Dispatchers.IO) {
                val allFiles = FileUtils.listFilesInDirectory(currentPath)
                FileUtils.filterMarkdownFiles(allFiles)
            }
        }
        isLoading = false
    }

    // Apply sorting and filtering
    val displayedFiles = remember(files, searchQuery, sortOption) {
        val filteredFiles = files ?: emptyList()
        val searchedFiles = if (searchQuery.isNotBlank()) {
            FileUtils.searchFiles(filteredFiles, searchQuery)
        } else {
            filteredFiles
        }

        when (sortOption) {
            SortOption.NAME_ASC -> FileUtils.sortFilesByName(searchedFiles, ascending = true)
            SortOption.NAME_DESC -> FileUtils.sortFilesByName(searchedFiles, ascending = false)
            SortOption.DATE_ASC -> FileUtils.sortFilesByDate(searchedFiles, ascending = true)
            SortOption.DATE_DESC -> FileUtils.sortFilesByDate(searchedFiles, ascending = false)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        SearchBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            onSearch = { isSearchActive = false },
                            active = isSearchActive,
                            onActiveChange = { isSearchActive = it },
                            placeholder = { Text(stringResource(R.string.search_hint)) },
                            modifier = Modifier.fillMaxSize()
                        ) {}
                    } else {
                        val displayName = currentPath?.let { path ->
                            val file = java.io.File(path)
                            if (file.name.isNotBlank()) file.name else path
                        } ?: stringResource(R.string.files)
                        Text(text = displayName)
                    }
                },
                navigationIcon = {
                    if (currentPath != null) {
                        IconButton(onClick = onNavigateUp) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onBookmarksClick) {
                        Icon(Icons.Default.Bookmark, contentDescription = "书签")
                    }
                    IconButton(onClick = onRecentFilesClick) {
                        Icon(Icons.Default.History, contentDescription = "最近文件")
                    }
                    if (!isSearchActive) {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = "搜索")
                        }
                    }
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Default.Sort, contentDescription = "排序")
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sort_by_name)) },
                            onClick = {
                                sortOption = if (sortOption == SortOption.NAME_ASC)
                                    SortOption.NAME_DESC else SortOption.NAME_ASC
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sort_by_date)) },
                            onClick = {
                                sortOption = if (sortOption == SortOption.DATE_ASC)
                                    SortOption.DATE_DESC else SortOption.DATE_ASC
                                showSortMenu = false
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                displayedFiles.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_files),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (searchQuery.isNotBlank()) {
                            Text(
                                text = "搜索: $searchQuery",
                                modifier = Modifier.padding(top = 8.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn {
                        itemsIndexed(
                            items = displayedFiles,
                            key = { _, file -> file.path }
                        ) { index, file ->
                            FileItem(
                                file = file,
                                onClick = { onFileClick(file) },
                                index = index
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FileBrowserScreenPreview() {
    FileBrowserScreen(
        currentPath = "/sdcard/Documents",
        onFileClick = {},
        onNavigateUp = {}
    )
}