package com.anri.weathercalendarapp.weather.data.repositoryimpl

import app.cash.turbine.test
import com.anri.weathercalendarapp.weather.data.datasource.FavoriteLocationLocalDataSource
import com.anri.weathercalendarapp.weather.data.datasource.WeatherRemoteDataSource
import com.anri.weathercalendarapp.weather.domain.model.request.WeatherReq
import com.anri.weathercalendarapp.weather.domain.model.response.Current
import com.anri.weathercalendarapp.weather.domain.model.response.Daily
import com.anri.weathercalendarapp.weather.domain.model.response.FavoriteLocation
import com.anri.weathercalendarapp.weather.domain.model.response.Hourly
import com.anri.weathercalendarapp.weather.domain.model.response.Temp
import com.anri.weathercalendarapp.weather.domain.model.response.Weather
import com.anri.weathercalendarapp.weather.domain.model.response.WeatherDescription
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class FavoriteLocationRepositoryImplTest {

    private val favoriteLocalDataSource: FavoriteLocationLocalDataSource = mockk(relaxUnitFun = true)
    private val weatherRemoteDataSource: WeatherRemoteDataSource = mockk()

    private lateinit var repository: FavoriteLocationRepositoryImpl

    private val testWeatherDescription = WeatherDescription(
        icon = "01d"
    )

    private val testWeather = Weather(
        timezone = "Asia/Tokyo",
        current = Current(
            temp = 20.0,
            feelsLike = 19.5,
            humidity = 60,
            windSpeed = 3.0,
            weather = listOf(testWeatherDescription)
        ),
        hourly = listOf(
            Hourly(
                dt = 1700000000L,
                temp = 20.0,
                pop = 0.1,
                weather = listOf(testWeatherDescription)
            )
        ),
        daily = listOf(
            Daily(
                dt = 1700000000L,
                temp = Temp(min = 15.0, max = 25.0),
                pop = 0.2,
                weather = listOf(testWeatherDescription)
            )
        )
    )

    private val testFavorite = FavoriteLocation(
        id = 0,
        placeId = "ChIJ51cu8IcbXWARiRtXIothAS4",
        name = "東京",
        secondaryName = "日本",
        latitude = 35.6762,
        longitude = 139.6503,
        createdAt = 1700000000L
    )

    @Before
    fun setup() {
        repository = FavoriteLocationRepositoryImpl(favoriteLocalDataSource, weatherRemoteDataSource)
    }

    // ========== addFavorite ==========

    @Test
    fun `addFavorite - 正常系 - 未登録の場合成功Resultを返す`() = runTest {
        // Arrange
        coEvery { favoriteLocalDataSource.existsByPlaceId(testFavorite.placeId) } returns false
        coEvery { favoriteLocalDataSource.addFavorite(testFavorite) } returns 1L

        // Act
        val result = repository.addFavorite(testFavorite)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())
        coVerify(exactly = 1) { favoriteLocalDataSource.addFavorite(testFavorite) }
    }

    @Test
    fun `addFavorite - 異常系 - 登録済みの場合失敗Resultを返す`() = runTest {
        // Arrange
        coEvery { favoriteLocalDataSource.existsByPlaceId(testFavorite.placeId) } returns true

        // Act
        val result = repository.addFavorite(testFavorite)

        // Assert
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is IllegalStateException)
        assertEquals("登録済みの地域です", exception?.message)
        coVerify(exactly = 0) { favoriteLocalDataSource.addFavorite(any()) }
    }

    @Test
    fun `addFavorite - 異常系 - existsByPlaceIdで例外発生時に失敗Resultを返す`() = runTest {
        // Arrange
        coEvery { favoriteLocalDataSource.existsByPlaceId(testFavorite.placeId) } throws RuntimeException("DB Error")

        // Act
        val result = repository.addFavorite(testFavorite)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("DB Error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `addFavorite - 異常系 - addFavoriteで例外発生時に失敗Resultを返す`() = runTest {
        // Arrange
        coEvery { favoriteLocalDataSource.existsByPlaceId(testFavorite.placeId) } returns false
        coEvery { favoriteLocalDataSource.addFavorite(testFavorite) } throws RuntimeException("Insert Error")

        // Act
        val result = repository.addFavorite(testFavorite)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Insert Error", result.exceptionOrNull()?.message)
    }

    // ========== deleteFavorite ==========

    @Test
    fun `deleteFavorite - 正常系 - 成功Resultを返す`() = runTest {
        // Act
        val result = repository.deleteFavorite(1L)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { favoriteLocalDataSource.deleteFavorite(1L) }
    }

    @Test
    fun `deleteFavorite - 異常系 - 例外発生時に失敗Resultを返す`() = runTest {
        // Arrange
        coEvery { favoriteLocalDataSource.deleteFavorite(1L) } throws RuntimeException("Delete Error")

        // Act
        val result = repository.deleteFavorite(1L)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Delete Error", result.exceptionOrNull()?.message)
    }

    // ========== fetchWeatherForLocation ==========

    @Test
    fun `fetchWeatherForLocation - 正常系 - 成功Resultを返す`() = runTest {
        // Arrange
        val lat = 35.6762
        val lon = 139.6503
        coEvery { weatherRemoteDataSource.getWeather(WeatherReq(lat = lat, lon = lon)) } returns testWeather

        // Act
        val result = repository.fetchWeatherForLocation(lat, lon)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(testWeather, result.getOrNull())
    }

    @Test
    fun `fetchWeatherForLocation - 異常系 - 例外発生時に失敗Resultを返す`() = runTest {
        // Arrange
        val lat = 35.6762
        val lon = 139.6503
        coEvery {
            weatherRemoteDataSource.getWeather(WeatherReq(lat = lat, lon = lon))
        } throws RuntimeException("Network Error")

        // Act
        val result = repository.fetchWeatherForLocation(lat, lon)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Network Error", result.exceptionOrNull()?.message)
    }

    // ========== getFavoritesStream ==========

    @Test
    fun `getFavoritesStream - LocalDataSourceのFlowを返す`() = runTest {
        // Arrange
        val favorites = listOf(testFavorite)
        every { favoriteLocalDataSource.getFavoritesStream() } returns flowOf(favorites)

        // Act & Assert
        repository.getFavoritesStream().test {
            assertEquals(favorites, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getFavoritesStream - 空リストを返す場合`() = runTest {
        // Arrange
        every { favoriteLocalDataSource.getFavoritesStream() } returns flowOf(emptyList())

        // Act & Assert
        repository.getFavoritesStream().test {
            assertEquals(emptyList<FavoriteLocation>(), awaitItem())
            awaitComplete()
        }
    }
}
