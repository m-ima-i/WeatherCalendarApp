package com.anri.weathercalendarapp.weather.remote.model.response

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * One Call API 4.0 の実レスポンス（2026-09-19に取得）と同じ構造のJSONが
 * DTOへ正しくパースされることを検証する。
 * 3.0からの差分（dataラッパー、currentのdataが配列、next/prev）を対象にしている。
 */
class WeatherResponseParseTest {

    private val gson = Gson()

    @Test
    fun `currentレスポンス - dataは1件の配列としてパースされる`() {
        // Arrange
        val json = """
            {
              "lat": 35.6812,
              "lon": 139.7671,
              "timezone": "Asia/Tokyo",
              "timezone_offset": 32400,
              "data": [
                {
                  "dt": 1789780076,
                  "sunrise": 1789763168,
                  "sunset": 1789807433,
                  "temp": 23.47,
                  "feels_like": 23.73,
                  "pressure": 1020,
                  "humidity": 71,
                  "dew_point": 17.91,
                  "uvi": 3.17,
                  "clouds": 100,
                  "visibility": 10000,
                  "wind_speed": 6.17,
                  "wind_deg": 360,
                  "weather": [
                    { "id": 804, "main": "Clouds", "description": "厚い雲", "icon": "04d" }
                  ]
                }
              ]
            }
        """.trimIndent()

        // Act
        val response = gson.fromJson(json, CurrentResponseRemote::class.java)

        // Assert
        assertEquals("Asia/Tokyo", response.timezone)
        assertEquals(1, response.data.size)

        val current = response.data[0]
        assertEquals(1789780076L, current.dt)
        assertEquals(23.47, current.temp, 0.0001)
        assertEquals(23.73, current.feelsLike, 0.0001)
        assertEquals(71, current.humidity)
        assertEquals(6.17, current.windSpeed, 0.0001)
        assertEquals(1, current.weather.size)
        assertEquals(804, current.weather[0].id)
        assertEquals("04d", current.weather[0].icon)
    }

    @Test
    fun `時間別レスポンス - dataとnextがパースされる`() {
        // Arrange
        val json = """
            {
              "lat": 35.6812,
              "lon": 139.7671,
              "timezone": "Asia/Tokyo",
              "timezone_offset": 32400,
              "data": [
                {
                  "dt": 1789779600,
                  "temp": 23.47,
                  "feels_like": 23.73,
                  "humidity": 71,
                  "wind_speed": 5.43,
                  "weather": [
                    { "id": 804, "main": "Clouds", "description": "厚い雲", "icon": "04d" }
                  ],
                  "pop": 0
                },
                {
                  "dt": 1789826400,
                  "temp": 23.13,
                  "feels_like": 23.74,
                  "humidity": 86,
                  "wind_speed": 4.97,
                  "weather": [
                    { "id": 500, "main": "Rain", "description": "小雨", "icon": "10n" }
                  ],
                  "pop": 0.49,
                  "rain": { "1h": 0.21 }
                }
              ],
              "prev": "http://api.openweathermap.org/data/4.0/onecall/timeline/1h?cnt=20&start=1789707600",
              "next": "http://api.openweathermap.org/data/4.0/onecall/timeline/1h?cnt=20&start=1789851600"
            }
        """.trimIndent()

        // Act
        val response = gson.fromJson(json, HourlyResponseRemote::class.java)

        // Assert
        assertEquals("Asia/Tokyo", response.timezone)
        assertEquals(2, response.data.size)
        assertEquals(1789779600L, response.data[0].dt)
        assertEquals(23.47, response.data[0].temp, 0.0001)
        assertEquals(0.0, response.data[0].pop, 0.0001)
        assertEquals(0.49, response.data[1].pop, 0.0001)
        assertEquals("10n", response.data[1].weather[0].icon)
        assertNotNull(response.next)
        assertNotNull(response.prev)
    }

    @Test
    fun `日別レスポンス - tempはmin_maxを持つオブジェクトとしてパースされる`() {
        // Arrange
        val json = """
            {
              "lat": 35.6812,
              "lon": 139.7671,
              "timezone": "Asia/Tokyo",
              "timezone_offset": 32400,
              "data": [
                {
                  "dt": 1789776000,
                  "sunrise": 1789763168,
                  "sunset": 1789807433,
                  "moon_phase": 0.25,
                  "temp": {
                    "day": 26.11,
                    "min": 21.69,
                    "max": 26.42,
                    "night": 21.97,
                    "eve": 24.04,
                    "morn": 21.8
                  },
                  "feels_like": {
                    "day": 26.11, "night": 21.97, "eve": 24.04, "morn": 21.8
                  },
                  "humidity": 63,
                  "wind_speed": 6.38,
                  "weather": [
                    { "id": 500, "main": "Rain", "description": "小雨", "icon": "10d" }
                  ],
                  "clouds": 100,
                  "pop": 0.24,
                  "rain": 0.46,
                  "uvi": 6.27
                }
              ],
              "next": "http://api.openweathermap.org/data/4.0/onecall/timeline/1day?cnt=10&start=1790640000"
            }
        """.trimIndent()

        // Act
        val response = gson.fromJson(json, DailyResponseRemote::class.java)

        // Assert
        assertEquals("Asia/Tokyo", response.timezone)
        assertEquals(1, response.data.size)

        val daily = response.data[0]
        assertEquals(1789776000L, daily.dt)
        assertEquals(21.69, daily.temp.min, 0.0001)
        assertEquals(26.42, daily.temp.max, 0.0001)
        assertEquals(0.24, daily.pop, 0.0001)
        assertEquals("10d", daily.weather[0].icon)
        assertNotNull(response.next)
        assertNull(response.prev)
    }

    @Test
    fun `日別レスポンス - 最終ページはnextを持たない`() {
        // Arrange
        val json = """
            {
              "lat": 35.6812,
              "lon": 139.7671,
              "timezone": "Asia/Tokyo",
              "timezone_offset": 32400,
              "data": []
            }
        """.trimIndent()

        // Act
        val response = gson.fromJson(json, DailyResponseRemote::class.java)

        // Assert
        assertEquals(0, response.data.size)
        assertNull(response.next)
        assertNull(response.prev)
    }
}
