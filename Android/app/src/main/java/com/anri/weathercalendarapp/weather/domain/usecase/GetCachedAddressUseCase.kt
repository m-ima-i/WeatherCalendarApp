package com.anri.weathercalendarapp.weather.domain.usecase

import com.anri.weathercalendarapp.common.geocoder.Address
import com.anri.weathercalendarapp.weather.domain.repository.WeatherRepository
import javax.inject.Inject

/** Roomから地名キャッシュを取得する */
class GetCachedAddressUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository
) {
    suspend operator fun invoke(): Address? = weatherRepository.getCachedAddress()
}
