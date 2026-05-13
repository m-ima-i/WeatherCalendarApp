package com.anri.weathercalendarapp.weather.domain.usecase

import com.anri.weathercalendarapp.common.Resource
import com.anri.weathercalendarapp.common.location.Location
import com.anri.weathercalendarapp.weather.domain.model.request.WeatherReq
import com.anri.weathercalendarapp.weather.domain.model.response.WeatherWithAddress
import com.anri.weathercalendarapp.weather.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** 位置情報からWeatherReqを生成し、天気APIの取得→Room保存をストリームで返す */
class UpdateWeatherUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository
) {
    operator fun invoke(location: Location): Flow<Resource<WeatherWithAddress>> {
        val req = WeatherReq(
            lat = location.latitude,
            lon = location.longitude
        )
        return weatherRepository.updateWeather(req)
    }
}
