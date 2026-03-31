package com.example.mdmobile.utils

import android.os.Environment
import android.util.Log
import com.example.mdmobile.data.model.MarkdownFile
import java.io.File
import java.nio.charset.Charset

object FileUtils {
    fun listFilesInDirectory(path: String?): List<MarkdownFile> {
        return try {
            val directory = when {
                path != null && path.isNotBlank() -> File(path)
                else -> Environment.getExternalStorageDirectory() // Default to root directory
            }

            if (directory.exists() && directory.isDirectory) {
                directory.listFiles()?.mapNotNull { file ->
                    try {
                        MarkdownFile.fromFile(file)
                    } catch (e: Exception) {
                        Log.w("FileUtils", "Skipping file ${file.path}: ${e.message}")
                        null // Skip files that can't be read
                    }
                }?.sortedWith(compareBy(
                    { !it.isDirectory }, // Directories first
                    { it.name.lowercase() } // Then alphabetical
                )) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("FileUtils", "Error listing files in directory: $path", e)
            emptyList() // Return empty list on error
        }
    }

    fun filterMarkdownFiles(files: List<MarkdownFile>): List<MarkdownFile> {
        return files.filter { it.isMarkdownFile || it.isDirectory }
    }

    fun sortFilesByName(files: List<MarkdownFile>, ascending: Boolean = true): List<MarkdownFile> {
        return if (ascending) {
            files.sortedWith(compareBy(
                { !it.isDirectory },
                { it.name.lowercase() }
            ))
        } else {
            files.sortedWith(compareBy(
                { !it.isDirectory },
                { -it.name.lowercase().hashCode() }
            ))
        }
    }

    fun sortFilesByDate(files: List<MarkdownFile>, ascending: Boolean = true): List<MarkdownFile> {
        return if (ascending) {
            files.sortedWith(compareBy(
                { !it.isDirectory },
                { it.lastModified }
            ))
        } else {
            files.sortedWith(compareBy(
                { !it.isDirectory },
                { -it.lastModified.time }
            ))
        }
    }

    fun searchFiles(files: List<MarkdownFile>, query: String): List<MarkdownFile> {
        if (query.isBlank()) return files

        return files.filter { file ->
            file.name.contains(query, ignoreCase = true) ||
            file.displayName.contains(query, ignoreCase = true)
        }
    }

    fun getParentDirectory(path: String): String? {
        val file = File(path)
        return file.parent
    }

    fun getDefaultDirectories(): List<MarkdownFile> {
        val storageDir = Environment.getExternalStorageDirectory()
        val defaultDirs = listOf(
            File(storageDir, "Documents"),
            File(storageDir, "Download"),
            File(storageDir, "DCIM")
        )

        return defaultDirs.mapNotNull { dir ->
            if (dir.exists() && dir.isDirectory) {
                MarkdownFile.fromFile(dir)
            } else null
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
}