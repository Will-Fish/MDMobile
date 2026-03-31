package com.example.mdmobile.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mdmobile.data.model.Bookmark
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bookmark: Bookmark): Long

    @Update
    suspend fun update(bookmark: Bookmark)

    @Delete
    suspend fun delete(bookmark: Bookmark)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM bookmarks ORDER BY lastAccessed DESC")
    fun getAllBookmarks(): Flow<List<Bookmark>>

    @Query("SELECT * FROM bookmarks WHERE filePath = :filePath LIMIT 1")
    suspend fun getBookmarkByFilePath(filePath: String): Bookmark?

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE filePath = :filePath LIMIT 1)")
    fun isBookmarked(filePath: String): Flow<Boolean>

    @Query("SELECT COUNT(*) FROM bookmarks")
    fun getBookmarkCount(): Flow<Int>

    @Query("UPDATE bookmarks SET lastAccessed = :date WHERE id = :id")
    suspend fun updateLastAccessed(id: Long, date: java.util.Date)

    @Query("SELECT * FROM bookmarks WHERE fileName LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%' ORDER BY lastAccessed DESC")
    fun searchBookmarks(query: String): Flow<List<Bookmark>>
}