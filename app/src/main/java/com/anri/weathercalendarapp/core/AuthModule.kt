package com.anri.weathercalendarapp.core

import com.anri.weathercalendarapp.common.auth.GoogleAuthTokenProvider
import com.anri.weathercalendarapp.common.auth.GoogleAuthTokenProviderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    abstract fun bindGoogleAuthTokenProvider(
        impl: GoogleAuthTokenProviderImpl
    ): GoogleAuthTokenProvider
}
