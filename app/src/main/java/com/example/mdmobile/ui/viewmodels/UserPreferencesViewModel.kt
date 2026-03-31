package com.example.mdmobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mdmobile.data.model.ThemeMode
import com.example.mdmobile.data.model.UserPreferences
import com.example.mdmobile.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// 暂时不使用Hilt，简化实现
class UserPreferencesViewModel(
    private val repository: UserPreferencesRepository
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences> = repository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    fun updateThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            repository.updateThemeMode(themeMode)
        }
    }

    fun updateDefaultFolder(folderPath: String?) {
        viewModelScope.launch {
            repository.updateDefaultFolder(folderPath)
        }
    }

    fun updateFontSize(fontSize: Int) {
        viewModelScope.launch {
            repository.updateFontSize(fontSize)
        }
    }
}