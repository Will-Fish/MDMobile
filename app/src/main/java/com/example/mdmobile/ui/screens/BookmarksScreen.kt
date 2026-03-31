package com.example.mdmobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.mdmobile.R
import com.example.mdmobile.data.model.Bookmark
import com.example.mdmobile.ui.components.BookmarkItem
import com.example.mdmobile.viewmodels.BookmarkViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    onBookmarkClick: (Bookmark) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val viewModel: BookmarkViewModel = viewModel(
        factory = BookmarkViewModel.provideFactory(LocalContext.current)
    )
    val bookmarks by viewModel.bookmarks.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadBookmarks()
    }

    // Filter bookmarks based on search query
    val displayedBookmarks = remember(bookmarks, searchQuery) {
        if (searchQuery.isBlank()) {
            bookmarks
        } else {
            bookmarks.filter { bookmark ->
                bookmark.fileName.contains(searchQuery, ignoreCase = true) ||
                        (bookmark.note?.contains(searchQuery, ignoreCase = true) == true)
            }
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
                            placeholder = { Text("搜索书签...") },
                            modifier = Modifier.fillMaxSize()
                        ) {}
                    } else {
                        Text(stringResource(R.string.bookmarks))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Bookmark, contentDescription = "书签")
                    }
                },
                actions = {
                    if (!isSearchActive) {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = "搜索")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
                    titleContentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
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
                displayedBookmarks.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "书签",
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Text(
                            text = stringResource(R.string.no_bookmarks),
                            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
                        )
                        if (searchQuery.isNotBlank()) {
                            Text(
                                text = "搜索: $searchQuery",
                                modifier = Modifier.padding(top = 8.dp),
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn {
                        items(
                            items = displayedBookmarks,
                            key = { it.id }
                        ) { bookmark ->
                            val scope = rememberCoroutineScope()
                            BookmarkItem(
                                bookmark = bookmark,
                                onClick = { onBookmarkClick(bookmark) },
                                onDelete = {
                                    scope.launch {
                                        viewModel.deleteBookmark(bookmark)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatDate(date: java.util.Date): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(date)
}

@Preview(showBackground = true)
@Composable
fun BookmarksScreenPreview() {
    androidx.compose.material3.MaterialTheme {
        BookmarksScreen(
            onBookmarkClick = {},
            onNavigateBack = {}
        )
    }
}