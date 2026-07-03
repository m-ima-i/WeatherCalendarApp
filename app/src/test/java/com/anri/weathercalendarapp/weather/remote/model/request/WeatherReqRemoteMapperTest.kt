package com.anri.weathercalendarapp.weather.remote.model.request

import com.anri.weathercalendarapp.weather.domain.model.request.WeatherReq
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherReqRemoteMapperTest {

    @Test
    fun `WeatherReq_toRemote - 正常変換かつデフォルト値が設定される`() {
        // Arrange
        val req = WeatherReq(lat = 35.6762, lon = 139.6503)

        // Act
        val remote = req.toRemote()

        // Assert
        assertEquals(35.6762, remote.lat, 0.0001)
        assertEquals(139.6503, remote.lon, 0.0001)
        assertEquals("minutely,alerts", remote.exclude)
        assertEquals("metric", remote.units)
        assertEquals("ja", remote.lang)
    }
}
