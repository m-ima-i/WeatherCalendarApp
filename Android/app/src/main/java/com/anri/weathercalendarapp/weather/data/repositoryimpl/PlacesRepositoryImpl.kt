package com.anri.weathercalendarapp.weather.data.repositoryimpl

import com.anri.weathercalendarapp.weather.domain.model.response.PlaceSuggestion
import com.anri.weathercalendarapp.weather.domain.repository.PlacesRepository
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PlacesRepositoryImpl @Inject constructor(
    private val placesClient: PlacesClient
): PlacesRepository {

    private var token = AutocompleteSessionToken.newInstance()

    override suspend fun searchPlaces(query: String): List<PlaceSuggestion> {
        val request = FindAutocompletePredictionsRequest.builder()
            .setSessionToken(token)
            .setQuery(query)
            .setTypesFilter(listOf(
                PlaceTypes.ADMINISTRATIVE_AREA_LEVEL_1,
                PlaceTypes.LOCALITY,
                PlaceTypes.SUBLOCALITY
            ))
            .build()

        return try {
            val response = placesClient.findAutocompletePredictions(request).await()
            response.autocompletePredictions
                .map {
                    PlaceSuggestion(
                        placeId = it.placeId,
                        mainText = it.getPrimaryText(null).toString(),
                        secondaryText = it.getSecondaryText(null).toString()
                    )
                }
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getPlaceCoordinates(placeId: String): LatLng? {
        val placeFields = listOf(Place.Field.LOCATION)
        val request = FetchPlaceRequest.builder(placeId, placeFields)
            .setSessionToken(token)
            .build()

        return try {
            val response = placesClient.fetchPlace(request).await()
            val latLng = response.place.location

            token = AutocompleteSessionToken.newInstance()

            latLng
        } catch (e: Exception) {
            null
        }
    }
}