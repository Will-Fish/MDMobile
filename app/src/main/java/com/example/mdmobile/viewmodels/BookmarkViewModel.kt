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

    private var hasStartedObserving = false

    init {
        loadBookmarks()
    }

    fun loadBookmarks() {
        if (hasStartedObserving) return
        hasStartedObserving = true
        viewModelScope.launch {
            _isLoading.value = true
            bookmarkRepository.getAllBookmarks().collect { bookmarksList ->
                _bookmarks.value = bookmarksList
                _isLoading.value = false
            }
        }
    }

    suspend fun addBookmark(bookmark: Bookmark): Long {
        return bookmarkRepository.addBookmark(bookmark)
    }

    suspend fun updateBookmark(bookmark: Bookmark) {
        bookmarkRepository.updateBookmark(bookmark)
    }

    suspend fun deleteBookmark(bookmark: Bookmark) {
        bookmarkRepository.deleteBookmark(bookmark)
    }

    suspend fun deleteBookmarkById(id: Long) {
        bookmarkRepository.deleteBookmarkById(id)
    }

    suspend fun toggleBookmark(filePath: String, fileName: String): Boolean {
        return bookmarkRepository.toggleBookmark(filePath, fileName)
    }

    suspend fun updateLastAccessed(bookmark: Bookmark) {
        bookmarkRepository.updateLastAccessed(bookmark.id)
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
