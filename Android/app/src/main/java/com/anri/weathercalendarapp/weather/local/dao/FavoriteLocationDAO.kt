package com.anri.weathercalendarapp.weather.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.anri.weathercalendarapp.weather.local.entity.FavoriteLocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteLocationDAO {

    @Query("SELECT * FROM favorite_location_table ORDER BY createdAt ASC")
    fun getAllFavorites(): Flow<List<FavoriteLocationEntity>>

    @Query("SELECT * FROM favorite_location_table WHERE placeId = :placeId LIMIT 1")
    suspend fun getFavoriteByPlaceId(placeId: String): FavoriteLocationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(entity: FavoriteLocationEntity): Long

    @Query("DELETE FROM favorite_location_table WHERE id = :id")
    suspend fun deleteFavoriteById(id: Long)

    @Query("SELECT * FROM favorite_location_table WHERE id = :id LIMIT 1")
    fun getFavoriteById(id: Long): Flow<FavoriteLocationEntity?>
}
