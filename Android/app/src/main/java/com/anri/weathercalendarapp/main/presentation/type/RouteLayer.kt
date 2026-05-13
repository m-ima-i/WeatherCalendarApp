package com.anri.weathercalendarapp.main.presentation.type

/**
 * ナビゲーションのレイヤー区分。
 * MAIN: HorizontalPager上のページ（Weather, Home, Calendar）
 * SUB: Pager外の画面（FavoriteList, FavoriteSearch, FavoriteWeather）
 */
enum class RouteLayer {
    MAIN,
    SUB,
}
