package com.example.mdmobile.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mdmobile.data.model.ReadingProgress
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ReadingProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(progress: ReadingProgress)

    @Update
    suspend fun update(progress: ReadingProgress)

    @Delete
    suspend fun delete(progress: ReadingProgress)

    @Query("DELETE FROM reading_progress WHERE filePath = :filePath")
    suspend fun deleteByFilePath(filePath: String)

    @Query("SELECT * FROM reading_progress ORDER BY lastAccessed DESC")
    fun getAllProgress(): Flow<List<ReadingProgress>>

    @Query("SELECT * FROM reading_progress WHERE filePath = :filePath LIMIT 1")
    suspend fun getProgressByFilePath(filePath: String): ReadingProgress?

    @Query("SELECT EXISTS(SELECT 1 FROM reading_progress WHERE filePath = :filePath LIMIT 1)")
    fun hasProgress(filePath: String): Flow<Boolean>

    @Query("SELECT COUNT(*) FROM reading_progress")
    fun getProgressCount(): Flow<Int>

    @Query("UPDATE reading_progress SET position = :position, percentage = :percentage, lastAccessed = :date WHERE filePath = :filePath")
    suspend fun updateProgress(filePath: String, position: Int, percentage: Float, date: Date)

    @Query("UPDATE reading_progress SET lastAccessed = :date WHERE filePath = :filePath")
    suspend fun updateLastAccessed(filePath: String, date: Date)

    @Query("UPDATE reading_progress SET readDuration = readDuration + :duration WHERE filePath = :filePath")
    suspend fun addReadDuration(filePath: String, duration: Long)

    @Query("UPDATE reading_progress SET isCompleted = :completed WHERE filePath = :filePath")
    suspend fun updateCompletionStatus(filePath: String, completed: Boolean)

    @Query("SELECT * FROM reading_progress WHERE fileName LIKE '%' || :query || '%' ORDER BY lastAccessed DESC")
    fun searchProgress(query: String): Flow<List<ReadingProgress>>

    @Query("SELECT * FROM reading_progress WHERE percentage >= 90 ORDER BY lastAccessed DESC LIMIT 10")
    fun getRecentlyRead(): Flow<List<ReadingProgress>>

    @Query("SELECT * FROM reading_progress ORDER BY lastAccessed DESC LIMIT 10")
    fun getRecentFiles(): Flow<List<ReadingProgress>>
}