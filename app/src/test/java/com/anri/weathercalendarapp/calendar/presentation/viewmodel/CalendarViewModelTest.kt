package com.anri.weathercalendarapp.calendar.presentation.viewmodel

import android.content.Context
import com.anri.weathercalendarapp.calendar.domain.model.HolidayEvent
import com.anri.weathercalendarapp.calendar.domain.model.response.CalendarEvent
import com.anri.weathercalendarapp.calendar.domain.model.response.CalendarRes
import com.anri.weathercalendarapp.calendar.domain.usecase.CreateCalendarEventUseCase
import com.anri.weathercalendarapp.calendar.domain.usecase.DeleteCalendarEventUseCase
import com.anri.weathercalendarapp.calendar.domain.usecase.GetCalendarUseCase
import com.anri.weathercalendarapp.calendar.domain.usecase.GetHolidaysUseCase
import com.anri.weathercalendarapp.calendar.domain.usecase.GetShowHolidaysUseCase
import com.anri.weathercalendarapp.calendar.domain.usecase.SyncCalendarEventsToLocalUseCase
import com.anri.weathercalendarapp.calendar.domain.usecase.UpdateCalendarEventUseCase
import com.anri.weathercalendarapp.calendar.presentation.type.CalendarFailureType
import com.anri.weathercalendarapp.common.Resource
import com.anri.weathercalendarapp.common.auth.AuthPreferences
import com.anri.weathercalendarapp.common.auth.GoogleAuthTokenProvider
import com.anri.weathercalendarapp.common.auth.TokenResult
import com.anri.weathercalendarapp.common.network.NetworkAvailabilityChecker
import com.anri.weathercalendarapp.main.presentation.GlobalUiManager
import com.anri.weathercalendarapp.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val googleAuthTokenProvider: GoogleAuthTokenProvider = mockk(relaxed = true)
    private val authPreferences: AuthPreferences = mockk()
    private val getCalendarUseCase: GetCalendarUseCase = mockk()
    private val getHolidaysUseCase: GetHolidaysUseCase = mockk()
    private val getShowHolidaysUseCase: GetShowHolidaysUseCase = mockk()
    private val createCalendarEventUseCase: CreateCalendarEventUseCase = mockk()
    private val updateCalendarEventUseCase: UpdateCalendarEventUseCase = mockk()
    private val deleteCalendarEventUseCase: DeleteCalendarEventUseCase = mockk()
    private val syncCalendarEventsToLocalUseCase: SyncCalendarEventsToLocalUseCase = mockk()
    private val globalUiManager: GlobalUiManager = mockk(relaxed = true)
    private val networkAvailabilityChecker: NetworkAvailabilityChecker = mockk()
    private val appContext: Context = mockk()

    private val token = "test-token"
    private val refreshedToken = "refreshed-token"
    private val testEmail = "user@example.com"

    private val sampleEvent = CalendarEvent(
        id = "evt1",
        summary = "Sample",
        start = "2026-05-15",
        end = "2026-05-15",
        isAllDayEvent = true
    )
    private val sampleHoliday = HolidayEvent(
        date = LocalDate.of(2026, 1, 1),
        name = "元日"
    )

    private fun httpException(code: Int): HttpException = mockk(relaxed = true) {
        every { code() } returns code
    }

    @Before
    fun setup() {
        every { getShowHolidaysUseCase() } returns flowOf(true)
        every { authPreferences.accountEmail } returns flowOf(testEmail)
        every { networkAvailabilityChecker.isOfflineDueToAirplaneMode() } returns false
        coEvery { syncCalendarEventsToLocalUseCase(any()) } returns Unit
    }

    private fun createViewModel(): CalendarViewModel = CalendarViewModel(
        googleAuthTokenProvider = googleAuthTokenProvider,
        authPreferences = authPreferences,
        getCalendarUseCase = getCalendarUseCase,
        getHolidaysUseCase = getHolidaysUseCase,
        getShowHolidaysUseCase = getShowHolidaysUseCase,
        createCalendarEventUseCase = createCalendarEventUseCase,
        updateCalendarEventUseCase = updateCalendarEventUseCase,
        deleteCalendarEventUseCase = deleteCalendarEventUseCase,
        syncCalendarEventsToLocalUseCase = syncCalendarEventsToLocalUseCase,
        globalUiManager = globalUiManager,
        networkAvailabilityChecker = networkAvailabilityChecker,
        appContext = appContext
    )

    // =========================================================================
    // runCalendarProcess (起動)
    // =========================================================================

    @Test
    fun `runCalendarProcess - accountEmail=null → isInitialized=true・isAuthorized=false・failureType=null・onComplete呼ばれる`() = runTest {
        every { authPreferences.accountEmail } returns flowOf(null)
        val vm = createViewModel()
        var completed = false
        vm.runCalendarProcess { completed = true }
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.isInitialized)
        assertFalse(state.isAuthorized)
        assertNull(state.failureType)
        assertTrue(completed)
    }

    @Test
    fun `runCalendarProcess - 機内モード → isInitialized=true・failureType=API_NETWORK_ERROR・onComplete呼ばれる`() = runTest {
        every { networkAvailabilityChecker.isOfflineDueToAirplaneMode() } returns true
        val vm = createViewModel()
        var completed = false
        vm.runCalendarProcess { completed = true }
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.isInitialized)
        assertEquals(CalendarFailureType.API_NETWORK_ERROR, state.failureType)
        assertTrue(completed)
    }

    @Test
    fun `runCalendarProcess - token NeedsConsent → 再連携UI・clearCachedToken呼ばれる・onComplete呼ばれる`() = runTest {
        coEvery { googleAuthTokenProvider.requestAccessToken(any(), any()) } returns TokenResult.NeedsConsent
        val vm = createViewModel()
        var completed = false
        vm.runCalendarProcess { completed = true }
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.isInitialized)
        assertFalse(state.isAuthorized)
        assertEquals(CalendarFailureType.API_UNAUTHORIZED, state.failureType)
        assertTrue(completed)
        verify(exactly = 1) { googleAuthTokenProvider.clearCachedToken() }
    }

    @Test
    fun `runCalendarProcess - token TransientFailure 初回 → isInitialized=true・failureType=API_NETWORK_ERROR・onComplete呼ばれる`() = runTest {
        coEvery { googleAuthTokenProvider.requestAccessToken(any(), any()) } returns TokenResult.TransientFailure
        val vm = createViewModel()
        var completed = false
        vm.runCalendarProcess { completed = true }
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.isInitialized)
        assertEquals(CalendarFailureType.API_NETWORK_ERROR, state.failureType)
        assertTrue(completed)
    }

    @Test
    fun `runCalendarProcess - 全成功 → events・holidays・isAuthorized=true反映・Local保存・onComplete呼ばれる`() = runTest {
        coEvery { googleAuthTokenProvider.requestAccessToken(any(), any()) } returns TokenResult.Success(token)
        every { getCalendarUseCase(any(), eq(token)) } returns flowOf(Resource.Success(CalendarRes(items = listOf(sampleEvent))))
        coEvery { getHolidaysUseCase(any(), any(), eq(token)) } returns listOf(sampleHoliday)

        val vm = createViewModel()
        var completed = false
        vm.runCalendarProcess { completed = true }
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.isInitialized)
        assertTrue(state.isAuthorized)
        assertEquals(listOf(sampleEvent), state.events)
        assertEquals(listOf(sampleHoliday), state.holidays)
        assertNull(state.failureType)
        assertTrue(completed)
        coVerify(exactly = 1) { syncCalendarEventsToLocalUseCase(listOf(sampleEvent)) }
    }

    @Test
    fun `runCalendarProcess - events 401 → リトライSuccess → 正常反映`() = runTest {
        coEvery { googleAuthTokenProvider.requestAccessToken(appContext, forceRefresh = false) } returns TokenResult.Success(token)
        coEvery { googleAuthTokenProvider.requestAccessToken(appContext, forceRefresh = true) } returns TokenResult.Success(refreshedToken)
        every { getCalendarUseCase(any(), eq(token)) } returns flowOf(Resource.Error(cause = httpException(401)))
        every { getCalendarUseCase(any(), eq(refreshedToken)) } returns flowOf(Resource.Success(CalendarRes(items = listOf(sampleEvent))))
        coEvery { getHolidaysUseCase(any(), any(), any()) } returns listOf(sampleHoliday)

        val vm = createViewModel()
        vm.runCalendarProcess { }
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.isAuthorized)
        assertEquals(listOf(sampleEvent), state.events)
        assertNull(state.failureType)
    }

    @Test
    fun `runCalendarProcess - events 401 → リトライ後も401 → failureType=API_UNKNOWN`() = runTest {
        coEvery { googleAuthTokenProvider.requestAccessToken(any(), any()) } returns TokenResult.Success(token)
        every { getCalendarUseCase(any(), any()) } returns flowOf(Resource.Error(cause = httpException(401)))
        coEvery { getHolidaysUseCase(any(), any(), any()) } returns listOf(sampleHoliday)

        val vm = createViewModel()
        vm.runCalendarProcess { }
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.isInitialized)
        assertEquals(CalendarFailureType.API_UNKNOWN, state.failureType)
    }

    @Test
    fun `runCalendarProcess - events 401 → リトライNeedsConsent → 再連携UI`() = runTest {
        coEvery { googleAuthTokenProvider.requestAccessToken(appContext, forceRefresh = false) } returns TokenResult.Success(token)
        coEvery { googleAuthTokenProvider.requestAccessToken(appContext, forceRefresh = true) } returns TokenResult.NeedsConsent
        every { getCalendarUseCase(any(), any()) } returns flowOf(Resource.Error(cause = httpException(401)))
        coEvery { getHolidaysUseCase(any(), any(), any()) } returns listOf(sampleHoliday)

        val vm = createViewModel()
        vm.runCalendarProcess { }
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.isInitialized)
        assertFalse(state.isAuthorized)
        assertEquals(CalendarFailureType.API_UNAUTHORIZED, state.failureType)
    }

    @Test
    fun `runCalendarProcess - events 5xx → failureType=API_SERVER_ERROR`() = runTest {
        coEvery { googleAuthTokenProvider.requestAccessToken(any(), any()) } returns TokenResult.Success(token)
        every { getCalendarUseCase(any(), any()) } returns flowOf(Resource.Error(cause = httpException(500)))
        coEvery { getHolidaysUseCase(any(), any(), any()) } returns listOf(sampleHoliday)

        val vm = createViewModel()
        vm.runCalendarProcess { }
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.isInitialized)
        assertEquals(CalendarFailureType.API_SERVER_ERROR, state.failureType)
    }

    @Test
    fun `runCalendarProcess - events Success・holidays失敗 → events反映・holiday Toast発火`() = runTest {
        coEvery { googleAuthTokenProvider.requestAccessToken(any(), any()) } returns TokenResult.Success(token)
        every { getCalendarUseCase(any(), any()) } returns flowOf(Resource.Success(CalendarRes(items = listOf(sampleEvent))))
        coEvery { getHolidaysUseCase(any(), any(), any()) } throws IOException("holiday fail")

        val vm = createViewModel()
        vm.runCalendarProcess { }
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.isInitialized)
        assertTrue(state.isAuthorized)
        assertEquals(listOf(sampleEvent), state.events)
        assertNull(state.failureType)
        verify(atLeast = 1) { globalUiManager.emitToast(any<Int>()) }
    }

    // =========================================================================
    // runCalendarApiOnly (OnResume)
    // =========================================================================

    @Test
    fun `runCalendarApiOnly - 機内モード → failureType=API_NETWORK_ERROR・isInitialized触らず`() = runTest {
        every { networkAvailabilityChecker.isOfflineDueToAirplaneMode() } returns true
        val vm = createViewModel()
        vm.runCalendarApiOnly()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isInitialized)
        assertEquals(CalendarFailureType.API_NETWORK_ERROR, state.failureType)
    }

    @Test
    fun `runCalendarApiOnly - token NeedsConsent → 再連携UI・isInitialized触らず・clearCachedToken呼ばれる`() = runTest {
        coEvery { googleAuthTokenProvider.requestAccessToken(any(), any()) } returns TokenResult.NeedsConsent
        val vm = createViewModel()
        vm.runCalendarApiOnly()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isInitialized)
        assertFalse(state.isAuthorized)
        assertEquals(CalendarFailureType.API_UNAUTHORIZED, state.failureType)
        verify(exactly = 1) { googleAuthTokenProvider.clearCachedToken() }
    }

    @Test
    fun `runCalendarApiOnly - token TransientFailure 初回 → 完全サイレント (state変更なし)`() = runTest {
        coEvery { googleAuthTokenProvider.requestAccessToken(any(), any()) } returns TokenResult.TransientFailure
        val vm = createViewModel()
        val initial = vm.uiState.value
        vm.runCalendarApiOnly()
        advanceUntilIdle()

        assertEquals(initial, vm.uiState.value)
    }

    @Test
    fun `runCalendarApiOnly - 全成功 → events・holidays・isAuthorized=true反映・isInitialized触らず`() = runTest {
        coEvery { googleAuthTokenProvider.requestAccessToken(any(), any()) } returns TokenResult.Success(token)
        every { getCalendarUseCase(any(), eq(token)) } returns flowOf(Resource.Success(CalendarRes(items = listOf(sampleEvent))))
        coEvery { getHolidaysUseCase(any(), any(), eq(token)) } returns listOf(sampleHoliday)

        val vm = createViewModel()
        vm.runCalendarApiOnly()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isInitialized)
        assertTrue(state.isAuthorized)
        assertEquals(listOf(sampleEvent), state.events)
        assertEquals(listOf(sampleHoliday), state.holidays)
        assertNull(state.failureType)
    }

    @Test
    fun `runCalendarApiOnly - events 401 → リトライSuccess → 正常反映`() = runTest {
        coEvery { googleAuthTokenProvider.requestAccessToken(appContext, forceRefresh = false) } returns TokenResult.Success(token)
        coEvery { googleAuthTokenProvider.requestAccessToken(appContext, forceRefresh = true) } returns TokenResult.Success(refreshedToken)
        every { getCalendarUseCase(any(), eq(token)) } returns flowOf(Resource.Error(cause = httpException(401)))
        every { getCalendarUseCase(any(), eq(refreshedToken)) } returns flowOf(Resource.Success(CalendarRes(items = listOf(sampleEvent))))
        coEvery { getHolidaysUseCase(any(), any(), any()) } returns listOf(sampleHoliday)

        val vm = createViewModel()
        vm.runCalendarApiOnly()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.isAuthorized)
        assertEquals(listOf(sampleEvent), state.events)
    }

    @Test
    fun `runCalendarApiOnly - events 401 → リトライTransientFailure → failureType=API_NETWORK_ERROR・isInitialized触らず`() = runTest {
        coEvery { googleAuthTokenProvider.requestAccessToken(appContext, forceRefresh = false) } returns TokenResult.Success(token)
        coEvery { googleAuthTokenProvider.requestAccessToken(appContext, forceRefresh = true) } returns TokenResult.TransientFailure
        every { getCalendarUseCase(any(), any()) } returns flowOf(Resource.Error(cause = httpException(401)))
        coEvery { getHolidaysUseCase(any(), any(), any()) } returns listOf(sampleHoliday)

        val vm = createViewModel()
        vm.runCalendarApiOnly()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isInitialized)
        assertEquals(CalendarFailureType.API_NETWORK_ERROR, state.failureType)
    }

    @Test
    fun `runCalendarApiOnly - events 5xx → failureType=API_SERVER_ERROR・isInitialized触らず`() = runTest {
        coEvery { googleAuthTokenProvider.requestAccessToken(any(), any()) } returns TokenResult.Success(token)
        every { getCalendarUseCase(any(), any()) } returns flowOf(Resource.Error(cause = httpException(500)))
        coEvery { getHolidaysUseCase(any(), any(), any()) } returns listOf(sampleHoliday)

        val vm = createViewModel()
        vm.runCalendarApiOnly()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isInitialized)
        assertEquals(CalendarFailureType.API_SERVER_ERROR, state.failureType)
    }
}
