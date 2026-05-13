package com.anri.weathercalendarapp.main.view

import kotlinx.serialization.Serializable

sealed interface AppRoute {
    @Serializable
    data object MainScreen : AppRoute {
        const val LABEL = "ホーム"
    }

    @Serializable
    data object Weather : AppRoute {
        const val LABEL = "天気"
    }

    @Serializable
    data object Calendar : AppRoute {
        const val LABEL = "カレンダー"
        const val TITLE = "カレンダー"
    }

    @Serializable
    data object FavoriteList : AppRoute {
        const val TITLE = "お気に入りリスト"
    }

    @Serializable
    data object FavoriteSearch : AppRoute {
        const val TITLE = "お気に入り追加"
    }

    @Serializable
    data class FavoriteWeather(
        val favoriteId: Long,
        val name: String,
        val secondaryName: String,
        val latitude: Double,
        val longitude: Double
    ) : AppRoute

}