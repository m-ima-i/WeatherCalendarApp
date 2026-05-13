package com.anri.weathercalendarapp.setting.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anri.weathercalendarapp.R
import com.anri.weathercalendarapp.calendar.domain.usecase.GetShowHolidaysUseCase
import com.anri.weathercalendarapp.calendar.domain.usecase.SetShowHolidaysUseCase
import com.anri.weathercalendarapp.common.auth.AuthPreferences
import com.anri.weathercalendarapp.common.auth.LogoutUseCase
import com.anri.weathercalendarapp.main.presentation.GlobalUiManager
import com.anri.weathercalendarapp.setting.presentation.state.SettingUiState
import com.anri.weathercalendarapp.widget.domain.usecase.GetWidgetOpacityUseCase
import com.anri.weathercalendarapp.widget.domain.usecase.SetWidgetOpacityUseCase
import com.anri.weathercalendarapp.widget.domain.usecase.UpdateAllWidgetsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SettingUiEvent {
    data object LogoutCompleted : SettingUiEvent()
}

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val authPreferences: AuthPreferences,
    private val logoutUseCase: LogoutUseCase,
    private val getWidgetOpacityUseCase: GetWidgetOpacityUseCase,
    private val setWidgetOpacityUseCase: SetWidgetOpacityUseCase,
    private val updateAllWidgetsUseCase: UpdateAllWidgetsUseCase,
    private val getShowHolidaysUseCase: GetShowHolidaysUseCase,
    private val setShowHolidaysUseCase: SetShowHolidaysUseCase,
    private val globalUiManager: GlobalUiManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingUiState())
    val uiState: StateFlow<SettingUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<SettingUiEvent>()
    val uiEvent: SharedFlow<SettingUiEvent> = _uiEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            authPreferences.isCalendarAuthorized.collect { authorized ->
                _uiState.update { it.copy(isCalendarAuthorized = authorized) }
            }
        }

        viewModelScope.launch {
            authPreferences.accountEmail.collect { email ->
                _uiState.update { it.copy(accountEmail = email) }
            }
        }

        viewModelScope.launch {
            getWidgetOpacityUseCase().collect { opacity ->
                _uiState.update { it.copy(widgetOpacity = opacity) }
            }
        }

        viewModelScope.launch {
            getShowHolidaysUseCase().collect { show ->
                _uiState.update { it.copy(showHolidays = show) }
            }
        }
    }

    fun onLogout() {
        viewModelScope.launch {
            logoutUseCase()
            _uiState.update { it.copy(isCalendarAuthorized = false, accountEmail = null) }
            _uiEvent.emit(SettingUiEvent.LogoutCompleted)
        }
    }

    fun onWidgetOpacityChanged(opacity: Int) {
        _uiState.update { it.copy(widgetOpacity = opacity) }
    }

    fun onWidgetOpacityCommitted() {
        val opacity = _uiState.value.widgetOpacity
        viewModelScope.launch {
            setWidgetOpacityUseCase(opacity)
            updateAllWidgetsUseCase(opacity)
            globalUiManager.emitToast(R.string.toast_widget_opacity_changed)
        }
    }

    fun onShowHolidaysChanged(value: Boolean) {
        viewModelScope.launch {
            setShowHolidaysUseCase(value)
        }
    }
}
