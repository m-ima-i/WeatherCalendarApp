package com.anri.weathercalendarapp.weather.local.datasourceimpl

import com.anri.weathercalendarapp.common.geocoder.Address
import com.anri.weathercalendarapp.weather.domain.model.response.Current
import com.anri.weathercalendarapp.weather.domain.model.response.Daily
import com.anri.weathercalendarapp.weather.domain.model.response.Hourly
import com.anri.weathercalendarapp.weather.domain.model.response.Temp
import com.anri.weathercalendarapp.weather.domain.model.response.Weather
import com.anri.weathercalendarapp.weather.domain.model.response.WeatherDescription
import com.anri.weathercalendarapp.weather.local.dao.WeatherDAO
import com.anri.weathercalendarapp.weather.local.entity.WeatherEntity
import com.google.gson.Gson
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WeatherLocalDataSourceImplTest {

    private val weatherDAO: WeatherDAO = mockk(relaxUnitFun = true)
    private val dataSource = WeatherLocalDataSourceImpl(weatherDAO)
    private val gson = Gson()

    private val testWeatherDescription = WeatherDescription(
        icon = "01d"
    )
    private val testWeather = Weather(
        timezone = "Asia/Tokyo",
        current = Current(
            temp = 20.0, feelsLike = 19.5,
            humidity = 60, windSpeed = 3.0, weather = listOf(testWeatherDescription)
        ),
        hourly = listOf(Hourly(dt = 1700000000L, temp = 20.0, pop = 0.1, weather = listOf(testWeatherDescription))),
        daily = listOf(Daily(dt = 1700000000L, temp = Temp(min = 15.0, max = 25.0), pop = 0.2, weather = listOf(testWeatherDescription)))
    )
    private val testAddress = Address(
        adminArea = "東京都",
        locality = "渋谷区",
        subLocality = "神南"
    )

    // --- getCachedWeather ---

    @Test
    fun `getCachedWeather - Roomにデータがある場合はWeatherを返す`() = runTest {
        val json = gson.toJson(testWeather)
        coEvery { weatherDAO.getWeather() } returns WeatherEntity(weatherJson = json)

        val result = dataSource.getCachedWeather()

        assertEquals(testWeather, result)
    }

    @Test
    fun `getCachedWeather - Roomにデータがない場合はnullを返す`() = runTest {
        coEvery { weatherDAO.getWeather() } returns null

        val result = dataSource.getCachedWeather()

        assertNull(result)
    }

    // --- getCachedAddress ---

    @Test
    fun `getCachedAddress - addressJsonがある場合はAddressを返す`() = runTest {
        val addressJson = gson.toJson(testAddress)
        coEvery { weatherDAO.getWeather() } returns WeatherEntity(
            weatherJson = gson.toJson(testWeather),
            addressJson = addressJson
        )

        val result = dataSource.getCachedAddress()

        assertEquals(testAddress, result)
    }

    @Test
    fun `getCachedAddress - addressJsonがnullの場合はnullを返す`() = runTest {
        coEvery { weatherDAO.getWeather() } returns WeatherEntity(
            weatherJson = gson.toJson(testWeather),
            addressJson = null
        )

        val result = dataSource.getCachedAddress()

        assertNull(result)
    }

    @Test
    fun `getCachedAddress - Roomにデータがない場合はnullを返す`() = runTest {
        coEvery { weatherDAO.getWeather() } returns null

        val result = dataSource.getCachedAddress()

        assertNull(result)
    }

    // --- saveWeather ---

    @Test
    fun `saveWeather - 天気と地名をJSON変換してRoomに保存する`() = runTest {
        val entitySlot = slot<WeatherEntity>()
        coEvery { weatherDAO.insertWeather(capture(entitySlot)) } returns Unit

        dataSource.saveWeather(testWeather, testAddress)

        coVerify(exactly = 1) { weatherDAO.insertWeather(any()) }
        assertEquals(gson.toJson(testWeather), entitySlot.captured.weatherJson)
        assertEquals(gson.toJson(testAddress), entitySlot.captured.addressJson)
    }

    @Test
    fun `saveWeather - 地名null + 既存addressJsonなしの場合はaddressJsonがnullで保存される`() = runTest {
        coEvery { weatherDAO.getWeather() } returns null
        val entitySlot = slot<WeatherEntity>()
        coEvery { weatherDAO.insertWeather(capture(entitySlot)) } returns Unit

        dataSource.saveWeather(testWeather, null)

        coVerify(exactly = 1) { weatherDAO.insertWeather(any()) }
        assertNull(entitySlot.captured.addressJson)
    }

    @Test
    fun `saveWeather - 地名null + 既存addressJsonありの場合は既存を保持する`() = runTest {
        val existingAddressJson = gson.toJson(testAddress)
        coEvery { weatherDAO.getWeather() } returns WeatherEntity(
            weatherJson = gson.toJson(testWeather),
            addressJson = existingAddressJson
        )
        val entitySlot = slot<WeatherEntity>()
        coEvery { weatherDAO.insertWeather(capture(entitySlot)) } returns Unit

        dataSource.saveWeather(testWeather, null)

        coVerify(exactly = 1) { weatherDAO.insertWeather(any()) }
        assertEquals(existingAddressJson, entitySlot.captured.addressJson)
    }
}
