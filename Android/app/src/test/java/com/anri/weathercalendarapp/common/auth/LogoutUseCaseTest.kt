package com.anri.weathercalendarapp.common.auth

import com.anri.weathercalendarapp.calendar.domain.repository.CalendarRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LogoutUseCaseTest {

    private val authPreferences: AuthPreferences = mockk(relaxUnitFun = true)
    private val googleAuthTokenProvider: GoogleAuthTokenProvider = mockk(relaxUnitFun = true)
    private val calendarRepository: CalendarRepository = mockk(relaxUnitFun = true)
    private val useCase = LogoutUseCase(authPreferences, googleAuthTokenProvider, calendarRepository)

    @Test
    fun `ログアウト実行時にclearCachedTokenとlogoutとdeleteAllLocalEventsが呼ばれる`() = runTest {
        // Arrange
        coEvery { googleAuthTokenProvider.clearCachedToken() } just runs
        coEvery { authPreferences.logout() } just runs

        // Act
        useCase()

        // Assert
        coVerify(exactly = 1) { googleAuthTokenProvider.clearCachedToken() }
        coVerify(exactly = 1) { calendarRepository.deleteAllLocalEvents() }
        coVerify(exactly = 1) { authPreferences.logout() }
    }
}
