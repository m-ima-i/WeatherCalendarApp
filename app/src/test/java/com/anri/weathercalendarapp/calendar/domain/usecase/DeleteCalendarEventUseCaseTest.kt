package com.anri.weathercalendarapp.calendar.domain.usecase

import com.anri.weathercalendarapp.calendar.domain.repository.CalendarRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteCalendarEventUseCaseTest {

    private val repository: CalendarRepository = mockk()
    private val useCase = DeleteCalendarEventUseCase(repository)

    @Test
    fun `正常系 - 予定を削除できる`() = runTest {
        // Arrange
        coEvery { repository.deleteEvent("token123", "event456") } returns Result.success(Unit)

        // Act
        val result = useCase("token123", "event456")

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.deleteEvent("token123", "event456") }
    }

    @Test
    fun `正常系 - 別のイベントIDで削除できる`() = runTest {
        // Arrange
        coEvery { repository.deleteEvent("token-abc", "evt-xyz") } returns Result.success(Unit)

        // Act
        val result = useCase("token-abc", "evt-xyz")

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.deleteEvent("token-abc", "evt-xyz") }
    }

    @Test
    fun `異常系 - Repository が失敗した場合 Result_failure が返る`() = runTest {
        // Arrange
        coEvery { repository.deleteEvent(any(), any()) } returns Result.failure(RuntimeException("Not found"))

        // Act
        val result = useCase("token123", "invalid-id")

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Not found", result.exceptionOrNull()?.message)
    }
}
