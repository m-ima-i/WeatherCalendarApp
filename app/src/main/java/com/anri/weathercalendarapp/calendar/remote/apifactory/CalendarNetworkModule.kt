package com.anri.weathercalendarapp.calendar.remote.apifactory

import com.anri.weathercalendarapp.calendar.remote.apiinterface.CalendarApiService
import com.anri.weathercalendarapp.core.ApiLoggingInterceptor
import com.anri.weathercalendarapp.core.CalendarRetrofit
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
object CalendarNetworkModule {

    private const val BASE_URL = "https://www.googleapis.com/"

    @CalendarRetrofit
    @Provides
    @Singleton
    fun provideCalendarOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .callTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(ApiLoggingInterceptor("Calendar"))
            .build()
    }

    @CalendarRetrofit
    @Provides
    @Singleton
    fun provideCalendarRetrofit(
        @CalendarRetrofit okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideCalendarApiService(
        @CalendarRetrofit retrofit: Retrofit
    ): CalendarApiService {
        return retrofit.create(CalendarApiService::class.java)
    }
}