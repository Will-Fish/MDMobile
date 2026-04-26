package com.example.mdmobile.ui.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mdmobile.data.model.ThemeMode
import com.example.mdmobile.data.model.UserPreferences
import com.example.mdmobile.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserPreferencesViewModel(
    application: Application,
    private val repository: UserPreferencesRepository
) : AndroidViewModel(application) {

    val userPreferences: StateFlow<UserPreferences> = repository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
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

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = context.applicationContext as Application
                val repository = UserPreferencesRepository(application)
                UserPreferencesViewModel(application, repository)
            }
        }
    }
}
