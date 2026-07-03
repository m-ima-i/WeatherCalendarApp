package com.anri.weathercalendarapp.setting.presentation.viewmodel

import app.cash.turbine.test
import com.anri.weathercalendarapp.calendar.domain.usecase.GetShowHolidaysUseCase
import com.anri.weathercalendarapp.calendar.domain.usecase.SetShowHolidaysUseCase
import com.anri.weathercalendarapp.common.auth.AuthPreferences
import com.anri.weathercalendarapp.common.auth.LogoutUseCase
import com.anri.weathercalendarapp.main.presentation.GlobalUiManager
import com.anri.weathercalendarapp.widget.domain.usecase.GetWidgetOpacityUseCase
import com.anri.weathercalendarapp.widget.domain.usecase.SetWidgetOpacityUseCase
import com.anri.weathercalendarapp.widget.domain.usecase.UpdateAllWidgetsUseCase
import com.anri.weathercalendarapp.util.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authPreferences: AuthPreferences = mockk(relaxed = true)
    private val logoutUseCase: LogoutUseCase = mockk(relaxUnitFun = true)
    private val getWidgetOpacityUseCase: GetWidgetOpacityUseCase = mockk(relaxed = true)
    private val setWidgetOpacityUseCase: SetWidgetOpacityUseCase = mockk(relaxUnitFun = true)
    private val updateAllWidgetsUseCase: UpdateAllWidgetsUseCase = mockk(relaxUnitFun = true)
    private val getShowHolidaysUseCase: GetShowHolidaysUseCase = mockk(relaxed = true)
    private val setShowHolidaysUseCase: SetShowHolidaysUseCase = mockk(relaxUnitFun = true)
    private val globalUiManager: GlobalUiManager = mockk(relaxed = true)

    private val isCalendarAuthorizedFlow = MutableStateFlow(false)
    private val widgetOpacityFlow = MutableStateFlow(100)
    private val showHolidaysFlow = MutableStateFlow(true)

    private lateinit var viewModel: SettingViewModel

    @Before
    fun setup() {
        every { authPreferences.isCalendarAuthorized } returns isCalendarAuthorizedFlow
        every { authPreferences.accountEmail } returns MutableStateFlow(null)
        every { getWidgetOpacityUseCase() } returns widgetOpacityFlow
        every { getShowHolidaysUseCase() } returns showHolidaysFlow
    }

    private fun createViewModel(): SettingViewModel {
        return SettingViewModel(
            authPreferences = authPreferences,
            logoutUseCase = logoutUseCase,
            getWidgetOpacityUseCase = getWidgetOpacityUseCase,
            setWidgetOpacityUseCase = setWidgetOpacityUseCase,
            updateAllWidgetsUseCase = updateAllWidgetsUseCase,
            getShowHolidaysUseCase = getShowHolidaysUseCase,
            setShowHolidaysUseCase = setShowHolidaysUseCase,
            globalUiManager = globalUiManager,
        )
    }

    // ========== init ==========

    @Test
    fun `init - isCalendarAuthorizedがauthPreferencesから反映される`() = runTest {
        // Arrange
        isCalendarAuthorizedFlow.value = true

        // Act
        viewModel = createViewModel()
        advanceUntilIdle()

        // Assert
        assertTrue(viewModel.uiState.value.isCalendarAuthorized)
    }

    // ========== onLogout ==========

    @Test
    fun `onLogout - logoutUseCaseが呼ばれLogoutCompletedイベントが発行される`() = runTest {
        // Arrange
        viewModel = createViewModel()
        advanceUntilIdle()

        // Act & Assert
        viewModel.uiEvent.test {
            viewModel.onLogout()
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is SettingUiEvent.LogoutCompleted)
        }

        coVerify(exactly = 1) { logoutUseCase() }
        assertFalse(viewModel.uiState.value.isCalendarAuthorized)
        assertNull(viewModel.uiState.value.accountEmail)
    }
}
