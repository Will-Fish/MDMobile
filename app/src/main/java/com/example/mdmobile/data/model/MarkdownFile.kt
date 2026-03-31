package com.example.mdmobile.data.model

import java.io.File
import java.util.Date

data class MarkdownFile(
    val name: String,
    val path: String,
    val size: Long,
    val lastModified: Date,
    val isDirectory: Boolean = false,
    val parentPath: String? = null
) {
    companion object {
        fun fromFile(file: File): MarkdownFile {
            return MarkdownFile(
                name = file.name,
                path = file.absolutePath,
                size = if (file.isDirectory) 0 else file.length(),
                lastModified = Date(file.lastModified()),
                isDirectory = file.isDirectory,
                parentPath = file.parent
            )
        }
    }

    val extension: String
        get() = if (isDirectory) "" else name.substringAfterLast('.', "")

    val isMarkdownFile: Boolean
        get() = !isDirectory && (extension.equals("md", ignoreCase = true)
                || extension.equals("markdown", ignoreCase = true))

    val displayName: String
        get() = if (isDirectory) name else name.removeSuffix(".$extension")
}