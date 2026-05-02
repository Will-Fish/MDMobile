package com.example.mdmobile.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import com.example.mdmobile.data.model.MarkdownFile
import java.io.File
import java.text.DecimalFormat

sealed class FileRenameResult {
    data class Success(val file: File) : FileRenameResult()
    data object BlankName : FileRenameResult()
    data object InvalidName : FileRenameResult()
    data object SourceMissing : FileRenameResult()
    data object NotMarkdownFile : FileRenameResult()
    data object TargetExists : FileRenameResult()
    data object RenameFailed : FileRenameResult()
}

object FileUtils {
    private const val APP_DOCUMENT_FOLDER = "md_files"

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

    fun filterDocumentFiles(files: List<MarkdownFile>): List<MarkdownFile> {
        return files.filter { it.isSupportedDocumentFile }
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

    fun importMarkdownFromUri(context: Context, uri: Uri): String? {
        return try {
            val displayName = getDisplayName(context, uri)
                ?.takeIf { it.isNotBlank() }
                ?: uri.lastPathSegment?.substringAfterLast('/')
                ?: "shared-markdown.md"
            val safeName = sanitizeFileName(displayName).let { name ->
                if (name.endsWith(".md", ignoreCase = true) ||
                    name.endsWith(".markdown", ignoreCase = true)
                ) {
                    name
                } else {
                    "$name.md"
                }
            }
            val importsDir = File(context.filesDir, "shared_markdown")
            if (!importsDir.exists()) {
                importsDir.mkdirs()
            }
            val target = File(importsDir, "${System.currentTimeMillis()}-$safeName")
            context.contentResolver.openInputStream(uri)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            } ?: return null
            target.absolutePath
        } catch (e: Exception) {
            Log.e("FileUtils", "Error importing markdown from uri: $uri", e)
            null
        }
    }

    fun createMarkdownFile(directoryPath: String, fileName: String, content: String = ""): String? {
        val normalizedName = if (fileName.endsWith(".md", ignoreCase = true)) fileName else "$fileName.md"
        val target = File(directoryPath, normalizedName)
        return if (writeMarkdownFile(target.absolutePath, content)) target.absolutePath else null
    }

    fun renameMarkdownFile(filePath: String, newName: String): FileRenameResult {
        val source = File(filePath)
        if (!source.exists() || !source.isFile) return FileRenameResult.SourceMissing
        if (!source.extension.equals("md", ignoreCase = true) &&
            !source.extension.equals("markdown", ignoreCase = true)
        ) {
            return FileRenameResult.NotMarkdownFile
        }

        val trimmedName = newName.trim()
        if (trimmedName.isBlank()) return FileRenameResult.BlankName
        if (trimmedName.contains(Regex("[\\\\/:*?\"<>|]"))) return FileRenameResult.InvalidName

        val normalizedName = if (
            trimmedName.endsWith(".md", ignoreCase = true) ||
            trimmedName.endsWith(".markdown", ignoreCase = true)
        ) {
            trimmedName
        } else {
            "$trimmedName.md"
        }
        val target = File(source.parentFile ?: return FileRenameResult.RenameFailed, normalizedName)
        if (source.absolutePath == target.absolutePath) return FileRenameResult.Success(source)
        if (target.exists()) return FileRenameResult.TargetExists

        return if (source.renameTo(target)) {
            FileRenameResult.Success(target)
        } else {
            FileRenameResult.RenameFailed
        }
    }

    fun deleteDocumentFile(filePath: String): Boolean {
        val source = File(filePath)
        if (!source.exists() || !source.isFile) return false
        val isSupportedDocument = source.extension.equals("md", ignoreCase = true) ||
            source.extension.equals("html", ignoreCase = true) ||
            source.extension.equals("pdf", ignoreCase = true)
        return isSupportedDocument && source.delete()
    }

    fun getRecommendedRootDirectory(): File {
        val documents = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        return if (documents.exists()) documents else Environment.getExternalStorageDirectory()
    }

    fun getAppDocumentDirectory(context: Context): File {
        return getAppDocumentDirectory(context.filesDir)
    }

    fun getAppDocumentDirectory(baseDir: File): File {
        val directory = File(baseDir, APP_DOCUMENT_FOLDER)
        ensureDirectory(directory.absolutePath)
        return directory
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

    private fun getDisplayName(context: Context, uri: Uri): String? {
        if (uri.scheme == "file") {
            return uri.path?.let { File(it).name }
        }

        var cursor: Cursor? = null
        return try {
            cursor = context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )
            val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME) ?: -1
            if (cursor?.moveToFirst() == true && nameIndex >= 0) {
                cursor.getString(nameIndex)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w("FileUtils", "Unable to read display name for uri: $uri", e)
            null
        } finally {
            cursor?.close()
        }
    }

    private fun sanitizeFileName(name: String): String {
        return name
            .replace(Regex("[\\\\/:*?\"<>|]"), "_")
            .trim()
            .ifBlank { "shared-markdown.md" }
    }
}
