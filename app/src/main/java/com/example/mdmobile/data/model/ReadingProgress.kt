package com.example.mdmobile.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "reading_progress")
data class ReadingProgress(
    @PrimaryKey
    val filePath: String,
    val fileName: String,
    val position: Int = 0,
    val percentage: Float = 0f,
    val lastPosition: Int = 0,
    val totalLines: Int = 0,
    val lastAccessed: Date = Date(),
    val readDuration: Long = 0,
    val isCompleted: Boolean = false
) {
    companion object {
        fun createInitial(filePath: String, fileName: String): ReadingProgress {
            return ReadingProgress(
                filePath = filePath,
                fileName = fileName,
                position = 0,
                percentage = 0f,
                lastPosition = 0,
                totalLines = 0,
                lastAccessed = Date(),
                readDuration = 0,
                isCompleted = false
            )
        }
    }

    val progressPercentage: Int
        get() = percentage.toInt()

    val displayProgress: String
        get() = if (percentage > 0) "${progressPercentage}%" else "未开始"
}
