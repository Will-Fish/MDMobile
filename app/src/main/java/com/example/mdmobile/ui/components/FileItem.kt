package com.example.mdmobile.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mdmobile.data.model.MarkdownFile
import com.example.mdmobile.utils.scaleOnPress
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun FileItem(
    file: MarkdownFile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    index: Int = 0
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .scaleOnPress(scale = 0.98f),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                contentDescription = if (file.isDirectory) "文件夹" else "文件",
                modifier = Modifier.size(28.dp),
                tint = if (file.isDirectory) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.secondary
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = file.displayName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!file.isDirectory) {
                    Text(
                        text = "${file.size / 1024} KB • ${formatDate(file.lastModified)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "文件夹 • ${formatDate(file.lastModified)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun formatDate(date: java.util.Date): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(date)
}

@Composable
fun FileItemPreview() {
    // Preview function for development
    MaterialTheme {
        FileItem(
            file = MarkdownFile(
                name = "示例文档.md",
                path = "/sdcard/文档/示例文档.md",
                size = 2048,
                lastModified = java.util.Date(),
                isDirectory = false
            ),
            onClick = {}
        )
    }
}