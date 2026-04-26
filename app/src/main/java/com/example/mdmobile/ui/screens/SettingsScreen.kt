package com.example.mdmobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mdmobile.R
import com.example.mdmobile.data.model.ThemeMode
import com.example.mdmobile.data.model.UserPreferences
import com.example.mdmobile.utils.FileUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userPreferences: UserPreferences,
    onUpdateThemeMode: (ThemeMode) -> Unit,
    onUpdateFontSize: (Int) -> Unit,
    onUpdateDefaultFolder: (String?) -> Unit,
    onPrivacyPolicyClick: () -> Unit
) {
    var showCustomPathDialog by remember { mutableStateOf(false) }
    var customPath by remember(userPreferences.defaultFolder) {
        mutableStateOf(userPreferences.defaultFolder.orEmpty())
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings), fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SettingsCard(
                    icon = Icons.Default.Palette,
                    title = "配色模式",
                    subtitle = "在浅色、深色和跟随系统之间切换"
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeMode.entries.forEach { mode ->
                            FilterChip(
                                selected = userPreferences.themeMode == mode,
                                onClick = { onUpdateThemeMode(mode) },
                                label = {
                                    Text(
                                        when (mode) {
                                            ThemeMode.LIGHT -> stringResource(R.string.light)
                                            ThemeMode.DARK -> stringResource(R.string.dark)
                                            ThemeMode.AUTO -> stringResource(R.string.auto)
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }

            item {
                SettingsCard(
                    icon = Icons.Default.TextFields,
                    title = "阅读字号",
                    subtitle = "更大更轻松，或更小更紧凑"
                ) {
                    Text(
                        text = "${userPreferences.fontSize}px",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = userPreferences.fontSize.toFloat(),
                        onValueChange = { onUpdateFontSize(it.toInt()) },
                        valueRange = 12f..24f,
                        steps = 5,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
            }

            item {
                SettingsCard(
                    icon = Icons.Default.Info,
                    title = "默认目录",
                    subtitle = userPreferences.defaultFolder ?: stringResource(R.string.default_folder_not_set)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FileUtils.getDefaultDirectories().forEach { directory ->
                            FilterChip(
                                selected = userPreferences.defaultFolder == directory.path,
                                onClick = { onUpdateDefaultFolder(directory.path) },
                                label = { Text(directory.name) }
                            )
                        }
                    }
                    FilterChip(
                        selected = false,
                        onClick = { showCustomPathDialog = true },
                        label = { Text(stringResource(R.string.custom_path)) },
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
            }

            item {
                SettingsCard(
                    icon = Icons.Default.PrivacyTip,
                    title = "隐私与说明",
                    subtitle = "查看应用政策和基本信息"
                ) {
                    TextButton(onClick = onPrivacyPolicyClick) {
                        Text(stringResource(R.string.privacy_policy))
                    }
                }
            }

            item {
                SettingsCard(
                    icon = Icons.Default.Info,
                    title = "关于应用",
                    subtitle = stringResource(R.string.about_content)
                ) {}
            }
        }
    }

    if (showCustomPathDialog) {
        AlertDialog(
            onDismissRequest = { showCustomPathDialog = false },
            title = { Text(stringResource(R.string.custom_path)) },
            text = {
                OutlinedTextField(
                    value = customPath,
                    onValueChange = { customPath = it },
                    label = { Text(stringResource(R.string.default_folder)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (customPath.isBlank() || FileUtils.ensureDirectory(customPath)) {
                            onUpdateDefaultFolder(customPath.ifBlank { null })
                            showCustomPathDialog = false
                        }
                    }
                ) {
                    Text(stringResource(R.string.apply))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomPathDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun SettingsCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp, bottom = 8.dp)
            )
            content()
        }
    }
}
