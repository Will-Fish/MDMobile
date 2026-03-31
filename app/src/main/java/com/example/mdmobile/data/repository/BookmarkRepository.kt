package com.example.mdmobile.data.repository

import android.content.Context
import com.example.mdmobile.data.database.AppDatabase
import com.example.mdmobile.data.model.Bookmark
import kotlinx.coroutines.flow.Flow
import java.util.Date

class BookmarkRepository(context: Context) {
    private val bookmarkDao = AppDatabase.getDatabase(context).bookmarkDao()

    suspend fun addBookmark(bookmark: Bookmark): Long {
        return bookmarkDao.insert(bookmark)
    }

    suspend fun updateBookmark(bookmark: Bookmark) {
        bookmarkDao.update(bookmark)
    }

    suspend fun deleteBookmark(bookmark: Bookmark) {
        bookmarkDao.delete(bookmark)
    }

    suspend fun deleteBookmarkById(id: Long) {
        bookmarkDao.deleteById(id)
    }

    fun getAllBookmarks(): Flow<List<Bookmark>> {
        return bookmarkDao.getAllBookmarks()
    }

    suspend fun getBookmarkByFilePath(filePath: String): Bookmark? {
        return bookmarkDao.getBookmarkByFilePath(filePath)
    }

    fun isBookmarked(filePath: String): Flow<Boolean> {
        return bookmarkDao.isBookmarked(filePath)
    }

    fun getBookmarkCount(): Flow<Int> {
        return bookmarkDao.getBookmarkCount()
    }

    suspend fun updateLastAccessed(id: Long) {
        bookmarkDao.updateLastAccessed(id, Date())
    }

    fun searchBookmarks(query: String): Flow<List<Bookmark>> {
        return bookmarkDao.searchBookmarks(query)
    }

    suspend fun toggleBookmark(filePath: String, fileName: String): Boolean {
        val existing = getBookmarkByFilePath(filePath)
        return if (existing != null) {
            deleteBookmark(existing)
            false // Bookmark removed
        } else {
            addBookmark(Bookmark(filePath = filePath, fileName = fileName))
            true // Bookmark added
        }
    }
}