package com.anri.weathercalendarapp.weather.presentation.viewmodel

import com.anri.weathercalendarapp.common.Resource
import com.anri.weathercalendarapp.common.auth.AuthPreferences
import com.anri.weathercalendarapp.common.geocoder.Address
import com.anri.weathercalendarapp.common.location.Location
import com.anri.weathercalendarapp.common.location.LocationTracker
import com.anri.weathercalendarapp.common.network.NetworkAvailabilityChecker
import com.anri.weathercalendarapp.util.MainDispatcherRule
import com.anri.weathercalendarapp.weather.domain.model.response.Current
import com.anri.weathercalendarapp.weather.domain.model.response.Weather
import com.anri.weathercalendarapp.weather.domain.model.response.WeatherDescription
import com.anri.weathercalendarapp.weather.domain.model.response.WeatherWithAddress
import com.anri.weathercalendarapp.weather.domain.usecase.GetCachedAddressUseCase
import com.anri.weathercalendarapp.weather.domain.usecase.GetCachedWeatherUseCase
import com.anri.weathercalendarapp.weather.domain.usecase.UpdateWeatherUseCase
import com.anri.weathercalendarapp.weather.presentation.type.WeatherFailureType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val updateWeatherUseCase: UpdateWeatherUseCase = mockk()
    private val locationTracker: LocationTracker = mockk()
    private val getCachedWeatherUseCase: GetCachedWeatherUseCase = mockk()
    private val getCachedAddressUseCase: GetCachedAddressUseCase = mockk()
    private val authPreferences: AuthPreferences = mockk()
    private val networkAvailabilityChecker: NetworkAvailabilityChecker = mockk()

    private val testLocation = Location(latitude = 35.6762, longitude = 139.6503)
    private val testAddress = Address(
        adminArea = "東京都",
        locality = "渋谷区",
        subLocality = "神南",
        thoroughfare = null
    )
    private val testWeather = Weather(
        timezone = "Asia/Tokyo",
        current = Current(
            temp = 20.0,
            feelsLike = 19.0,
            humidity = 60,
            windSpeed = 3.0,
            weather = listOf(
                WeatherDescription(icon = "01d")
            )
        ),
        hourly = emptyList(),
        daily = emptyList()
    )
    private val cachedWeather = testWeather.copy(timezone = "Asia/Tokyo-cached")
    private val testWeatherWithAddress = WeatherWithAddress(testWeather, testAddress)

    @Before
    fun setup() {
        every { locationTracker.hasLocationPermission() } returns true
        every { locationTracker.isGpsEnabled() } returns true
        every { authPreferences.locationEvaluated } returns flowOf(false)
        every { authPreferences.gpsEvaluated } returns flowOf(false)
        coEvery { authPreferences.setLocationEvaluated() } returns Unit
        coEvery { authPreferences.setGpsEvaluated() } returns Unit
        coEvery { getCachedAddressUseCase() } returns null
        every { networkAvailabilityChecker.isOfflineDueToAirplaneMode() } returns false
    }

    private fun createViewModel(): WeatherViewModel {
        return WeatherViewModel(
            updateWeatherUseCase = updateWeatherUseCase,
            locationTracker = locationTracker,
            getCachedWeatherUseCase = getCachedWeatherUseCase,
            getCachedAddressUseCase = getCachedAddressUseCase,
            authPreferences = authPreferences,
            networkAvailabilityChecker = networkAvailabilityChecker,
        )
    }

    // =========================================================================
    // loadLocalWeather
    // =========================================================================

    @Test
    fun `loadLocalWeather - キャッシュあり→weatherに反映`() = runTest {
        coEvery { getCachedWeatherUseCase() } returns cachedWeather

        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.loadLocalWeather()
        advanceUntilIdle()

        assertEquals(cachedWeather, viewModel.uiState.value.weather)
    }

    @Test
    fun `loadLocalWeather - 地名キャッシュあり→currentAddressに反映`() = runTest {
        coEvery { getCachedWeatherUseCase() } returns cachedWeather
        coEvery { getCachedAddressUseCase() } returns testAddress

        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.loadLocalWeather()
        advanceUntilIdle()

        assertEquals(testAddress, viewModel.uiState.value.currentAddress)
    }

    @Test
    fun `loadLocalWeather - キャッシュなし→weatherはnullのまま`() = runTest {
        coEvery { getCachedWeatherUseCase() } returns null

        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.loadLocalWeather()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.weather)
    }

    // =========================================================================
    // runWeatherProcess - 権限/GPS フラグ分岐
    // =========================================================================

    @Test
    fun `runWeatherProcess - 権限なし＆locationEvaluated未消費→onLocationPermissionRequired呼び出し・onComplete呼ばれない`() = runTest {
        every { locationTracker.hasLocationPermission() } returns false
        every { authPreferences.locationEvaluated } returns flowOf(false)

        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        var locationCalled = false
        var gpsCalled = false
        var completeCalled = false
        viewModel.runWeatherProcess(
            onLocationPermissionRequired = { locationCalled = true },
            onGpsRequired = { gpsCalled = true },
            onComplete = { completeCalled = true }
        )
        advanceUntilIdle()

        assertTrue(locationCalled)
        assertFalse(gpsCalled)
        assertFalse(completeCalled)
    }

    @Test
    fun `runWeatherProcess - 権限なし＆locationEvaluated消費済→onComplete・Dialogなし`() = runTest {
        every { locationTracker.hasLocationPermission() } returns false
        every { authPreferences.locationEvaluated } returns flowOf(true)

        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        var locationCalled = false
        var completeCalled = false
        viewModel.runWeatherProcess(
            onLocationPermissionRequired = { locationCalled = true },
            onComplete = { completeCalled = true }
        )
        advanceUntilIdle()

        assertFalse(locationCalled)
        assertTrue(completeCalled)
    }

    @Test
    fun `runWeatherProcess - 権限あり＆GPS無効＆gpsEvaluated未消費→onGpsRequired呼び出し・onComplete呼ばれない`() = runTest {
        every { locationTracker.isGpsEnabled() } returns false
        every { authPreferences.gpsEvaluated } returns flowOf(false)

        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        var locationCalled = false
        var gpsCalled = false
        var completeCalled = false
        viewModel.runWeatherProcess(
            onLocationPermissionRequired = { locationCalled = true },
            onGpsRequired = { gpsCalled = true },
            onComplete = { completeCalled = true }
        )
        advanceUntilIdle()

        assertFalse(locationCalled)
        assertTrue(gpsCalled)
        assertFalse(completeCalled)
    }

    @Test
    fun `runWeatherProcess - 権限あり＆GPS無効＆gpsEvaluated消費済→onComplete・Dialogなし`() = runTest {
        every { locationTracker.isGpsEnabled() } returns false
        every { authPreferences.gpsEvaluated } returns flowOf(true)

        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        var gpsCalled = false
        var completeCalled = false
        viewModel.runWeatherProcess(
            onGpsRequired = { gpsCalled = true },
            onComplete = { completeCalled = true }
        )
        advanceUntilIdle()

        assertFalse(gpsCalled)
        assertTrue(completeCalled)
    }

    // =========================================================================
    // 評価済フラグ消費（API成功経路でも常に消費される）
    // =========================================================================

    @Test
    fun `runWeatherProcess - 権限ON経路でsetLocationEvaluated呼び出し`() = runTest {
        coEvery { locationTracker.getCurrentLocation() } returns testLocation
        every { updateWeatherUseCase(testLocation) } returns flowOf(
            Resource.Success(testWeatherWithAddress)
        )

        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.runWeatherProcess()
        advanceUntilIdle()

        coVerify { authPreferences.setLocationEvaluated() }
    }

    @Test
    fun `runWeatherProcess - GPS ON経路でsetGpsEvaluated呼び出し`() = runTest {
        coEvery { locationTracker.getCurrentLocation() } returns testLocation
        every { updateWeatherUseCase(testLocation) } returns flowOf(
            Resource.Success(testWeatherWithAddress)
        )

        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.runWeatherProcess()
        advanceUntilIdle()

        coVerify { authPreferences.setGpsEvaluated() }
    }

    @Test
    fun `runWeatherApiOnly - 権限GPS ON経路で両フラグ消費`() = runTest {
        coEvery { locationTracker.getCurrentLocation() } returns testLocation
        every { updateWeatherUseCase(testLocation) } returns flowOf(
            Resource.Success(testWeatherWithAddress)
        )

        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.runWeatherApiOnly()
        advanceUntilIdle()

        coVerify { authPreferences.setLocationEvaluated() }
        coVerify { authPreferences.setGpsEvaluated() }
    }

    // =========================================================================
    // runWeatherProcess - 通常フロー（権限/GPS共にOK）
    // =========================================================================

    @Test
    fun `runWeatherProcess正常系 - 位置取得→Repositoryから天気と地名をUIに反映`() = runTest {
        coEvery { locationTracker.getCurrentLocation() } returns testLocation
        every { updateWeatherUseCase(testLocation) } returns flowOf(
            Resource.Loading(),
            Resource.Success(testWeatherWithAddress)
        )

        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.runWeatherProcess()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.failureType)
        assertEquals(testWeather, viewModel.uiState.value.weather)
        assertEquals(testAddress, viewModel.uiState.value.currentAddress)
    }

    @Test
    fun `runWeatherProcess異常系 - 位置情報null→failureType=LOCATION_FAILED・weather不変`() = runTest {
        coEvery { getCachedWeatherUseCase() } returns cachedWeather
        coEvery { locationTracker.getCurrentLocation() } returns null

        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.loadLocalWeather()
        advanceUntilIdle()

        viewModel.runWeatherProcess()
        advanceUntilIdle()

        assertEquals(cachedWeather, viewModel.uiState.value.weather)
        assertEquals(WeatherFailureType.LOCATION_FAILED, viewModel.uiState.value.failureType)
    }

    @Test
    fun `runWeatherProcess異常系 - 位置情報例外→failureType=LOCATION_FAILED・weather不変`() = runTest {
        coEvery { getCachedWeatherUseCase() } returns cachedWeather
        coEvery { locationTracker.getCurrentLocation() } throws RuntimeException("GPS Error")

        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.loadLocalWeather()
        advanceUntilIdle()

        viewModel.runWeatherProcess()
        advanceUntilIdle()

        assertEquals(cachedWeather, viewModel.uiState.value.weather)
        assertEquals(WeatherFailureType.LOCATION_FAILED, viewModel.uiState.value.failureType)
    }

    @Test
    fun `runWeatherProcess異常系 - API失敗→failureTypeセット・weather不変`() = runTest {
        coEvery { getCachedWeatherUseCase() } returns cachedWeather
        coEvery { locationTracker.getCurrentLocation() } returns testLocation
        every { updateWeatherUseCase(testLocation) } returns flowOf(
            Resource.Loading(),
            Resource.Error()
        )

        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.loadLocalWeather()
        advanceUntilIdle()

        viewModel.runWeatherProcess()
        advanceUntilIdle()

        assertEquals(cachedWeather, viewModel.uiState.value.weather)
        // Resource.Error with no cause → API_UNKNOWN
        assertEquals(WeatherFailureType.API_UNKNOWN, viewModel.uiState.value.failureType)
    }

    // =========================================================================
    // Geocoder 失敗時の挙動（天気API成功 × address=null）
    // =========================================================================

    @Test
    fun `runWeatherProcess - 天気成功＆Geocoder失敗→failureType=GEOCODER・currentAddressは前回値を保持`() = runTest {
        coEvery { getCachedWeatherUseCase() } returns cachedWeather
        coEvery { getCachedAddressUseCase() } returns testAddress
        coEvery { locationTracker.getCurrentLocation() } returns testLocation
        every { updateWeatherUseCase(testLocation) } returns flowOf(
            Resource.Loading(),
            Resource.Success(WeatherWithAddress(testWeather, address = null))
        )

        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.loadLocalWeather()
        advanceUntilIdle()
        // この時点で currentAddress は testAddress（キャッシュ）

        viewModel.runWeatherProcess()
        advanceUntilIdle()

        assertEquals(testWeather, viewModel.uiState.value.weather)
        // 地名は更新せず前回値を保持
        assertEquals(testAddress, viewModel.uiState.value.currentAddress)
        assertEquals(WeatherFailureType.GEOCODER, viewModel.uiState.value.failureType)
    }

    @Test
    fun `runWeatherApiOnly - 天気成功＆Geocoder失敗→failureType=GEOCODER・currentAddressは前回値を保持`() = runTest {
        coEvery { getCachedWeatherUseCase() } returns cachedWeather
        coEvery { getCachedAddressUseCase() } returns testAddress
        coEvery { locationTracker.getCurrentLocation() } returns testLocation
        every { updateWeatherUseCase(testLocation) } returns flowOf(
            Resource.Loading(),
            Resource.Success(WeatherWithAddress(testWeather, address = null))
        )

        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.loadLocalWeather()
        advanceUntilIdle()

        viewModel.runWeatherApiOnly()
        advanceUntilIdle()

        assertEquals(testWeather, viewModel.uiState.value.weather)
        assertEquals(testAddress, viewModel.uiState.value.currentAddress)
        assertEquals(WeatherFailureType.GEOCODER, viewModel.uiState.value.failureType)
    }
}
