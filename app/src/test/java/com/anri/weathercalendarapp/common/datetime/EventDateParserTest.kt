package com.anri.weathercalendarapp.common.datetime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class EventDateParserTest {

    @Test
    fun `parseEventDate - null入力はnullを返す`() {
        assertNull(parseEventDate(null))
    }

    @Test
    fun `parseEventDate - ISO date形式（終日イベント）`() {
        val result = parseEventDate("2024-01-15")
        assertEquals(LocalDate.of(2024, 1, 15), result)
    }

    @Test
    fun `parseEventDate - ISO date-time形式 JSTオフセット`() {
        val result = parseEventDate("2024-01-15T10:00:00+09:00")
        assertEquals(LocalDate.of(2024, 1, 15), result)
    }

    @Test
    fun `parseEventDate - ISO date-time形式 UTCオフセット`() {
        val result = parseEventDate("2024-01-15T01:00:00+00:00")
        assertEquals(LocalDate.of(2024, 1, 15), result)
    }

    @Test
    fun `parseEventDate - タイムゾーン変換は行わずローカル日付部分を返す`() {
        val result = parseEventDate("2024-01-15T23:00:00+09:00")
        assertEquals(LocalDate.of(2024, 1, 15), result)
    }

    @Test
    fun `parseEventDate - 不正な文字列はnullを返す`() {
        assertNull(parseEventDate("not-a-date"))
    }

    @Test
    fun `parseEventDate - 空文字列はnullを返す`() {
        assertNull(parseEventDate(""))
    }
}
