package com.anri.weathercalendarapp.calendar.data.repositoryimpl

import com.anri.weathercalendarapp.calendar.data.datasource.HolidayRemoteDataSource
import com.anri.weathercalendarapp.calendar.domain.model.HolidayEvent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class HolidayRepositoryImplTest {

    private val remoteDataSource: HolidayRemoteDataSource = mockk()

    private lateinit var repository: HolidayRepositoryImpl

    private val testHolidays = listOf(
        HolidayEvent(
            date = LocalDate.of(2026, 1, 1),
            name = "元日"
        ),
        HolidayEvent(
            date = LocalDate.of(2026, 1, 12),
            name = "成人の日"
        )
    )

    @Before
    fun setup() {
        repository = HolidayRepositoryImpl(remoteDataSource)
    }

    // ========== getHolidays - リモートから取得 ==========

    @Test
    fun `getHolidays - リモートから取得して返す`() = runTest {
        // Arrange
        val startYearMonth = "2025-04"
        val endYearMonth = "2027-04"
        val authToken = "test-token"

        coEvery {
            remoteDataSource.getHolidays(
                timeMin = "2025-04-01T00:00:00Z",
                timeMax = "2027-05-01T00:00:00Z",
                authToken = authToken
            )
        } returns testHolidays

        // Act
        val result = repository.getHolidays(startYearMonth, endYearMonth, authToken)

        // Assert
        assertEquals(testHolidays, result)
        coVerify(exactly = 1) {
            remoteDataSource.getHolidays(
                timeMin = "2025-04-01T00:00:00Z",
                timeMax = "2027-05-01T00:00:00Z",
                authToken = authToken
            )
        }
    }

    // ========== 年越し (endYearMonth="2027-12") ==========

    @Test
    fun `getHolidays - endYearMonthが12月のときtimeMaxが翌年1月になる`() = runTest {
        // Arrange
        val startYearMonth = "2026-01"
        val endYearMonth = "2027-12"
        val authToken = "test-token"

        coEvery {
            remoteDataSource.getHolidays(
                timeMin = "2026-01-01T00:00:00Z",
                timeMax = "2028-01-01T00:00:00Z",
                authToken = authToken
            )
        } returns emptyList()

        // Act
        val result = repository.getHolidays(startYearMonth, endYearMonth, authToken)

        // Assert
        assertEquals(emptyList<HolidayEvent>(), result)
        coVerify(exactly = 1) {
            remoteDataSource.getHolidays(
                timeMin = "2026-01-01T00:00:00Z",
                timeMax = "2028-01-01T00:00:00Z",
                authToken = authToken
            )
        }
    }
}
