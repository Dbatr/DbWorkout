package com.dbworkout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dbworkout.data.repository.AppThemeMode
import com.dbworkout.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {
    val themeMode: StateFlow<AppThemeMode> = repository.themeMode.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AppThemeMode.SYSTEM,
    )

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }
}
