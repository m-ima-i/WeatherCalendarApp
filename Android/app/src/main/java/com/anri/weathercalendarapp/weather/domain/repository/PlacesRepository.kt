package com.anri.weathercalendarapp.weather.domain.repository

import com.anri.weathercalendarapp.weather.domain.model.response.PlaceSuggestion
import com.google.android.gms.maps.model.LatLng

interface PlacesRepository {
    // 入力された値から地名を予測
    suspend fun searchPlaces(query: String): List<PlaceSuggestion>
    // 選択された地名IDから緯度経度を取得
    suspend fun getPlaceCoordinates(placeId: String): LatLng?
}