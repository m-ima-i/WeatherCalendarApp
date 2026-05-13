package com.anri.weathercalendarapp.common.geocoder

import android.content.Context
import android.location.Geocoder
import com.anri.weathercalendarapp.common.location.Location
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AppGeocoderImplTest {

    private val context: Context = mockk(relaxed = true)
    private val geocoder = AppGeocoderImpl(context)

    @Before
    fun setup() {
        mockkStatic(Geocoder::class)
        mockkConstructor(Geocoder::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `fetchAddress異常系 - Geocoderが非対応の場合は空のAddressを返す`() = runTest {
        // Arrange
        val location = Location(latitude = 35.6762, longitude = 139.6503)
        every { Geocoder.isPresent() } returns false

        // Act
        val result = geocoder.fetchAddress(location)

        // Assert
        assertTrue(result.isSuccess)
        val address = result.getOrNull()
        assertNotNull(address)
        assertNull(address?.adminArea)
        assertNull(address?.locality)
        assertNull(address?.subLocality)
        assertNull(address?.thoroughfare)
    }
}
