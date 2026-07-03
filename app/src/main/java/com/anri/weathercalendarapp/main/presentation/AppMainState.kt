package com.anri.weathercalendarapp.main.presentation

import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.anri.weathercalendarapp.main.presentation.type.PageType
import com.anri.weathercalendarapp.main.presentation.type.RouteLayer
import com.anri.weathercalendarapp.main.view.AppRoute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Stable
class AppMainState(
    val navController: NavHostController,
    val pagerState: PagerState,
    val drawerState: DrawerState,
    private val coroutineScope: CoroutineScope,
) {
    // ── derived ─────────────────────────────────────

    /** 現在のレイヤー（MAIN: Pager上 / SUB: Pager外） */
    val currentLayer: RouteLayer
        get() = if (navController.currentDestination?.hasRoute<AppRoute.MainScreen>() == true)
            RouteLayer.MAIN else RouteLayer.SUB

    /** 現在のPagerページ種別 */
    val currentPageType: PageType
        get() = PageType.entries[pagerState.currentPage]

    // ── ナビゲーション ──────────────────────────────

    /** アプリ内ナビゲーション */
    fun navigate(route: AppRoute) {
        val page = PageType.fromRoute(route)
        if(page == null) {
            navController.navigate(route)
        } else {
            popToMainAndScrollTo(page.ordinal)
        }
    }

    private fun popToMainAndScrollTo(page: Int) {
        if (currentLayer != RouteLayer.MAIN) {
            navController.popBackStack(AppRoute.MainScreen, inclusive = false)
        }
        coroutineScope.launch { pagerState.animateScrollToPage(page) }
    }

    // ── ドロワー操作 ────────────────────────────────

    fun setSettingDrawerOpen(open: Boolean) {
        coroutineScope.launch {
            if (open) drawerState.open() else drawerState.close()
        }
    }

}

@Composable
fun rememberAppMainState(
    navController: NavHostController = rememberNavController(),
    pagerState: PagerState = rememberPagerState(initialPage = PageType.HOME.ordinal) { PageType.entries.size },
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
): AppMainState {
    return remember(navController, pagerState, drawerState, coroutineScope) {
        AppMainState(navController, pagerState, drawerState, coroutineScope)
    }
}
