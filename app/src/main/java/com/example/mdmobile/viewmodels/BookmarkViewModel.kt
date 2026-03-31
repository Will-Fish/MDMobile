package com.example.mdmobile.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mdmobile.data.model.Bookmark
import com.example.mdmobile.data.repository.BookmarkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BookmarkViewModel(
    application: Application,
    private val bookmarkRepository: BookmarkRepository
) : AndroidViewModel(application) {

    private val _bookmarks = MutableStateFlow<List<Bookmark>>(emptyList())
    val bookmarks: StateFlow<List<Bookmark>> = _bookmarks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadBookmarks()
    }

    fun loadBookmarks() {
        viewModelScope.launch {
            _isLoading.value = true
            bookmarkRepository.getAllBookmarks().collect { bookmarksList ->
                _bookmarks.value = bookmarksList
                _isLoading.value = false
            }
        }
    }

    suspend fun addBookmark(bookmark: Bookmark): Long {
        return bookmarkRepository.addBookmark(bookmark).also {
            loadBookmarks() // Refresh the list
        }
    }

    suspend fun updateBookmark(bookmark: Bookmark) {
        bookmarkRepository.updateBookmark(bookmark)
        loadBookmarks()
    }

    suspend fun deleteBookmark(bookmark: Bookmark) {
        bookmarkRepository.deleteBookmark(bookmark)
        loadBookmarks()
    }

    suspend fun deleteBookmarkById(id: Long) {
        bookmarkRepository.deleteBookmarkById(id)
        loadBookmarks()
    }

    suspend fun toggleBookmark(filePath: String, fileName: String): Boolean {
        return bookmarkRepository.toggleBookmark(filePath, fileName).also {
            loadBookmarks()
        }
    }

    suspend fun updateLastAccessed(bookmark: Bookmark) {
        bookmarkRepository.updateLastAccessed(bookmark.id)
        loadBookmarks()
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = context.applicationContext as Application
                val repository = BookmarkRepository(application)
                BookmarkViewModel(application, repository)
            }
        }
    }
}