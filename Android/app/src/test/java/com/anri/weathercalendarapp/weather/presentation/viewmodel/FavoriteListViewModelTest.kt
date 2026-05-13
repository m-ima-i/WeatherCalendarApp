package com.anri.weathercalendarapp.weather.presentation.viewmodel

import app.cash.turbine.test
import com.anri.weathercalendarapp.main.presentation.GlobalUiManager
import com.anri.weathercalendarapp.util.MainDispatcherRule
import com.anri.weathercalendarapp.weather.domain.model.response.Current
import com.anri.weathercalendarapp.weather.domain.model.response.Daily
import com.anri.weathercalendarapp.weather.domain.model.response.FavoriteLocation
import com.anri.weathercalendarapp.weather.domain.model.response.PlaceSuggestion
import com.anri.weathercalendarapp.weather.domain.model.response.Temp
import com.anri.weathercalendarapp.weather.domain.model.response.Weather
import com.anri.weathercalendarapp.weather.domain.model.response.WeatherDescription
import com.anri.weathercalendarapp.weather.domain.repository.FavoriteLocationRepository
import com.anri.weathercalendarapp.weather.domain.usecase.AddFavoriteUseCase
import com.anri.weathercalendarapp.weather.domain.usecase.DeleteFavoriteUseCase
import com.anri.weathercalendarapp.weather.domain.usecase.FetchFavoriteWeatherUseCase
import com.anri.weathercalendarapp.weather.domain.usecase.GetFavoritesStreamUseCase
import com.anri.weathercalendarapp.weather.domain.usecase.SearchPlaces
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
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
class FavoriteListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getFavoritesStreamUseCase: GetFavoritesStreamUseCase = mockk()
    private val addFavoriteUseCase: AddFavoriteUseCase = mockk()
    private val deleteFavoriteUseCase: DeleteFavoriteUseCase = mockk()
    private val fetchFavoriteWeatherUseCase: FetchFavoriteWeatherUseCase = mockk()
    private val searchPlaces: SearchPlaces = mockk()
    private val favoriteLocationRepository: FavoriteLocationRepository = mockk(relaxed = true)
    private val globalUiManager: GlobalUiManager = mockk(relaxed = true)

    private val favoritesFlow = MutableStateFlow<List<FavoriteLocation>>(emptyList())

    private lateinit var viewModel: FavoriteListViewModel

    private val testFavorite1 = FavoriteLocation(
        id = 1L,
        placeId = "place_1",
        name = "東京タワー",
        secondaryName = "東京都港区",
        latitude = 35.6586,
        longitude = 139.7454,
        createdAt = 1000L
    )

    private val testFavorite2 = FavoriteLocation(
        id = 2L,
        placeId = "place_2",
        name = "大阪城",
        secondaryName = "大阪府大阪市",
        latitude = 34.6873,
        longitude = 135.5259,
        createdAt = 2000L
    )

    private val testWeather = Weather(
        timezone = "Asia/Tokyo",
        current = Current(
            temp = 25.0,
            feelsLike = 24.0,
            humidity = 50,
            windSpeed = 2.0,
            weather = listOf(
                WeatherDescription(icon = "01d")
            )
        ),
        hourly = emptyList(),
        daily = listOf(
            Daily(
                dt = 1000L,
                temp = Temp(min = 18.0, max = 30.0),
                pop = 0.0,
                weather = listOf(
                    WeatherDescription(icon = "01d")
                )
            )
        )
    )

    @Before
    fun setup() {
        every { getFavoritesStreamUseCase() } returns favoritesFlow
        coEvery { fetchFavoriteWeatherUseCase(any(), any()) } returns Result.success(testWeather)
    }

    private fun createViewModel(): FavoriteListViewModel {
        return FavoriteListViewModel(
            getFavoritesStreamUseCase = getFavoritesStreamUseCase,
            addFavoriteUseCase = addFavoriteUseCase,
            deleteFavoriteUseCase = deleteFavoriteUseCase,
            fetchFavoriteWeatherUseCase = fetchFavoriteWeatherUseCase,
            searchPlaces = searchPlaces,
            favoriteLocationRepository = favoriteLocationRepository,
            globalUiManager = globalUiManager,
        )
    }

    // -------------------------------------------------------------------------
    // お気に入りStream→UiState反映
    // -------------------------------------------------------------------------

    @Test
    fun `お気に入りStream→リストをUiStateに反映`() = runTest {
        // Arrange
        viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        // Act
        favoritesFlow.value = listOf(testFavorite1, testFavorite2)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(2, state.favorites.size)
        assertEquals("東京タワー", state.favorites[0].favoriteLocation.name)
        assertEquals("大阪城", state.favorites[1].favoriteLocation.name)
    }

    // -------------------------------------------------------------------------
    // fetchAllFavoriteWeather
    // -------------------------------------------------------------------------

    @Test
    fun `fetchAllFavoriteWeather - 全お気に入りの天気取得が呼ばれる`() = runTest {
        // Arrange
        favoritesFlow.value = listOf(testFavorite1, testFavorite2)
        viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        // Act
        viewModel.fetchAllFavoriteWeather()
        advanceUntilIdle()

        // Assert
        coVerify { fetchFavoriteWeatherUseCase(testFavorite1.latitude, testFavorite1.longitude) }
        coVerify { fetchFavoriteWeatherUseCase(testFavorite2.latitude, testFavorite2.longitude) }
    }

    @Test
    fun `fetchAllFavoriteWeather - API失敗→failureMapにセット（Toast発火なし）`() = runTest {
        // Arrange
        favoritesFlow.value = listOf(testFavorite1)
        coEvery { fetchFavoriteWeatherUseCase(any(), any()) } returns Result.failure(
            java.io.IOException("ネットワークエラー")
        )

        viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        // Act
        viewModel.fetchAllFavoriteWeather()
        advanceUntilIdle()

        // Assert: failureMap にセットされ、UiState の summary.failureType に反映される
        val summary = viewModel.uiState.value.favorites.first()
        assertEquals(
            com.anri.weathercalendarapp.weather.presentation.type.WeatherFailureType.API_NETWORK_ERROR,
            summary.failureType
        )
    }

    @Test
    fun `fetchAllFavoriteWeather - 全件失敗時に各お気に入りに failureType がセットされる`() = runTest {
        // Arrange
        favoritesFlow.value = listOf(testFavorite1, testFavorite2)
        coEvery { fetchFavoriteWeatherUseCase(any(), any()) } returns Result.failure(
            java.io.IOException("ネットワークエラー")
        )

        viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        // Act
        viewModel.fetchAllFavoriteWeather()
        advanceUntilIdle()

        // Assert: 2件すべてに API_NETWORK_ERROR がセット
        val summaries = viewModel.uiState.value.favorites
        assertEquals(2, summaries.size)
        summaries.forEach { summary ->
            assertEquals(
                com.anri.weathercalendarapp.weather.presentation.type.WeatherFailureType.API_NETWORK_ERROR,
                summary.failureType
            )
        }
    }

    @Test
    fun `fetchAllFavoriteWeather - 全件成功時はfailureTypeがnull`() = runTest {
        // Arrange
        favoritesFlow.value = listOf(testFavorite1, testFavorite2)
        coEvery { fetchFavoriteWeatherUseCase(any(), any()) } returns Result.success(testWeather)

        viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        // Act
        viewModel.fetchAllFavoriteWeather()
        advanceUntilIdle()

        // Assert
        viewModel.uiState.value.favorites.forEach { summary ->
            assertNull(summary.failureType)
        }
    }

    // -------------------------------------------------------------------------
    // onSuggestionSelected
    // -------------------------------------------------------------------------

    @Test
    fun `onSuggestionSelected正常系 - 登録成功→検索クリア＋閉じイベント`() = runTest {
        // Arrange
        val suggestion = PlaceSuggestion(
            placeId = "place_new",
            mainText = "京都タワー",
            secondaryText = "京都府京都市"
        )
        coEvery { addFavoriteUseCase(suggestion) } returns Result.success(3L)

        viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        // Act & Assert
        viewModel.searchCloseEvent.test {
            viewModel.onSuggestionSelected(suggestion)
            advanceUntilIdle()

            awaitItem()
            assertTrue(viewModel.uiState.value.searchSuggestions.isEmpty())
            assertFalse(viewModel.uiState.value.isAddingFavorite)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onSuggestionSelected異常系 - 登録失敗→Snackbar発火`() = runTest {
        // Arrange
        val suggestion = PlaceSuggestion(
            placeId = "place_dup",
            mainText = "東京タワー",
            secondaryText = "東京都港区"
        )
        coEvery { addFavoriteUseCase(suggestion) } returns Result.failure(
            IllegalStateException("すでに登録されています")
        )

        viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        // Act
        viewModel.onSuggestionSelected(suggestion)
        advanceUntilIdle()

        // Assert
        coVerify { globalUiManager.emitToast("すでに登録されています") }
        assertFalse(viewModel.uiState.value.isAddingFavorite)
    }

    // -------------------------------------------------------------------------
    // onDeleteFavorite
    // -------------------------------------------------------------------------

    @Test
    fun `onDeleteFavorite正常系 - 削除成功`() = runTest {
        // Arrange
        coEvery { deleteFavoriteUseCase(1L) } returns Result.success(Unit)
        viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        // Act
        viewModel.onDeleteFavorite(1L)
        advanceUntilIdle()

        // Assert
        coVerify { deleteFavoriteUseCase(1L) }
    }

    @Test
    fun `onDeleteFavorite異常系 - 削除失敗→Toast発火`() = runTest {
        // Arrange
        coEvery { deleteFavoriteUseCase(1L) } returns Result.failure(
            RuntimeException("削除に失敗しました")
        )
        viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        // Act
        viewModel.onDeleteFavorite(1L)
        advanceUntilIdle()

        // Assert
        coVerify { globalUiManager.emitToast("削除に失敗しました") }
    }

    // -------------------------------------------------------------------------
    // onSearchQueryChange
    // -------------------------------------------------------------------------

    @Test
    fun `onSearchQueryChange - デバウンス後にPlaces API検索が実行される`() = runTest {
        // Arrange
        val suggestions = listOf(
            PlaceSuggestion(placeId = "p1", mainText = "東京タワー", secondaryText = "東京都港区")
        )
        coEvery { searchPlaces("東京") } returns suggestions
        viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        // Act
        viewModel.onSearchQueryChange("東京")
        advanceTimeBy(350)
        advanceUntilIdle()

        // Assert
        coVerify { searchPlaces("東京") }
        assertEquals(suggestions, viewModel.uiState.value.searchSuggestions)
    }

    @Test
    fun `onSearchQueryChange - 空文字→候補クリア`() = runTest {
        // Arrange
        viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        // Act
        viewModel.onSearchQueryChange("")
        advanceUntilIdle()

        // Assert
        assertTrue(viewModel.uiState.value.searchSuggestions.isEmpty())
        coVerify(exactly = 0) { searchPlaces("") }
    }

    @Test
    fun `onSearchQueryChange - searchPlaces失敗→候補クリア＋failureTypeセット`() = runTest {
        // Arrange: IOException → API_NETWORK_ERROR
        coEvery { searchPlaces("東京") } throws java.io.IOException("ネットワークエラー")
        viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        // Act
        viewModel.onSearchQueryChange("東京")
        advanceTimeBy(350)
        advanceUntilIdle()

        // Assert
        assertTrue(viewModel.uiState.value.searchSuggestions.isEmpty())
        assertEquals(
            com.anri.weathercalendarapp.weather.presentation.type.PlaceFailureType.API_NETWORK_ERROR,
            viewModel.uiState.value.searchFailureType
        )
    }

    @Test
    fun `onSearchQueryChange - 検索成功時はfailureTypeがクリアされる`() = runTest {
        // Arrange: 失敗→成功 の順で呼ぶ
        coEvery { searchPlaces("東京") } throws java.io.IOException("ネットワークエラー")
        coEvery { searchPlaces("大阪") } returns listOf(
            PlaceSuggestion(placeId = "p1", mainText = "大阪", secondaryText = "大阪府")
        )
        viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        // Act 1: 失敗
        viewModel.onSearchQueryChange("東京")
        advanceTimeBy(350)
        advanceUntilIdle()
        assertEquals(
            com.anri.weathercalendarapp.weather.presentation.type.PlaceFailureType.API_NETWORK_ERROR,
            viewModel.uiState.value.searchFailureType
        )

        // Act 2: 成功でfailureTypeクリア
        viewModel.onSearchQueryChange("大阪")
        advanceTimeBy(350)
        advanceUntilIdle()

        // Assert
        assertNull(viewModel.uiState.value.searchFailureType)
        assertEquals(1, viewModel.uiState.value.searchSuggestions.size)
    }

    @Test
    fun `onSearchQueryChange - 空文字→failureTypeもクリアされる`() = runTest {
        // Arrange
        coEvery { searchPlaces("東京") } throws java.io.IOException("ネットワークエラー")
        viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        // Act 1: 失敗状態にする
        viewModel.onSearchQueryChange("東京")
        advanceTimeBy(350)
        advanceUntilIdle()
        assertEquals(
            com.anri.weathercalendarapp.weather.presentation.type.PlaceFailureType.API_NETWORK_ERROR,
            viewModel.uiState.value.searchFailureType
        )

        // Act 2: 空文字でクリア
        viewModel.onSearchQueryChange("")
        advanceUntilIdle()

        // Assert
        assertNull(viewModel.uiState.value.searchFailureType)
        assertTrue(viewModel.uiState.value.searchSuggestions.isEmpty())
    }
}
