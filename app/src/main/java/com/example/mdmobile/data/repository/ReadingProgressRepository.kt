package com.example.mdmobile.data.repository

import android.content.Context
import com.example.mdmobile.data.database.AppDatabase
import com.example.mdmobile.data.model.ReadingProgress
import kotlinx.coroutines.flow.Flow
import java.util.Date

class ReadingProgressRepository(context: Context) {
    private val progressDao = AppDatabase.getDatabase(context).readingProgressDao()

    suspend fun saveProgress(progress: ReadingProgress) {
        progressDao.insert(progress)
    }

    suspend fun updateProgress(progress: ReadingProgress) {
        progressDao.update(progress)
    }

    suspend fun deleteProgress(progress: ReadingProgress) {
        progressDao.delete(progress)
    }

    suspend fun deleteProgressByFilePath(filePath: String) {
        progressDao.deleteByFilePath(filePath)
    }

    fun getAllProgress(): Flow<List<ReadingProgress>> {
        return progressDao.getAllProgress()
    }

    suspend fun getProgressByFilePath(filePath: String): ReadingProgress? {
        return progressDao.getProgressByFilePath(filePath)
    }

    fun hasProgress(filePath: String): Flow<Boolean> {
        return progressDao.hasProgress(filePath)
    }

    fun getProgressCount(): Flow<Int> {
        return progressDao.getProgressCount()
    }

    suspend fun updateProgressPosition(filePath: String, position: Int, percentage: Float) {
        progressDao.updateProgress(filePath, position, percentage, Date())
    }

    suspend fun updateLastAccessed(filePath: String) {
        progressDao.updateLastAccessed(filePath, Date())
    }

    suspend fun addReadDuration(filePath: String, duration: Long) {
        progressDao.addReadDuration(filePath, duration)
    }

    suspend fun markAsCompleted(filePath: String, completed: Boolean = true) {
        progressDao.updateCompletionStatus(filePath, completed)
    }

    fun searchProgress(query: String): Flow<List<ReadingProgress>> {
        return progressDao.searchProgress(query)
    }

    fun getRecentlyRead(): Flow<List<ReadingProgress>> {
        return progressDao.getRecentlyRead()
    }

    fun getRecentFiles(): Flow<List<ReadingProgress>> {
        return progressDao.getRecentFiles()
    }

    suspend fun saveOrUpdateProgress(
        filePath: String,
        fileName: String,
        position: Int = 0,
        percentage: Float = 0f,
        totalLines: Int = 0
    ): ReadingProgress {
        val existing = getProgressByFilePath(filePath)
        return if (existing != null) {
            val updated = existing.copy(
                position = position,
                percentage = percentage,
                lastPosition = position,
                totalLines = if (totalLines > 0) totalLines else existing.totalLines,
                lastAccessed = Date()
            )
            updateProgress(updated)
            updated
        } else {
            val newProgress = ReadingProgress(
                filePath = filePath,
                fileName = fileName,
                position = position,
                percentage = percentage,
                lastPosition = position,
                totalLines = totalLines,
                lastAccessed = Date(),
                readDuration = 0,
                isCompleted = percentage >= 90f
            )
            saveProgress(newProgress)
            newProgress
        }
    }

    suspend fun updateReadingProgress(
        filePath: String,
        fileName: String,
        scrollPosition: Int,
        totalContentHeight: Int,
        visibleHeight: Int
    ) {
        if (totalContentHeight <= 0) return

        val percentage = (scrollPosition.toFloat() / totalContentHeight.toFloat() * 100f).coerceIn(0f, 100f)
        saveOrUpdateProgress(
            filePath = filePath,
            fileName = fileName,
            position = scrollPosition,
            percentage = percentage
        )
    }
}