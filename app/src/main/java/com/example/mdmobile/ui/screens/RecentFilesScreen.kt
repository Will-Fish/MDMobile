package com.example.mdmobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mdmobile.R
import com.example.mdmobile.data.model.MarkdownFile
import com.example.mdmobile.data.model.ReadingProgress
import com.example.mdmobile.ui.components.FileItem
import com.example.mdmobile.viewmodels.ReadingProgressViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentFilesScreen(
    onFileClick: (ReadingProgress) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val viewModel: ReadingProgressViewModel = viewModel(
        factory = ReadingProgressViewModel.provideFactory(LocalContext.current)
    )
    val recentFiles by viewModel.recentFiles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadRecentFiles()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.recent_files))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
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
                recentFiles.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "最近文件",
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Text(
                            text = "暂无最近打开的文件",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                else -> {
                    LazyColumn {
                        items(
                            items = recentFiles,
                            key = { it.filePath }
                        ) { progress ->
                            // Convert ReadingProgress to MarkdownFile for display
                            val markdownFile = MarkdownFile(
                                name = progress.fileName,
                                path = progress.filePath,
                                size = 0,
                                lastModified = progress.lastAccessed,
                                isDirectory = false
                            )

                            FileItem(
                                file = markdownFile,
                                onClick = { onFileClick(progress) }
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
fun RecentFilesScreenPreview() {
    MaterialTheme {
        RecentFilesScreen(
            onFileClick = {},
            onNavigateBack = {}
        )
    }
}