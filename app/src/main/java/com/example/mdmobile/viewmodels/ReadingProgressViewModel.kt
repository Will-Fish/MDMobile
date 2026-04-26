package com.example.mdmobile.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mdmobile.data.model.ReadingProgress
import com.example.mdmobile.data.repository.ReadingProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReadingProgressViewModel(
    application: Application,
    private val progressRepository: ReadingProgressRepository
) : AndroidViewModel(application) {

    private val _progressList = MutableStateFlow<List<ReadingProgress>>(emptyList())
    val progressList: StateFlow<List<ReadingProgress>> = _progressList.asStateFlow()

    private val _recentFiles = MutableStateFlow<List<ReadingProgress>>(emptyList())
    val recentFiles: StateFlow<List<ReadingProgress>> = _recentFiles.asStateFlow()

    private val _recentlyRead = MutableStateFlow<List<ReadingProgress>>(emptyList())
    val recentlyRead: StateFlow<List<ReadingProgress>> = _recentlyRead.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var allProgressObserved = false
    private var recentFilesObserved = false
    private var recentlyReadObserved = false

    init {
        loadAllProgress()
        loadRecentFiles()
        loadRecentlyRead()
    }

    fun loadAllProgress() {
        if (allProgressObserved) return
        allProgressObserved = true
        viewModelScope.launch {
            _isLoading.value = true
            progressRepository.getAllProgress().collect { progressList ->
                _progressList.value = progressList
                _isLoading.value = false
            }
        }
    }

    fun loadRecentFiles() {
        if (recentFilesObserved) return
        recentFilesObserved = true
        viewModelScope.launch {
            progressRepository.getRecentFiles().collect { recentFiles ->
                _recentFiles.value = recentFiles
            }
        }
    }

    fun loadRecentlyRead() {
        if (recentlyReadObserved) return
        recentlyReadObserved = true
        viewModelScope.launch {
            progressRepository.getRecentlyRead().collect { recentlyRead ->
                _recentlyRead.value = recentlyRead
            }
        }
    }

    suspend fun saveProgress(progress: ReadingProgress) {
        progressRepository.saveProgress(progress)
    }

    suspend fun updateProgress(progress: ReadingProgress) {
        progressRepository.updateProgress(progress)
    }

    suspend fun deleteProgress(progress: ReadingProgress) {
        progressRepository.deleteProgress(progress)
    }

    suspend fun deleteProgressByFilePath(filePath: String) {
        progressRepository.deleteProgressByFilePath(filePath)
    }

    suspend fun getProgressByFilePath(filePath: String): ReadingProgress? {
        return progressRepository.getProgressByFilePath(filePath)
    }

    suspend fun saveOrUpdateProgress(
        filePath: String,
        fileName: String,
        position: Int = 0,
        percentage: Float = 0f,
        totalLines: Int = 0
    ): ReadingProgress {
        return progressRepository.saveOrUpdateProgress(
            filePath = filePath,
            fileName = fileName,
            position = position,
            percentage = percentage,
            totalLines = totalLines
        )
    }

    suspend fun updateReadingProgress(
        filePath: String,
        fileName: String,
        scrollPosition: Int,
        totalContentHeight: Int,
        visibleHeight: Int
    ) {
        progressRepository.updateReadingProgress(
            filePath = filePath,
            fileName = fileName,
            scrollPosition = scrollPosition,
            totalContentHeight = totalContentHeight,
            visibleHeight = visibleHeight
        )
    }

    suspend fun updateLastAccessed(filePath: String) {
        progressRepository.updateLastAccessed(filePath)
    }

    suspend fun markAsCompleted(filePath: String, completed: Boolean = true) {
        progressRepository.markAsCompleted(filePath, completed)
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = context.applicationContext as Application
                val repository = ReadingProgressRepository(application)
                ReadingProgressViewModel(application, repository)
            }
        }
    }
}
