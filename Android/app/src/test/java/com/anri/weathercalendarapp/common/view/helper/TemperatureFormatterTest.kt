package com.anri.weathercalendarapp.common.view.helper

import org.junit.Assert.assertEquals
import org.junit.Test

class TemperatureFormatterTest {

    @Test
    fun `formatTemperature - 摂氏0度`() {
        val result = TemperatureFormatter.formatTemperature(0.0)
        assertEquals("0°C", result)
    }

    @Test
    fun `formatTemperature - 摂氏25度`() {
        val result = TemperatureFormatter.formatTemperature(25.0)
        assertEquals("25°C", result)
    }

    @Test
    fun `formatTemperature - 摂氏マイナス10度`() {
        val result = TemperatureFormatter.formatTemperature(-10.0)
        assertEquals("-10°C", result)
    }

    @Test
    fun `formatTemperatureValue - 度記号のみ`() {
        val result = TemperatureFormatter.formatTemperatureValue(25.0)
        assertEquals("25°", result)
    }

    @Test
    fun `formatTemperature - 小数点以下は四捨五入`() {
        val result = TemperatureFormatter.formatTemperature(25.6)
        assertEquals("26°C", result)
    }

    @Test
    fun `formatTemperature - 小数点以下は四捨五入（切り捨て）`() {
        val result = TemperatureFormatter.formatTemperature(25.4)
        assertEquals("25°C", result)
    }
}
