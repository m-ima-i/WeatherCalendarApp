package com.anri.weathercalendarapp.calendar.domain.usecase

import com.anri.weathercalendarapp.calendar.domain.model.request.CreateEventReq
import com.anri.weathercalendarapp.calendar.domain.model.response.CalendarEvent
import com.anri.weathercalendarapp.calendar.domain.repository.CalendarRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class CreateCalendarEventUseCaseTest {

    private val repository: CalendarRepository = mockk()
    private val useCase = CreateCalendarEventUseCase(repository)

    @Test
    fun `正常系 - 予定を作成できる`() = runTest {
        // Arrange
        val body = CreateEventReq(
            summary = "テスト予定",
            isAllDay = false,
            startDate = LocalDate.of(2026, 3, 4),
            startTime = LocalTime.of(10, 0),
            endDate = LocalDate.of(2026, 3, 4),
            endTime = LocalTime.of(11, 0)
        )
        val expected = CalendarEvent(
            id = "event123",
            summary = "テスト予定",
            start = "2026-03-04T10:00:00+09:00",
            end = "2026-03-04T11:00:00+09:00",
            isAllDayEvent = false
        )
        coEvery { repository.createEvent("token123", body) } returns Result.success(expected)

        // Act
        val result = useCase("token123", body)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals("event123", result.getOrThrow().id)
        assertEquals("テスト予定", result.getOrThrow().summary)
        coVerify(exactly = 1) { repository.createEvent("token123", body) }
    }

    @Test
    fun `正常系 - 終日予定を作成できる`() = runTest {
        // Arrange
        val body = CreateEventReq(
            summary = "終日イベント",
            isAllDay = true,
            startDate = LocalDate.of(2026, 3, 4)
        )
        val expected = CalendarEvent(
            id = "event456",
            summary = "終日イベント",
            start = "2026-03-04",
            end = "2026-03-05",
            isAllDayEvent = true
        )
        coEvery { repository.createEvent("token123", body) } returns Result.success(expected)

        // Act
        val result = useCase("token123", body)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrThrow().isAllDayEvent)
    }

    @Test
    fun `異常系 - Repository が失敗した場合 Result_failure が返る`() = runTest {
        // Arrange
        val body = CreateEventReq(
            summary = "テスト",
            isAllDay = false,
            startDate = LocalDate.of(2026, 3, 4),
            startTime = LocalTime.of(10, 0),
            endDate = LocalDate.of(2026, 3, 4),
            endTime = LocalTime.of(11, 0)
        )
        coEvery { repository.createEvent(any(), any()) } returns Result.failure(RuntimeException("API Error"))

        // Act
        val result = useCase("token123", body)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("API Error", result.exceptionOrNull()?.message)
    }
}
