package com.example.mdmobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mdmobile.R
import com.example.mdmobile.data.model.MarkdownFile
import com.example.mdmobile.ui.components.FileItem
import com.example.mdmobile.utils.FileRenameResult
import com.example.mdmobile.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Date

private enum class SortOption {
    NAME_ASC,
    NAME_DESC,
    DATE_DESC,
    DATE_ASC
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileBrowserScreen(
    currentPath: String? = null,
    defaultFolder: String? = null,
    onFileClick: (MarkdownFile) -> Unit = {},
    onNavigateUp: () -> Unit = {},
    onBookmarksClick: () -> Unit = {},
    onRecentFilesClick: () -> Unit = {},
    onCreateFile: (String) -> Unit = {}
) {
    var files by remember { mutableStateOf<List<MarkdownFile>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf(SortOption.NAME_ASC) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var draftFileName by remember { mutableStateOf("") }
    var refreshToken by remember { mutableStateOf(0) }
    var renameTarget by remember { mutableStateOf<MarkdownFile?>(null) }
    var renameDraftName by remember { mutableStateOf("") }
    var renameError by remember { mutableStateOf<String?>(null) }
    var deleteTarget by remember { mutableStateOf<MarkdownFile?>(null) }

    val context = LocalContext.current
    val isPreviewMode = LocalInspectionMode.current
    val appDocumentFolder = remember(context) { FileUtils.getAppDocumentDirectory(context).absolutePath }
    val resolvedPath = currentPath ?: defaultFolder ?: appDocumentFolder
    val emptyFileNameMessage = stringResource(R.string.empty_file_name)
    val invalidFileNameMessage = stringResource(R.string.invalid_file_name)
    val fileNotFoundMessage = stringResource(R.string.file_not_found)
    val renameMarkdownOnlyMessage = stringResource(R.string.rename_markdown_only)
    val fileAlreadyExistsMessage = stringResource(R.string.file_already_exists)
    val renameFailedMessage = stringResource(R.string.rename_failed)

    LaunchedEffect(resolvedPath, refreshToken) {
        isLoading = true
        files = if (isPreviewMode) {
            listOf(
                MarkdownFile("README.md", "/data/user/0/com.example.mdmobile/files/md_files/README.md", 2_048, Date(), false),
                MarkdownFile("export.html", "/data/user/0/com.example.mdmobile/files/md_files/export.html", 4_096, Date(), false),
                MarkdownFile("archive.pdf", "/data/user/0/com.example.mdmobile/files/md_files/archive.pdf", 32_768, Date(), false)
            )
        } else {
            withContext(Dispatchers.IO) {
                FileUtils.filterDocumentFiles(FileUtils.listFilesInDirectory(resolvedPath))
            }
        }
        isLoading = false
    }

    val visibleFiles = remember(files, searchQuery, sortOption) {
        val searched = if (searchQuery.isBlank()) files else FileUtils.searchFiles(files, searchQuery)
        when (sortOption) {
            SortOption.NAME_ASC -> FileUtils.sortFilesByName(searched, true)
            SortOption.NAME_DESC -> FileUtils.sortFilesByName(searched, false)
            SortOption.DATE_DESC -> FileUtils.sortFilesByDate(searched, false)
            SortOption.DATE_ASC -> FileUtils.sortFilesByDate(searched, true)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = resolvedPath?.let { File(it).name.ifBlank { it } }
                            ?: stringResource(R.string.all_files),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (currentPath != null) {
                        IconButton(onClick = onNavigateUp) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        sortOption = when (sortOption) {
                            SortOption.NAME_ASC -> SortOption.NAME_DESC
                            SortOption.NAME_DESC -> SortOption.DATE_DESC
                            SortOption.DATE_DESC -> SortOption.DATE_ASC
                            SortOption.DATE_ASC -> SortOption.NAME_ASC
                        }
                    }) {
                        Icon(Icons.Default.SortByAlpha, contentDescription = stringResource(R.string.sort_by_name))
                    }
                    IconButton(onClick = onBookmarksClick) {
                        Icon(Icons.Default.Bookmark, contentDescription = stringResource(R.string.bookmarks))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            val targetDirectory = resolvedPath
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.new_note))
            }
            if (showCreateDialog) {
                AlertDialog(
                    onDismissRequest = { showCreateDialog = false },
                    title = { Text(stringResource(R.string.create_file)) },
                    text = {
                        OutlinedTextField(
                            value = draftFileName,
                            onValueChange = { draftFileName = it },
                            label = { Text(stringResource(R.string.file_name)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (draftFileName.isNotBlank()) {
                                    val finalName = if (draftFileName.endsWith(".md", true)) draftFileName else "$draftFileName.md"
                                    draftFileName = ""
                                    showCreateDialog = false
                                    onCreateFile(File(targetDirectory, finalName).absolutePath)
                                }
                            }
                        ) {
                            Text(stringResource(R.string.create_file))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCreateDialog = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search_hint))
                },
                placeholder = { Text(stringResource(R.string.search_hint)) },
                singleLine = true,
                shape = RoundedCornerShape(20.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 12.dp)
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    visibleFiles.isEmpty() -> {
                        CardEmptyState(
                            title = stringResource(R.string.no_files),
                            message = if (searchQuery.isBlank()) {
                                "你可以切换目录，或者直接新建一篇 Markdown。"
                            } else {
                                "没有找到与“$searchQuery”匹配的内容。"
                            }
                        )
                    }

                    else -> {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(visibleFiles, key = { it.path }) { file ->
                                FileItem(
                                    file = file,
                                    onClick = { onFileClick(file) },
                                    onRenameClick = if (file.isMarkdownFile) {
                                        {
                                            renameTarget = file
                                            renameDraftName = file.name
                                            renameError = null
                                        }
                                    } else {
                                        null
                                    },
                                    onDeleteClick = if (file.isSupportedDocumentFile) {
                                        { deleteTarget = file }
                                    } else {
                                        null
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    RenameFileDialog(
        file = renameTarget,
        draftName = renameDraftName,
        error = renameError,
        onDraftNameChange = {
            renameDraftName = it
            renameError = null
        },
        onDismiss = {
            renameTarget = null
            renameDraftName = ""
            renameError = null
        },
        onConfirm = { file ->
            when (FileUtils.renameMarkdownFile(file.path, renameDraftName)) {
                is FileRenameResult.Success -> {
                    renameTarget = null
                    renameDraftName = ""
                    renameError = null
                    refreshToken += 1
                }

                FileRenameResult.BlankName -> renameError = emptyFileNameMessage
                FileRenameResult.InvalidName -> renameError = invalidFileNameMessage
                FileRenameResult.SourceMissing -> renameError = fileNotFoundMessage
                FileRenameResult.NotMarkdownFile -> renameError = renameMarkdownOnlyMessage
                FileRenameResult.TargetExists -> renameError = fileAlreadyExistsMessage
                FileRenameResult.RenameFailed -> renameError = renameFailedMessage
            }
        }
    )

    DeleteFileDialog(
        file = deleteTarget,
        onDismiss = { deleteTarget = null },
        onConfirm = { file ->
            if (FileUtils.deleteDocumentFile(file.path)) {
                refreshToken += 1
            }
            deleteTarget = null
        }
    )
}

@Composable
private fun RenameFileDialog(
    file: MarkdownFile?,
    draftName: String,
    error: String?,
    onDraftNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (MarkdownFile) -> Unit
) {
    if (file == null) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.rename_file)) },
        text = {
            Column {
                OutlinedTextField(
                    value = draftName,
                    onValueChange = onDraftNameChange,
                    label = { Text(stringResource(R.string.file_name)) },
                    singleLine = true,
                    isError = error != null,
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(file) }) {
                Text(stringResource(R.string.rename_file))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun DeleteFileDialog(
    file: MarkdownFile?,
    onDismiss: () -> Unit,
    onConfirm: (MarkdownFile) -> Unit
) {
    if (file == null) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("删除文件") },
        text = { Text("确定要删除“${file.name}”吗？此操作无法撤销。") },
        confirmButton = {
            TextButton(onClick = { onConfirm(file) }) {
                Text("删除")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun CardEmptyState(title: String, message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        androidx.compose.material3.Card(
            shape = RoundedCornerShape(24.dp),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            )
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
