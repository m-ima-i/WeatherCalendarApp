package com.anri.weathercalendarapp.main.view

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.anri.weathercalendarapp.calendar.presentation.viewmodel.CalendarViewModel
import com.anri.weathercalendarapp.calendar.ui.CalendarScreen
import com.anri.weathercalendarapp.main.presentation.AppMainState
import com.anri.weathercalendarapp.main.presentation.type.PageType
import com.anri.weathercalendarapp.weather.domain.usecase.GetUpcomingEventsWithWeatherUseCase
import com.anri.weathercalendarapp.weather.presentation.state.WeatherUiState
import com.anri.weathercalendarapp.weather.presentation.viewmodel.FavoriteListViewModel
import com.anri.weathercalendarapp.weather.ui.AppMainScreen
import com.anri.weathercalendarapp.weather.ui.FavoriteListScreen
import com.anri.weathercalendarapp.weather.ui.FavoriteSearchScreen
import com.anri.weathercalendarapp.weather.ui.FavoriteWeatherScreen
import com.anri.weathercalendarapp.weather.ui.WeatherScreen

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    appMainState: AppMainState,
    calendarViewModel: CalendarViewModel,
    favoriteListViewModel: FavoriteListViewModel,
    weatherUiState: WeatherUiState,
    onBack: () -> Unit,
    onRequestLogin: (onSuccess: (() -> Unit)?) -> Unit = {},
    onRefreshWeather: () -> Unit = {},
    onOpenLocationSettings: () -> Unit = {},
    onOpenGpsSettings: () -> Unit = {},
) {
    NavHost(
        navController = appMainState.navController,
        startDestination = AppRoute.MainScreen,
        modifier = modifier.fillMaxSize(),
        enterTransition = { fadeIn(tween(200)) },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(200)) },
        popExitTransition = { fadeOut(tween(200)) }
    ) {
        // メインPager: 天気(0) / ホーム(1) / カレンダー(2)
        composable<AppRoute.MainScreen> {
            BackHandler(onBack = onBack)

            val calendarUiState by calendarViewModel.uiState.collectAsState()
            val getUpcomingEventsWithWeatherUseCase =
                remember { GetUpcomingEventsWithWeatherUseCase() }
            // events==null は未取得（ローダー表示）。取得後のみ計算。
            val upcomingEvents = remember(calendarUiState.events, weatherUiState.weather?.daily) {
                calendarUiState.events?.let { events ->
                    getUpcomingEventsWithWeatherUseCase(
                        events = events,
                        daily = weatherUiState.weather?.daily
                    )
                }
            }

            HorizontalPager(
                state = appMainState.pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 2
            ) { page ->
                when (PageType.entries[page]) {
                    PageType.WEATHER -> {
                        WeatherScreen(
                            weatherUiState = weatherUiState,
                        )
                    }

                    PageType.HOME -> {
                        AppMainScreen(
                            weatherUiState = weatherUiState,
                            upcomingEvents = upcomingEvents,
                            calendarEvents = calendarUiState.events,
                            eventColors = calendarUiState.eventColors,
                            isCalendarInitialized = calendarUiState.isInitialized,
                            isCalendarAuthorized = calendarUiState.isAuthorized,
                            calendarFailureType = calendarUiState.failureType,
                            isCalendarLoading = calendarUiState.isLoading,
                            onRequestCalendarLogin = {
                                onRequestLogin { calendarViewModel.onReauthCompleted() }
                            },
                            onRefreshCalendar = calendarViewModel::runCalendarApiOnly,
                            onRefreshWeather = onRefreshWeather,
                            onOpenLocationSettings = onOpenLocationSettings,
                            onOpenGpsSettings = onOpenGpsSettings,
                            onEditEvent = calendarViewModel::onEditEvent,
                            onDeleteEvent = calendarViewModel::onDeleteEvent,
                        )
                    }

                    PageType.CALENDAR -> {
                        CalendarScreen(
                            viewModel = calendarViewModel,
                            onRequestLogin = {
                                onRequestLogin { calendarViewModel.onReauthCompleted() }
                            },
                        )
                    }
                }
            }
        }

        composable<AppRoute.FavoriteList> {
            BackHandler(onBack = onBack)
            FavoriteListScreen(
                viewModel = favoriteListViewModel,
                onNavigateToSearch = {
                    appMainState.navController.navigate(AppRoute.FavoriteSearch)
                },
                onNavigateToFavoriteWeather = { id, name, secondaryName, lat, lon ->
                    appMainState.navController.navigate(
                        AppRoute.FavoriteWeather(
                            favoriteId = id,
                            name = name,
                            secondaryName = secondaryName,
                            latitude = lat,
                            longitude = lon
                        )
                    )
                }
            )
        }

        composable<AppRoute.FavoriteSearch> {
            BackHandler(onBack = onBack)
            FavoriteSearchScreen(
                viewModel = favoriteListViewModel,
                onNavigateBack = { appMainState.navController.popBackStack() }
            )
        }

        composable<AppRoute.FavoriteWeather> { backStackEntry ->
            BackHandler(onBack = onBack)
            val route = backStackEntry.toRoute<AppRoute.FavoriteWeather>()
            FavoriteWeatherScreen(
                favoriteListViewModel = favoriteListViewModel,
                favoriteId = route.favoriteId,
            )
        }
    }
}
