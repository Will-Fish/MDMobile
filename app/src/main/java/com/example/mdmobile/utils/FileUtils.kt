package com.example.mdmobile.utils

import android.os.Environment
import android.util.Log
import com.example.mdmobile.data.model.MarkdownFile
import java.io.File
import java.text.DecimalFormat

object FileUtils {
    fun listFilesInDirectory(path: String?): List<MarkdownFile> {
        return try {
            val directory = when {
                path != null && path.isNotBlank() -> File(path)
                else -> getRecommendedRootDirectory()
            }

            if (directory.exists() && directory.isDirectory) {
                directory.listFiles()
                    ?.mapNotNull { file ->
                        try {
                            MarkdownFile.fromFile(file)
                        } catch (e: Exception) {
                            Log.w("FileUtils", "Skipping file ${file.path}: ${e.message}")
                            null
                        }
                    }
                    ?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
                    ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("FileUtils", "Error listing files in directory: $path", e)
            emptyList()
        }
    }

    fun filterMarkdownFiles(files: List<MarkdownFile>): List<MarkdownFile> {
        return files.filter { it.isMarkdownFile || it.isDirectory }
    }

    fun sortFilesByName(files: List<MarkdownFile>, ascending: Boolean = true): List<MarkdownFile> {
        val sorted = files.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
        return if (ascending) sorted else sorted.reversed()
    }

    fun sortFilesByDate(files: List<MarkdownFile>, ascending: Boolean = true): List<MarkdownFile> {
        val sorted = files.sortedWith(compareBy({ !it.isDirectory }, { it.lastModified.time }))
        return if (ascending) sorted else sorted.reversed()
    }

    fun searchFiles(files: List<MarkdownFile>, query: String): List<MarkdownFile> {
        if (query.isBlank()) return files

        return files.filter { file ->
            file.name.contains(query, ignoreCase = true) ||
                file.displayName.contains(query, ignoreCase = true)
        }
    }

    fun getParentDirectory(path: String): String? = File(path).parent

    fun getDefaultDirectories(): List<MarkdownFile> {
        val defaultDirs = listOf(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            File(Environment.getExternalStorageDirectory(), "Notes")
        )

        return defaultDirs.mapNotNull { dir ->
            if (dir.exists() && dir.isDirectory) MarkdownFile.fromFile(dir) else null
        }
    }

    fun readMarkdownFile(filePath: String): String? {
        return try {
            File(filePath).readText(Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e("FileUtils", "Error reading markdown file: $filePath", e)
            null
        }
    }

    fun writeMarkdownFile(filePath: String, content: String): Boolean {
        return try {
            val file = File(filePath)
            file.parentFile?.mkdirs()
            file.writeText(content, Charsets.UTF_8)
            true
        } catch (e: Exception) {
            Log.e("FileUtils", "Error writing markdown file: $filePath", e)
            false
        }
    }

    fun createMarkdownFile(directoryPath: String, fileName: String, content: String = ""): String? {
        val normalizedName = if (fileName.endsWith(".md", ignoreCase = true)) fileName else "$fileName.md"
        val target = File(directoryPath, normalizedName)
        return if (writeMarkdownFile(target.absolutePath, content)) target.absolutePath else null
    }

    fun getRecommendedRootDirectory(): File {
        val documents = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        return if (documents.exists()) documents else Environment.getExternalStorageDirectory()
    }

    fun ensureDirectory(path: String): Boolean {
        val file = File(path)
        return file.exists() || file.mkdirs()
    }

    fun formatFileSize(size: Long): String {
        if (size <= 0) return "0 B"
        val kb = 1024.0
        val mb = kb * 1024.0
        val formatter = DecimalFormat("#.#")
        return when {
            size >= mb -> "${formatter.format(size / mb)} MB"
            size >= kb -> "${formatter.format(size / kb)} KB"
            else -> "$size B"
        }
    }
}
