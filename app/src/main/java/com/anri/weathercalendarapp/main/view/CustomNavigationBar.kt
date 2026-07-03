package com.anri.weathercalendarapp.main.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.anri.weathercalendarapp.main.presentation.type.PageType
import com.anri.weathercalendarapp.main.presentation.type.RouteLayer

@Composable
fun CustomNavigationBar(
    layer: RouteLayer,
    pageType: PageType,
    onNavigate: (AppRoute) -> Unit,
) {
    val isMainLayer = layer == RouteLayer.MAIN

    val items = listOf(
        NavItem(
            route = AppRoute.Weather,
            iconType = NavIconType.SUN,
            label = AppRoute.Weather.LABEL,
            selected = isMainLayer && pageType == PageType.WEATHER,
            onClick = { onNavigate(AppRoute.Weather) }
        ),
        NavItem(
            route = AppRoute.MainScreen,
            iconType = NavIconType.HOME,
            label = AppRoute.MainScreen.LABEL,
            selected = isMainLayer && pageType == PageType.HOME,
            onClick = { onNavigate(AppRoute.MainScreen) }
        ),
        NavItem(
            route = AppRoute.Calendar,
            iconType = NavIconType.CALENDAR,
            label = AppRoute.Calendar.LABEL,
            selected = isMainLayer && pageType == PageType.CALENDAR,
            onClick = { onNavigate(AppRoute.Calendar) }
        ),
    )

    val selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer
    val unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = item.selected,
                onClick = item.onClick,
                icon = {
                    val tint = if (item.selected) selectedIconColor else unselectedIconColor
                    when (item.iconType) {
                        NavIconType.SUN -> Icon(
                            imageVector = if (item.selected) Icons.Filled.WbSunny else Icons.Outlined.WbSunny,
                            contentDescription = null,
                            tint = tint
                        )
                        NavIconType.HOME -> Icon(
                            imageVector = if (item.selected) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = null,
                            tint = tint
                        )
                        NavIconType.CALENDAR -> Icon(
                            imageVector = if (item.selected) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            tint = tint
                        )
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = selectedIconColor,
                    selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    unselectedIconColor = unselectedIconColor,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}
