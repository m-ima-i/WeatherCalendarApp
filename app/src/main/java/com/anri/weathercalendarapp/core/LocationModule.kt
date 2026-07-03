package com.anri.weathercalendarapp.core

import android.app.Application
import com.anri.weathercalendarapp.common.geocoder.AppGeocoder
import com.anri.weathercalendarapp.common.geocoder.AppGeocoderImpl
import com.anri.weathercalendarapp.common.location.LocationTracker
import com.anri.weathercalendarapp.common.location.LocationTrackerImpl
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocationModule {

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(app: Application): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(app)
    }

    @Provides
    @Singleton
    fun provideLocationTracker(
        fusedLocationProviderClient: FusedLocationProviderClient,
        application: Application
    ): LocationTracker {
        return LocationTrackerImpl(fusedLocationProviderClient, application)
    }

    @Provides
    @Singleton
    fun provideAppGeocoder(impl: AppGeocoderImpl): AppGeocoder = impl
}