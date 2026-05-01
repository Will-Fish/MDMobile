package com.example.mdmobile.ui.screens

import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mdmobile.R
import com.example.mdmobile.data.model.UserPreferences
import com.example.mdmobile.ui.components.MarkdownRenderer
import com.example.mdmobile.ui.components.TyporaEditorPane
import com.example.mdmobile.utils.FileUtils
import com.example.mdmobile.viewmodels.BookmarkViewModel
import com.example.mdmobile.viewmodels.ReadingProgressViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

private enum class ReaderWorkspaceMode {
    WRITE,
    PREVIEW,
    SPLIT
}

private data class MarkdownTool(
    val label: String,
    val icon: ImageVector,
    val before: String,
    val after: String = "",
    val placeholder: String = ""
)

private data class HeadingItem(
    val level: Int,
    val title: String,
    val anchorId: String,
    val lineIndex: Int
)

private val TextFieldValueSaver = Saver<TextFieldValue, List<Any>>(
    save = { listOf(it.text, it.selection.start, it.selection.end) },
    restore = {
        TextFieldValue(
            text = it[0] as String,
            selection = TextRange(it[1] as Int, it[2] as Int)
        )
    }
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    filePath: String?,
    userPreferences: UserPreferences,
    startInEditMode: Boolean,
    isNewFile: Boolean,
    onBack: () -> Unit,
    onFontSizeChange: (Int) -> Unit
) {
    val context = LocalContext.current
    val bookmarkViewModel: BookmarkViewModel = viewModel(
        factory = BookmarkViewModel.provideFactory(context)
    )
    val progressViewModel: ReadingProgressViewModel = viewModel(
        factory = ReadingProgressViewModel.provideFactory(context)
    )
    val bookmarks by bookmarkViewModel.bookmarks.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var editorValue by rememberSaveable(filePath, stateSaver = TextFieldValueSaver) {
        mutableStateOf(TextFieldValue(""))
    }
    var isLoading by remember { mutableStateOf(true) }
    var showFontMenu by remember { mutableStateOf(false) }
    var showShareMenu by remember { mutableStateOf(false) }
    var hasLoadError by remember { mutableStateOf(false) }
    var originalContent by remember(filePath) { mutableStateOf("") }
    var workspaceMode by rememberSaveable(filePath) {
        mutableStateOf(if (startInEditMode) ReaderWorkspaceMode.WRITE else ReaderWorkspaceMode.PREVIEW)
    }
    var selectedAnchor by remember(filePath) { mutableStateOf<String?>(null) }
    var lastSavedAt by rememberSaveable(filePath) { mutableLongStateOf(0L) }
    var collapsedAnchors by rememberSaveable(filePath) { mutableStateOf(setOf<String>()) }
    var outlineSearchQuery by rememberSaveable(filePath) { mutableStateOf("") }

    val undoStack = remember(filePath) { mutableStateListOf<TextFieldValue>() }
    val redoStack = remember(filePath) { mutableStateListOf<TextFieldValue>() }

    val currentPath = filePath.orEmpty()
    val fileName = remember(currentPath) {
        currentPath.substringAfterLast('/').ifBlank { "Untitled.md" }
    }
    val content = editorValue.text
    val headings = remember(content) { extractHeadings(content) }
    val isBookmarked = remember(bookmarks, currentPath) {
        currentPath.isNotBlank() && bookmarks.any { it.filePath == currentPath }
    }
    val isDirty = content != originalContent
    val wordCount = remember(content) { countWords(content) }
    val lastSavedLabel = remember(lastSavedAt) {
        if (lastSavedAt <= 0L) null
        else SimpleDateFormat("HH:mm", Locale.getDefault()).format(lastSavedAt)
    }

    val inlineTools = remember {
        listOf(
            MarkdownTool("粗体", Icons.Default.FormatBold, "**", "**", "重点"),
            MarkdownTool("斜体", Icons.Default.FormatItalic, "*", "*", "内容"),
            MarkdownTool("引用", Icons.Default.FormatQuote, "> ", placeholder = "引用"),
            MarkdownTool("代码", Icons.Default.Code, "```kotlin\n", "\n```", "println(\"Hello\")"),
            MarkdownTool("链接", Icons.Default.Link, "[", "](https://example.com)", "链接文字"),
            MarkdownTool("分隔", Icons.Default.HorizontalRule, "\n---\n")
        )
    }

    fun updateEditorValue(newValue: TextFieldValue, trackHistory: Boolean = true) {
        if (trackHistory && newValue != editorValue) {
            undoStack += editorValue
            if (undoStack.size > 100) {
                undoStack.removeAt(0)
            }
            redoStack.clear()
        }
        editorValue = newValue
    }

    fun saveDocument(
        textSnapshot: String = editorValue.text,
        selectionEnd: Int = editorValue.selection.end,
        showToast: Boolean = false,
        onComplete: (() -> Unit)? = null
    ) {
        if (currentPath.isBlank()) {
            onComplete?.invoke()
            return
        }
        val lineCount = textSnapshot.lineSequence().count()
        val progress = editorProgress(textSnapshot, selectionEnd)
        scope.launch {
            val success = withContext(Dispatchers.IO) {
                FileUtils.writeMarkdownFile(currentPath, textSnapshot)
            }
            if (success) {
                if (editorValue.text == textSnapshot) {
                    originalContent = textSnapshot
                }
                lastSavedAt = System.currentTimeMillis()
                progressViewModel.saveOrUpdateProgress(
                    filePath = currentPath,
                    fileName = fileName,
                    position = selectionEnd,
                    percentage = progress,
                    totalLines = lineCount
                )
                if (showToast) {
                    Toast.makeText(context, context.getString(R.string.save_success), Toast.LENGTH_SHORT).show()
                }
            } else if (showToast) {
                Toast.makeText(context, context.getString(R.string.save_failed), Toast.LENGTH_SHORT).show()
            }
            onComplete?.invoke()
        }
    }

    fun cycleWorkspaceMode(canSplit: Boolean) {
        workspaceMode = when (workspaceMode) {
            ReaderWorkspaceMode.WRITE -> ReaderWorkspaceMode.PREVIEW
            ReaderWorkspaceMode.PREVIEW -> if (canSplit) ReaderWorkspaceMode.SPLIT else ReaderWorkspaceMode.WRITE
            ReaderWorkspaceMode.SPLIT -> ReaderWorkspaceMode.WRITE
        }
    }

    fun exportHtml() {
        if (currentPath.isBlank()) return
        val markdownSnapshot = editorValue.text
        val sourceFile = File(currentPath)
        val targetFile = File(sourceFile.parentFile ?: return, "${sourceFile.nameWithoutExtension}.html")
        scope.launch {
            val success = withContext(Dispatchers.IO) {
                targetFile.writeText(renderMarkdownHtml(markdownSnapshot), Charsets.UTF_8)
                true
            }
            if (success) {
                Toast.makeText(context, "已导出到 ${targetFile.name}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "导出失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun shareMarkdown() {
        if (currentPath.isBlank()) return
        val file = File(currentPath)
        if (!file.exists()) return
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/markdown"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, file.name)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "分享 Markdown"))
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        if (currentPath.isBlank()) {
            Toast.makeText(context, "请先保存文件后再插入图片", Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            val imageUrl = withContext(Dispatchers.IO) {
                copyImageToDocumentFolder(context, currentPath, uri)
            }
            if (imageUrl == null) {
                Toast.makeText(context, "图片插入失败", Toast.LENGTH_SHORT).show()
            } else {
                updateEditorValue(
                    insertMarkdownSnippet(
                        editorValue,
                        before = "![图片](",
                        after = ")",
                        placeholder = imageUrl
                    )
                )
            }
        }
    }

    LaunchedEffect(filePath, isNewFile) {
        isLoading = true
        hasLoadError = false
        undoStack.clear()
        redoStack.clear()

        val loadedContent = if (filePath.isNullOrBlank()) {
            null
        } else {
            withContext(Dispatchers.IO) {
                val file = File(filePath)
                when {
                    file.exists() -> FileUtils.readMarkdownFile(filePath)
                    isNewFile -> "# 新文档\n\n从这里开始写作。\n"
                    else -> null
                }
            }
        }

        if (loadedContent == null && !isNewFile) {
            hasLoadError = true
        } else {
            val initial = loadedContent.orEmpty()
            editorValue = TextFieldValue(initial, selection = TextRange(initial.length))
            originalContent = initial
            if (!filePath.isNullOrBlank()) {
                progressViewModel.saveOrUpdateProgress(
                    filePath = filePath,
                    fileName = fileName,
                    position = 0,
                    percentage = 0f,
                    totalLines = initial.lineSequence().count()
                )
            }
        }

        isLoading = false
    }

    LaunchedEffect(headings) {
        if (selectedAnchor == null && headings.isNotEmpty()) {
            selectedAnchor = headings.first().anchorId
        }
    }

    LaunchedEffect(content, currentPath, isLoading) {
        if (isLoading || currentPath.isBlank() || content == originalContent) return@LaunchedEffect
        val snapshot = content
        val cursor = editorValue.selection.end
        delay(900)
        if (editorValue.text == snapshot && snapshot != originalContent) {
            saveDocument(textSnapshot = snapshot, selectionEnd = cursor)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        val canSplit = maxWidth >= 980.dp
        val showOutlineSidebar = maxWidth >= 1180.dp && headings.isNotEmpty()
        val effectiveMode = if (workspaceMode == ReaderWorkspaceMode.SPLIT && !canSplit) {
            ReaderWorkspaceMode.WRITE
        } else {
            workspaceMode
        }
        val visibleHeadings = remember(headings, collapsedAnchors, outlineSearchQuery) {
            buildVisibleHeadings(headings, collapsedAnchors, outlineSearchQuery)
        }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = fileName,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = when {
                                    isDirty -> "未保存修改"
                                    lastSavedLabel != null -> "已保存 $lastSavedLabel"
                                    else -> "文档工作区"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                if (isDirty) {
                                    saveDocument(onComplete = onBack)
                                } else {
                                    onBack()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = context.getString(R.string.back)
                            )
                        }
                    },
                    actions = {
                        val canUseHistory = effectiveMode != ReaderWorkspaceMode.PREVIEW
                        IconButton(
                            onClick = {
                                if (undoStack.isNotEmpty()) {
                                    redoStack += editorValue
                                    editorValue = undoStack.removeAt(undoStack.lastIndex)
                                }
                            },
                            enabled = canUseHistory && undoStack.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Undo,
                                contentDescription = "撤销"
                            )
                        }
                        IconButton(
                            onClick = {
                                if (redoStack.isNotEmpty()) {
                                    undoStack += editorValue
                                    editorValue = redoStack.removeAt(redoStack.lastIndex)
                                }
                            },
                            enabled = canUseHistory && redoStack.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Redo,
                                contentDescription = "重做"
                            )
                        }
                        if (currentPath.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        bookmarkViewModel.toggleBookmark(currentPath, fileName)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = if (isBookmarked) "移除书签" else "添加书签"
                                )
                            }
                        }
                        IconButton(onClick = { cycleWorkspaceMode(canSplit) }) {
                            Icon(
                                imageVector = Icons.Default.Preview,
                                contentDescription = "切换预览"
                            )
                        }
                        IconButton(onClick = { showFontMenu = true }) {
                            Icon(Icons.Default.FontDownload, contentDescription = "字体大小")
                        }
                        DropdownMenu(
                            expanded = showFontMenu,
                            onDismissRequest = { showFontMenu = false }
                        ) {
                            listOf(14, 16, 18, 20, 22).forEach { size ->
                                DropdownMenuItem(
                                    text = { Text("${size}px") },
                                    onClick = {
                                        onFontSizeChange(size)
                                        showFontMenu = false
                                    }
                                )
                            }
                        }
                        IconButton(onClick = { saveDocument(showToast = true) }) {
                            Icon(Icons.Default.Save, contentDescription = context.getString(R.string.save))
                        }
                        Box {
                            IconButton(onClick = { showShareMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "更多操作")
                            }
                            DropdownMenu(
                                expanded = showShareMenu,
                                onDismissRequest = { showShareMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("分享 Markdown") },
                                    onClick = {
                                        showShareMenu = false
                                        if (isDirty) {
                                            saveDocument(onComplete = ::shareMarkdown)
                                        } else {
                                            shareMarkdown()
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("导出 HTML") },
                                    onClick = {
                                        showShareMenu = false
                                        if (isDirty) {
                                            saveDocument(onComplete = ::exportHtml)
                                        } else {
                                            exportHtml()
                                        }
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.96f),
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                        actionIconContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        ) { paddingValues ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (showOutlineSidebar) {
                    OutlineSidebar(
                        headings = headings,
                        visibleHeadings = visibleHeadings,
                        selectedAnchor = selectedAnchor,
                        searchQuery = outlineSearchQuery,
                        onSearchQueryChange = { outlineSearchQuery = it },
                        collapsedAnchors = collapsedAnchors,
                        onToggleCollapsed = { anchor ->
                            collapsedAnchors = if (collapsedAnchors.contains(anchor)) {
                                collapsedAnchors - anchor
                            } else {
                                collapsedAnchors + anchor
                            }
                        },
                        onHeadingClick = { heading ->
                            selectedAnchor = heading.anchorId
                            if (effectiveMode != ReaderWorkspaceMode.PREVIEW) {
                                val position = findHeadingCursorPosition(content, heading.lineIndex)
                                editorValue = editorValue.copy(selection = TextRange(position))
                            }
                        },
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.32f)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .animateContentSize(animationSpec = tween(280, easing = FastOutSlowInEasing))
                ) {
                    if (effectiveMode != ReaderWorkspaceMode.PREVIEW) {
                        EditorToolbar(
                            tools = inlineTools,
                            onHeadingClick = { level ->
                                updateEditorValue(applyHeadingTool(editorValue, level))
                            },
                            onListClick = { ordered ->
                                updateEditorValue(applyListTool(editorValue, ordered))
                            },
                            onToolClick = { tool ->
                                updateEditorValue(applyMarkdownTool(editorValue, tool))
                            },
                            onInsertImage = {
                                imagePickerLauncher.launch(arrayOf("image/*"))
                            }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(top = if (effectiveMode == ReaderWorkspaceMode.PREVIEW) 0.dp else 12.dp)
                    ) {
                        when {
                            isLoading -> {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            }

                            hasLoadError -> {
                                ReaderMessageCard(
                                    title = "无法加载文档",
                                    message = "请确认文件仍然存在，或者重新从文件列表打开。",
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }

                            content.isBlank() && effectiveMode == ReaderWorkspaceMode.PREVIEW -> {
                                ReaderMessageCard(
                                    title = "这篇文档还是空的",
                                    message = context.getString(R.string.reader_empty_hint),
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }

                            effectiveMode == ReaderWorkspaceMode.SPLIT -> {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    TyporaEditorPane(
                                        value = editorValue,
                                        onValueChange = { updateEditorValue(it) },
                                        onFocusLost = {
                                            if (isDirty) {
                                                saveDocument()
                                            }
                                        },
                                        fontSize = userPreferences.fontSize,
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                    )
                                    PreviewPane(
                                        markdown = content,
                                        fontSize = userPreferences.fontSize,
                                        scrollToAnchor = selectedAnchor,
                                        onCurrentAnchorChange = { selectedAnchor = it },
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                    )
                                }
                            }

                            effectiveMode == ReaderWorkspaceMode.PREVIEW -> {
                                PreviewPane(
                                    markdown = content,
                                    fontSize = userPreferences.fontSize,
                                    scrollToAnchor = selectedAnchor,
                                    onCurrentAnchorChange = { selectedAnchor = it },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            else -> {
                                TyporaEditorPane(
                                    value = editorValue,
                                    onValueChange = { updateEditorValue(it) },
                                    onFocusLost = {
                                        if (isDirty) {
                                            saveDocument()
                                        }
                                    },
                                    fontSize = userPreferences.fontSize,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Text(
                            text = "字数 $wordCount",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 10.dp, bottom = 6.dp)
                                .alpha(0.82f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OutlineSidebar(
    headings: List<HeadingItem>,
    visibleHeadings: List<HeadingItem>,
    selectedAnchor: String?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    collapsedAnchors: Set<String>,
    onToggleCollapsed: (String) -> Unit,
    onHeadingClick: (HeadingItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "目录大纲",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${visibleHeadings.size} / ${headings.size} 个标题",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "搜索目录",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                placeholder = { Text(text = "搜索标题", style = MaterialTheme.typography.bodySmall) }
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                modifier = Modifier.padding(top = 12.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(visibleHeadings, key = { it.anchorId }) { heading ->
                    val selected = heading.anchorId == selectedAnchor
                    val hasChildren = hasChildHeadings(headings, heading)
                    val collapsed = collapsedAnchors.contains(heading.anchorId)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (selected) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                } else {
                                    Color.Transparent
                                },
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onHeadingClick(heading) }
                            .padding(
                                start = ((heading.level - 1) * 10).dp + 10.dp,
                                top = 10.dp,
                                end = 10.dp,
                                bottom = 10.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (hasChildren) {
                            Icon(
                                imageVector = if (collapsed) {
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight
                                } else {
                                    Icons.Default.KeyboardArrowDown
                                },
                                contentDescription = if (collapsed) "展开" else "折叠",
                                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { onToggleCollapsed(heading.anchorId) }
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        CircleShape
                                    )
                            )
                        }
                        Text(
                            text = heading.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EditorToolbar(
    tools: List<MarkdownTool>,
    onHeadingClick: (Int) -> Unit,
    onListClick: (Boolean) -> Unit,
    onToolClick: (MarkdownTool) -> Unit,
    onInsertImage: () -> Unit
) {
    var showHeadingMenu by remember { mutableStateOf(false) }
    var showListMenu by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        LazyRow(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Box {
                    AssistChip(
                        onClick = { showHeadingMenu = true },
                        label = { Text("标题") },
                        leadingIcon = {
                            Icon(Icons.Default.Title, contentDescription = "标题", modifier = Modifier.size(18.dp))
                        }
                    )
                    DropdownMenu(
                        expanded = showHeadingMenu,
                        onDismissRequest = { showHeadingMenu = false }
                    ) {
                        (1..5).forEach { level ->
                            DropdownMenuItem(
                                text = { Text("H$level") },
                                onClick = {
                                    showHeadingMenu = false
                                    onHeadingClick(level)
                                }
                            )
                        }
                    }
                }
            }
            item {
                Box {
                    AssistChip(
                        onClick = { showListMenu = true },
                        label = { Text("列表") },
                        leadingIcon = {
                            Icon(
                                Icons.AutoMirrored.Filled.FormatListBulleted,
                                contentDescription = "列表",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                    DropdownMenu(
                        expanded = showListMenu,
                        onDismissRequest = { showListMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("无序列表") },
                            onClick = {
                                showListMenu = false
                                onListClick(false)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("有序列表") },
                            onClick = {
                                showListMenu = false
                                onListClick(true)
                            }
                        )
                    }
                }
            }
            items(tools) { tool ->
                AssistChip(
                    onClick = { onToolClick(tool) },
                    label = { Text(tool.label) },
                    leadingIcon = {
                        Icon(tool.icon, contentDescription = tool.label, modifier = Modifier.size(18.dp))
                    }
                )
            }
            item {
                AssistChip(
                    onClick = onInsertImage,
                    label = { Text("图片") },
                    leadingIcon = {
                        Icon(Icons.Default.Image, contentDescription = "图片", modifier = Modifier.size(18.dp))
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        labelColor = MaterialTheme.colorScheme.primary,
                        leadingIconContentColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

@Composable
private fun PreviewPane(
    markdown: String,
    fontSize: Int,
    scrollToAnchor: String?,
    onCurrentAnchorChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Crossfade(
            targetState = markdown.isBlank(),
            modifier = Modifier.fillMaxSize(),
            label = "preview_state"
        ) { isBlank ->
            if (isBlank) {
                ReaderMessageCard(
                    title = "预览区",
                    message = "开始输入内容后，这里会实时显示排版效果。",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                )
            } else {
                MarkdownRenderer(
                    markdownContent = markdown,
                    fontSize = fontSize,
                    scrollToAnchor = scrollToAnchor,
                    onCurrentAnchorChange = onCurrentAnchorChange,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun ReaderMessageCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.widthIn(max = 420.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
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

private fun applyMarkdownTool(
    value: TextFieldValue,
    tool: MarkdownTool
): TextFieldValue {
    return insertMarkdownSnippet(
        value = value,
        before = tool.before,
        after = tool.after,
        placeholder = tool.placeholder
    )
}

private fun applyHeadingTool(
    value: TextFieldValue,
    level: Int
): TextFieldValue {
    val marker = "${"#".repeat(level)} "
    return insertMarkdownSnippet(value, before = marker, placeholder = "标题")
}

private fun applyListTool(
    value: TextFieldValue,
    ordered: Boolean
): TextFieldValue {
    return insertMarkdownSnippet(
        value = value,
        before = if (ordered) "1. " else "- ",
        placeholder = "列表项"
    )
}

private fun insertMarkdownSnippet(
    value: TextFieldValue,
    before: String,
    after: String = "",
    placeholder: String = ""
): TextFieldValue {
    val selection = value.selection
    val start = selection.min
    val end = selection.max
    val selectedText = value.text.substring(start, end)
    val content = if (selectedText.isNotEmpty()) selectedText else placeholder
    val replacement = before + content + after
    val newText = buildString {
        append(value.text.substring(0, start))
        append(replacement)
        append(value.text.substring(end))
    }
    val cursor = start + replacement.length
    return TextFieldValue(
        text = newText,
        selection = TextRange(cursor)
    )
}

private fun copyImageToDocumentFolder(
    context: android.content.Context,
    markdownPath: String,
    uri: Uri
): String? {
    return runCatching {
        val markdownFile = File(markdownPath)
        val assetDir = File(markdownFile.parentFile ?: return null, "${markdownFile.nameWithoutExtension}_assets")
        if (!assetDir.exists()) {
            assetDir.mkdirs()
        }
        val mimeType = context.contentResolver.getType(uri).orEmpty()
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
            ?: uri.lastPathSegment?.substringAfterLast('.', "")
            ?: "png"
        val fileName = "image_${System.currentTimeMillis()}.$extension"
        val targetFile = File(assetDir, fileName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            targetFile.outputStream().use { output -> input.copyTo(output) }
        } ?: return null
        "file://${targetFile.absolutePath.replace("\\", "/")}"
    }.getOrNull()
}

private fun extractHeadings(markdown: String): List<HeadingItem> {
    val headingRegex = Regex("^(#{1,6})\\s+(.+)$", RegexOption.MULTILINE)
    var index = 0
    return markdown.lineSequence().mapIndexedNotNull { lineIndex, line ->
        val match = headingRegex.find(line.trim()) ?: return@mapIndexedNotNull null
        val level = match.groupValues[1].length
        val title = match.groupValues[2].trim()
        val anchor = slugifyHeading(title, index++)
        HeadingItem(level = level, title = title, anchorId = anchor, lineIndex = lineIndex)
    }.toList()
}

private fun buildVisibleHeadings(
    headings: List<HeadingItem>,
    collapsedAnchors: Set<String>,
    searchQuery: String
): List<HeadingItem> {
    if (searchQuery.isNotBlank()) {
        return headings.filter { it.title.contains(searchQuery, ignoreCase = true) }
    }

    val visible = mutableListOf<HeadingItem>()
    val collapsedLevels = ArrayDeque<Int>()

    headings.forEach { heading ->
        while (collapsedLevels.isNotEmpty() && heading.level <= collapsedLevels.last()) {
            collapsedLevels.removeLast()
        }
        if (collapsedLevels.isNotEmpty()) return@forEach
        visible += heading
        if (collapsedAnchors.contains(heading.anchorId)) {
            collapsedLevels.addLast(heading.level)
        }
    }

    return visible
}

private fun hasChildHeadings(
    headings: List<HeadingItem>,
    heading: HeadingItem
): Boolean {
    val currentIndex = headings.indexOfFirst { it.anchorId == heading.anchorId }
    if (currentIndex == -1 || currentIndex == headings.lastIndex) return false
    for (index in currentIndex + 1 until headings.size) {
        val next = headings[index]
        if (next.level <= heading.level) return false
        if (next.level > heading.level) return true
    }
    return false
}

private fun slugifyHeading(text: String, index: Int): String {
    val base = text
        .lowercase()
        .replace(Regex("[^\\p{L}\\p{N}\\s-]"), "")
        .trim()
        .replace(Regex("\\s+"), "-")
    return if (base.isBlank()) "section-$index" else "$base-$index"
}

private fun findHeadingCursorPosition(content: String, lineIndex: Int): Int {
    if (lineIndex <= 0) return 0
    val lines = content.lines()
    val safeIndex = lineIndex.coerceIn(0, lines.lastIndex)
    var position = 0
    for (i in 0 until safeIndex) {
        position += lines[i].length + 1
    }
    return position.coerceAtMost(content.length)
}

private fun editorProgress(text: String, selectionEnd: Int): Float {
    val total = text.length.coerceAtLeast(1)
    return (selectionEnd.toFloat() / total.toFloat() * 100f).coerceIn(0f, 100f)
}

private fun countWords(text: String): Int {
    return Regex("\\S+").findAll(text).count()
}

private fun renderMarkdownHtml(markdown: String): String {
    val parser = Parser.builder().build()
    val renderer = HtmlRenderer.builder().build()
    val body = renderer.render(parser.parse(markdown))
    return """
        <!DOCTYPE html>
        <html lang="zh-CN">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Markdown Export</title>
            <style>
                body {
                    margin: 0 auto;
                    max-width: 820px;
                    padding: 40px 24px 80px;
                    font-family: "Noto Serif", "Source Han Serif SC", serif;
                    color: #12233B;
                    line-height: 1.85;
                    background: #FFFFFF;
                }
                h1, h2, h3, h4, h5, h6 {
                    line-height: 1.3;
                    margin-top: 32px;
                    margin-bottom: 12px;
                }
                h1, h2 {
                    padding-bottom: 0.25em;
                    border-bottom: 1px solid #D7E0E7;
                }
                code, pre {
                    font-family: "JetBrains Mono", Consolas, monospace;
                }
                code {
                    background: #E6F0FF;
                    padding: 0.15em 0.4em;
                    border-radius: 6px;
                    color: #1859B8;
                }
                pre {
                    background: #F4F8FD;
                    padding: 16px;
                    border-radius: 16px;
                    overflow-x: auto;
                    border: 1px solid #D3E0F0;
                }
                blockquote {
                    margin: 0 0 18px 0;
                    padding: 10px 18px;
                    border-left: 4px solid #1859B8;
                    background: #D9E8FF;
                    color: #5379A7;
                }
                img {
                    max-width: 100%;
                    border-radius: 16px;
                }
                table {
                    width: 100%;
                    border-collapse: collapse;
                }
                th, td {
                    border: 1px solid #D7E0E7;
                    padding: 10px 12px;
                }
            </style>
        </head>
        <body>$body</body>
        </html>
    """.trimIndent()
}
