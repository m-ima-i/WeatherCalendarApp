package com.anri.weathercalendarapp.weather.domain.repository

import com.anri.weathercalendarapp.weather.domain.model.response.FavoriteLocation
import com.anri.weathercalendarapp.weather.domain.model.response.Weather
import kotlinx.coroutines.flow.Flow

interface FavoriteLocationRepository {

    /** Roomに保存されたお気に入り地点の一覧をFlowで監視する */
    fun getFavoritesStream(): Flow<List<FavoriteLocation>>

    /** お気に入り地点をRoomに追加する（placeId重複時は失敗を返す） */
    suspend fun addFavorite(favorite: FavoriteLocation): Result<Long>

    /** 指定IDのお気に入り地点をRoomから削除する */
    suspend fun deleteFavorite(id: Long): Result<Unit>

    /** 指定緯度経度で天気APIを呼び出し、Weatherを取得する */
    suspend fun fetchWeatherForLocation(lat: Double, lon: Double): Result<Weather>

    /** 指定IDのお気に入り地点をRoomからFlowで監視する */
    fun getFavoriteByIdStream(id: Long): Flow<FavoriteLocation?>
}
