package com.anri.weathercalendarapp.calendar.domain.usecase

import com.anri.weathercalendarapp.calendar.domain.model.HolidayEvent
import com.anri.weathercalendarapp.calendar.domain.repository.HolidayRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class GetHolidaysUseCaseTest {

    private val repository: HolidayRepository = mockk()
    private val useCase = GetHolidaysUseCase(repository)

    @Test
    fun `正常系 - 祝日リストを取得できる`() = runTest {
        // Arrange
        val startYearMonth = "2025-01"
        val endYearMonth = "2027-01"
        val authToken = "test-token"
        val holidays = listOf(
            HolidayEvent(
                date = LocalDate.of(2026, 1, 1),
                name = "元日"
            ),
            HolidayEvent(
                date = LocalDate.of(2026, 1, 12),
                name = "成人の日"
            )
        )
        coEvery { repository.getHolidays(startYearMonth, endYearMonth, authToken) } returns holidays

        // Act
        val result = useCase(startYearMonth, endYearMonth, authToken)

        // Assert
        assertEquals(holidays, result)
    }

    @Test
    fun `正常系 - 空リストが返る`() = runTest {
        // Arrange
        val startYearMonth = "2025-06"
        val endYearMonth = "2027-06"
        val authToken = "test-token"
        coEvery { repository.getHolidays(startYearMonth, endYearMonth, authToken) } returns emptyList()

        // Act
        val result = useCase(startYearMonth, endYearMonth, authToken)

        // Assert
        assertEquals(emptyList<HolidayEvent>(), result)
    }
}
