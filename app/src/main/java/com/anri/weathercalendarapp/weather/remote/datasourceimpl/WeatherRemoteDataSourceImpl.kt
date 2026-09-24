package com.anri.weathercalendarapp.weather.remote.datasourceimpl

import com.anri.weathercalendarapp.common.AppInfo
import com.anri.weathercalendarapp.weather.data.datasource.WeatherRemoteDataSource
import com.anri.weathercalendarapp.weather.domain.model.request.WeatherReq
import com.anri.weathercalendarapp.weather.domain.model.response.Weather
import com.anri.weathercalendarapp.weather.remote.apiinterface.WeatherApiService
import com.anri.weathercalendarapp.weather.remote.model.request.WeatherReqRemote
import com.anri.weathercalendarapp.weather.remote.model.request.toRemote
import com.anri.weathercalendarapp.weather.remote.model.response.DailyRemote
import com.anri.weathercalendarapp.weather.remote.model.response.HourlyRemote
import com.anri.weathercalendarapp.weather.remote.model.response.WeatherRemote
import com.anri.weathercalendarapp.weather.remote.model.response.toDomain
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class WeatherRemoteDataSourceImpl @Inject constructor(
    private val weatherApiService: WeatherApiService
) : WeatherRemoteDataSource {

    /**
     * One Call API 4.0 は用途別にエンドポイントが分かれているため、
     * current / 時間別 / 日別を並行して取得し1件のWeatherにまとめる。
     */
    override suspend fun getWeather(req: WeatherReq): Weather = coroutineScope {
        val request = req.toRemote()

        val currentDeferred = async { fetchCurrent(request) }
        val hourlyDeferred = async { fetchHourly(request) }
        val dailyDeferred = async { fetchDaily(request) }

        val currentResponse = currentDeferred.await()
        val current = currentResponse.data.firstOrNull()
            ?: throw IllegalStateException("current レスポンスのdataが空です")

        WeatherRemote(
            timezone = currentResponse.timezone,
            current = current,
            hourly = hourlyDeferred.await(),
            daily = dailyDeferred.await()
        ).toDomain()
    }

    private suspend fun fetchCurrent(request: WeatherReqRemote) =
        weatherApiService.getCurrentWeather(
            lat = request.lat,
            lon = request.lon,
            apiKey = AppInfo.ONE_CALL_API_KEY,
            units = request.units,
            lang = request.lang
        )

    /**
     * 時間別は1レスポンス最大20件だが、UIは24件表示するため2ページ目まで取得する。
     * 2ページ目の起点は1ページ目の最後のdtの次の時刻とし、時刻のずれによる
     * 重複・欠落が起きないようにする。
     */
    private suspend fun fetchHourly(request: WeatherReqRemote): List<HourlyRemote> {
        val firstPage = weatherApiService.getHourlyTimeline(
            lat = request.lat,
            lon = request.lon,
            apiKey = AppInfo.ONE_CALL_API_KEY,
            units = request.units,
            lang = request.lang
        )

        val lastDt = firstPage.data.lastOrNull()?.dt
        val isEnough = firstPage.data.size >= REQUIRED_HOURLY_COUNT
        if (isEnough || lastDt == null || firstPage.next == null) {
            return firstPage.data
        }

        val secondPage = weatherApiService.getHourlyTimeline(
            lat = request.lat,
            lon = request.lon,
            apiKey = AppInfo.ONE_CALL_API_KEY,
            units = request.units,
            lang = request.lang,
            start = lastDt + HOURLY_STEP_SECONDS
        )

        return firstPage.data + secondPage.data
    }

    /** 日別は1レスポンス最大10件で、UIの8件表示を1コールで満たせる。 */
    private suspend fun fetchDaily(request: WeatherReqRemote): List<DailyRemote> =
        weatherApiService.getDailyTimeline(
            lat = request.lat,
            lon = request.lon,
            apiKey = AppInfo.ONE_CALL_API_KEY,
            units = request.units,
            lang = request.lang
        ).data

    private companion object {
        /** UI（HourlyWeatherRow）が表示する時間別の件数 */
        const val REQUIRED_HOURLY_COUNT = 24

        /** 時間別タイムラインの1ステップ（秒） */
        const val HOURLY_STEP_SECONDS = 3600L
    }

}
