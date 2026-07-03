package com.anri.weathercalendarapp.weather.remote.apifactory

import com.anri.weathercalendarapp.core.ApiLoggingInterceptor
import com.anri.weathercalendarapp.core.WeatherRetrofit
import com.anri.weathercalendarapp.weather.remote.apiinterface.WeatherApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WeatherNetworkModule {

    private const val BASE_URL = "https://api.openweathermap.org"

    @WeatherRetrofit
    @Provides
    @Singleton
    fun provideWeatherOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .callTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(ApiLoggingInterceptor("Weather"))
            .build()
    }

    @WeatherRetrofit
    @Provides
    @Singleton
    fun provideWeatherRetrofit(
        @WeatherRetrofit okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideWeatherApiService(
        @WeatherRetrofit retrofit: Retrofit
    ): WeatherApiService {
        return retrofit.create(WeatherApiService::class.java)
    }
}