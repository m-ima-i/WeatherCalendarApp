package com.anri.weathercalendarapp.weather.data.datasource

import com.anri.weathercalendarapp.weather.domain.model.response.FavoriteLocation
import kotlinx.coroutines.flow.Flow

interface FavoriteLocationLocalDataSource {

    /** Roomに保存されたお気に入り地点の一覧をFlowで監視する */
    fun getFavoritesStream(): Flow<List<FavoriteLocation>>

    /** お気に入り地点をRoomに追加し、生成されたIDを返す */
    suspend fun addFavorite(favorite: FavoriteLocation): Long

    /** 指定IDのお気に入り地点をRoomから削除する */
    suspend fun deleteFavorite(id: Long)

    /** 指定placeIdのお気に入り地点がRoomに存在するか判定する */
    suspend fun existsByPlaceId(placeId: String): Boolean

    /** 指定IDのお気に入り地点をRoomからFlowで監視する */
    fun getFavoriteByIdStream(id: Long): Flow<FavoriteLocation?>
}
