package com.anri.weathercalendarapp.main.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import com.anri.weathercalendarapp.common.geocoder.Address
import com.anri.weathercalendarapp.main.presentation.type.PageType
import com.anri.weathercalendarapp.main.presentation.type.RouteLayer
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopAppBar(
    currentDestination: NavDestination?,
    layer: RouteLayer,
    pageType: PageType,
    currentAddress: Address,
    favoriteWeatherTitle: String = "",
    calendarOverlayDate: LocalDate? = null,
    onNavigate: (AppRoute) -> Unit,
    onOpenSetting: () -> Unit,
) {
    val isMainLayer = layer == RouteLayer.MAIN
    val isWeather = isMainLayer && pageType == PageType.WEATHER
    val isMainScreen = isMainLayer && pageType == PageType.HOME
    val isCalendar = isMainLayer && pageType == PageType.CALENDAR
    val isFavoriteWeather = currentDestination?.hasRoute<AppRoute.FavoriteWeather>() == true

    val isWeatherDomain = isMainScreen || isWeather
    val nonNullCurrentAddress = currentAddress.thoroughfare ?: currentAddress.subLocality ?: currentAddress.locality ?: currentAddress.subAdminArea ?: currentAddress.adminArea ?: ""
    val yearMonthFormatter = remember { DateTimeFormatter.ofPattern("yyyy年 M月", Locale.JAPANESE) }
    val title = when {
        isWeatherDomain -> nonNullCurrentAddress
        isCalendar && calendarOverlayDate != null -> calendarOverlayDate.format(yearMonthFormatter)
        isCalendar -> AppRoute.Calendar.TITLE
        isFavoriteWeather -> favoriteWeatherTitle
        else -> getScreenTitle(currentDestination)
    }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        actions = {
            if (isWeatherDomain || isFavoriteWeather) {
                IconButton(
                    onClick = { onNavigate(AppRoute.FavoriteList) },
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null
                    )
                }
            }
            IconButton(
                onClick = onOpenSetting,
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = null
                )
            }
        },
    )
}

@Composable
private fun getScreenTitle(destination: NavDestination?): String {
    return when {
        destination?.hasRoute<AppRoute.Calendar>() == true -> AppRoute.Calendar.TITLE
        destination?.hasRoute<AppRoute.FavoriteList>() == true -> AppRoute.FavoriteList.TITLE
        destination?.hasRoute<AppRoute.FavoriteSearch>() == true -> AppRoute.FavoriteSearch.TITLE
        else -> ""
    }
}
