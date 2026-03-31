package com.example.mdmobile.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mdmobile.ui.components.AdaptiveLayout
import com.example.mdmobile.ui.components.MarkdownRenderer
import com.example.mdmobile.utils.FileUtils
import com.example.mdmobile.utils.scaleOnPress
import com.example.mdmobile.viewmodels.BookmarkViewModel
import com.example.mdmobile.viewmodels.ReadingProgressViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    filePath: String? = null,
    onBack: () -> Unit = {}
) {
    var content by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showSettingsMenu by remember { mutableStateOf(false) }
    var fontSize by remember { mutableStateOf(16) }
    var isDarkTheme by remember { mutableStateOf(false) }
    val isPreviewMode = LocalInspectionMode.current
    val context = LocalContext.current
    val viewModel: BookmarkViewModel = viewModel(
        factory = BookmarkViewModel.provideFactory(context)
    )
    val progressViewModel: ReadingProgressViewModel = viewModel(
        factory = ReadingProgressViewModel.provideFactory(context)
    )
    val bookmarks by viewModel.bookmarks.collectAsState()

    // Calculate if current file is bookmarked
    val isBookmarked = remember(bookmarks, filePath) {
        filePath != null && bookmarks.any { it.filePath == filePath }
    }

    LaunchedEffect(filePath) {
        isLoading = true

        // Load content
        content = if (isPreviewMode || filePath == null) {
            // Mock markdown content for preview
            """
                # 欢迎使用 MDMobile

                ## 功能特性

                - 📁 **文件夹样式界面** - 像文件管理器一样浏览您的Markdown文档
                - 📖 **简洁阅读器** - Typora风格的干净阅读体验
                - 🔖 **书签功能** - 快速标记和访问重要文档
                - 🎨 **主题切换** - 支持亮色和暗色模式

                ## 示例文档

                ### 代码示例
                ```kotlin
                fun main() {
                    println("Hello, MDMobile!")
                }
                ```

                ### 列表示例
                1. 第一项
                2. 第二项
                3. 第三项

                ### 表格示例
                | 功能 | 状态 |
                |------|------|
                | 文件浏览 | ✅ 已完成 |
                | Markdown渲染 | 🔄 开发中 |
                | 书签管理 | 📅 计划中 |

                ### 引用示例
                > 这是引用文本
                > 多行引用示例

                ---

                **粗体文本** *斜体文本* ~~删除线~~

                [链接示例](https://example.com)

                *感谢使用MDMobile！*
            """.trimIndent()
        } else {
            withContext(Dispatchers.IO) {
                FileUtils.readMarkdownFile(filePath)
            }
        }

        // Update reading progress (record file access)
        if (filePath != null && !isPreviewMode) {
            val fileName = filePath.substringAfterLast('/')
            progressViewModel.saveOrUpdateProgress(
                filePath = filePath,
                fileName = fileName,
                position = 0,
                percentage = 0f
            )
        }

        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = filePath?.substringAfterLast('/')?.ifEmpty { "文档" } ?: "文档"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // Bookmark toggle with animation
                    if (filePath != null) {
                        val scope = rememberCoroutineScope()
                        IconButton(
                            onClick = {
                                scope.launch {
                                    val fileName = filePath.substringAfterLast('/')
                                    viewModel.toggleBookmark(filePath, fileName)
                                }
                            },
                            modifier = Modifier.scaleOnPress(scale = 0.85f)
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = if (isBookmarked) "移除书签" else "添加书签",
                                tint = if (isBookmarked) MaterialTheme.colorScheme.primary
                                      else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Font size selector
                    IconButton(
                        onClick = { showSettingsMenu = true },
                        modifier = Modifier.scaleOnPress(scale = 0.85f)
                    ) {
                        Icon(
                            Icons.Default.FontDownload,
                            contentDescription = "字体大小"
                        )
                    }
                    DropdownMenu(
                        expanded = showSettingsMenu,
                        onDismissRequest = { showSettingsMenu = false }
                    ) {
                        Text(
                            text = "字体大小",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelMedium
                        )
                        listOf(12, 14, 16, 18, 20).forEach { size ->
                            DropdownMenuItem(
                                text = { Text("${size}px") },
                                onClick = {
                                    fontSize = size
                                    showSettingsMenu = false
                                }
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(if (isDarkTheme) "切换到亮色主题" else "切换到暗色主题") },
                            onClick = {
                                isDarkTheme = !isDarkTheme
                                showSettingsMenu = false
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
                content == null -> {
                    Text(
                        text = "无法加载文档",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        AdaptiveLayout {
                            MarkdownRenderer(
                                markdownContent = content ?: "",
                                isDarkTheme = isDarkTheme,
                                fontSize = fontSize,
                                modifier = Modifier.fillMaxSize()
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
fun ReaderScreenPreview() {
    ReaderScreen(
        filePath = "/sdcard/Documents/example.md",
        onBack = {}
    )
}