package com.anri.weathercalendarapp.main.view

import android.app.Activity
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.anri.weathercalendarapp.calendar.presentation.viewmodel.CalendarViewModel
import com.anri.weathercalendarapp.common.location.GpsResolver
import com.anri.weathercalendarapp.common.view.dialog.GpsDialog
import com.anri.weathercalendarapp.common.view.dialog.LocationPermissionDialog
import com.anri.weathercalendarapp.main.presentation.GlobalUiManager
import com.anri.weathercalendarapp.main.presentation.ToastMessage
import com.anri.weathercalendarapp.main.presentation.event.AppScreenUiEvent
import com.anri.weathercalendarapp.main.presentation.event.OnResumeEvent
import com.anri.weathercalendarapp.main.presentation.event.StartupEvent
import com.anri.weathercalendarapp.main.presentation.rememberAppMainState
import com.anri.weathercalendarapp.main.presentation.type.PageType
import com.anri.weathercalendarapp.main.presentation.viewmodel.MainViewModel
import com.anri.weathercalendarapp.setting.ui.SettingScreen
import com.anri.weathercalendarapp.weather.presentation.viewmodel.FavoriteListViewModel
import com.anri.weathercalendarapp.weather.presentation.viewmodel.WeatherViewModel
import com.google.android.gms.auth.api.identity.Identity
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch

@EntryPoint
@InstallIn(SingletonComponent::class)
interface GpsResolverEntryPoint {
    fun gpsResolver(): GpsResolver
}

@Composable
fun AppScreen(
    mainViewModel: MainViewModel,
    globalUiManager: GlobalUiManager,
    weatherViewModel: WeatherViewModel = hiltViewModel(),
    calendarViewModel: CalendarViewModel = hiltViewModel(),
    favoriteListViewModel: FavoriteListViewModel = hiltViewModel()
) {
    val appMainState = rememberAppMainState()
    val coroutineScope = rememberCoroutineScope()

    val weatherUiState by weatherViewModel.uiState.collectAsState()
    val calendarUiState by calendarViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val gpsResolver = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            GpsResolverEntryPoint::class.java
        ).gpsResolver()
    }

    // 起動プロセス: カレンダーAPIプロセスを発火する関数（認証成功後のリトライにも使用）
    val runCalendarProcess: () -> Unit = {
        calendarViewModel.runCalendarProcess(
            onComplete = { mainViewModel.onCalendarProcessCompleted() }
        )
    }

    // Google Auth consent launcher
    val consentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val authResult = Identity.getAuthorizationClient(context)
                .getAuthorizationResultFromIntent(result.data)
            val token = authResult.accessToken
            val email = try {
                Identity.getSignInClient(context)
                    .getSignInCredentialFromIntent(result.data).id
            } catch (e: Exception) {
                null
            }
            if (token != null) {
                mainViewModel.onAuthSuccess(token, email)
            } else {
                mainViewModel.onAuthFailed()
            }
        } else {
            mainViewModel.onAuthFailed()
        }
    }

    // お気に入り天気画面のタイトル（derived）
    val navBackStackEntry by appMainState.navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val favoriteWeatherTitle = remember(navBackStackEntry) {
        try {
            if (currentDestination?.hasRoute<AppRoute.FavoriteWeather>() == true) {
                navBackStackEntry?.toRoute<AppRoute.FavoriteWeather>()?.name ?: ""
            } else ""
        } catch (_: Exception) {
            ""
        }
    }

    // runWeatherProcess のコールバック（リフレッシュバナーと startup の両方で共有）
    val onLocationPermissionRequired: () -> Unit = {
        globalUiManager.setLocationPermissionDialog(true)
    }
    val onGpsRequired: () -> Unit = {
        coroutineScope.launch {
            when (val result = gpsResolver.check()) {
                is GpsResolver.Result.Resolvable -> {
                    result.exception.resolution.intentSender.let { sender ->
                        mainViewModel.requestGpsResolution(sender)
                    }
                }
                GpsResolver.Result.NotResolvable -> {
                    globalUiManager.setGpsDialog(true)
                }
                GpsResolver.Result.AlreadyEnabled -> {
                    // 念のため: GPS が ON になっていたら再評価
                    mainViewModel.onGpsResolutionResult(true)
                }
            }
        }
    }

    // 失敗バナー: リフレッシュ用のラムダ
    val onRefreshWeather: () -> Unit = {
        weatherViewModel.runWeatherProcess(
            onLocationPermissionRequired = onLocationPermissionRequired,
            onGpsRequired = onGpsRequired
        )
    }

    // 失敗バナー: 設定画面遷移
    val onOpenLocationSettings: () -> Unit = {
        context.startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                android.net.Uri.fromParts("package", context.packageName, null)
            )
        )
    }
    val onOpenGpsSettings: () -> Unit = {
        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    }

    // 起動プロセス Event 処理
    LaunchedEffect(Unit) {
        mainViewModel.startupEvent.collect { event ->
            when (event) {
                is StartupEvent.LoadLocalWeather -> {
                    weatherViewModel.loadLocalWeather {
                        mainViewModel.onLocalWeatherLoaded()
                    }
                }
                is StartupEvent.RunWeatherProcess -> {
                    weatherViewModel.runWeatherProcess(
                        onLocationPermissionRequired = onLocationPermissionRequired,
                        onGpsRequired = onGpsRequired,
                        onComplete = { mainViewModel.onWeatherProcessCompleted() }
                    )
                    // お気に入り地域の天気を現在地天気と同タイミングで並列取得
                    favoriteListViewModel.fetchAllFavoriteWeather()
                }
                is StartupEvent.RunCalendarProcess -> {
                    runCalendarProcess()
                }
            }
        }
    }

    // Toast 発火
    LaunchedEffect(Unit) {
        globalUiManager.toastEvent.collect { msg ->
            val text = when (msg) {
                is ToastMessage.Resource -> context.getString(msg.resId)
                is ToastMessage.Plain -> msg.text
            }
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        }
    }

    // OnResume Event 処理（API実行のみ、Dialog/権限チェックなし）
    LaunchedEffect(Unit) {
        mainViewModel.onResumeEvent.collect { event ->
            when (event) {
                is OnResumeEvent.RunApis -> {
                    weatherViewModel.runWeatherApiOnly()
                    calendarViewModel.runCalendarApiOnly()
                    favoriteListViewModel.fetchAllFavoriteWeather()
                }
            }
        }
    }

    // 認証 UiEvent 処理
    LaunchedEffect(Unit) {
        mainViewModel.uiEvent.collect { event ->
            when (event) {
                is AppScreenUiEvent.LaunchConsent -> {
                    val intentSenderRequest =
                        IntentSenderRequest.Builder(event.pendingIntent).build()
                    consentLauncher.launch(intentSenderRequest)
                }

                is AppScreenUiEvent.AuthSucceeded -> {
                    appMainState.setSettingDrawerOpen(false)
                }
            }
        }
    }

    // ナビゲーションバーで CALENDAR から他ページへ遷移した時にカレンダーUIを初期化
    LaunchedEffect(appMainState, calendarViewModel) {
        var prevPage = appMainState.pagerState.settledPage
        snapshotFlow { appMainState.pagerState.settledPage }.collect { current ->
            if (prevPage == PageType.CALENDAR.ordinal && current != PageType.CALENDAR.ordinal) {
                calendarViewModel.resetCalendarUi()
            }
            prevPage = current
        }
    }

    // ナビゲーション（AppMainState に委譲）
    val onNavigate: (AppRoute) -> Unit = { route ->
        appMainState.navigate(route)
    }

    // 戻るボタン処理（各composableのBackHandlerから呼ばれる）
    val onBack: () -> Unit = {
        when {
            appMainState.drawerState.isOpen -> appMainState.setSettingDrawerOpen(false)
            appMainState.navController.previousBackStackEntry != null -> {
                if (currentDestination?.hasRoute<AppRoute.FavoriteList>() == true) {
                    appMainState.navController.popBackStack(AppRoute.MainScreen, inclusive = false)
                } else {
                    appMainState.navController.popBackStack()
                }
            }
            appMainState.currentPageType != PageType.HOME ->
                coroutineScope.launch { appMainState.pagerState.animateScrollToPage(PageType.HOME.ordinal) }
            else -> (context as? Activity)?.finishAffinity()
        }
    }

    // 設定ドロワー（右から開く）+ メインコンテンツ
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = appMainState.drawerState,
            gesturesEnabled = appMainState.drawerState.isOpen,
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    ModalDrawerSheet {
                        SettingScreen(
                            onLogout = {
                                calendarViewModel.onLogout()
                                appMainState.setSettingDrawerOpen(false)
                                // カレンダー画面にいた場合はホーム画面へ自動遷移
                                if (appMainState.currentPageType == PageType.CALENDAR) {
                                    coroutineScope.launch {
                                        appMainState.pagerState.animateScrollToPage(PageType.HOME.ordinal)
                                    }
                                }
                            },
                            onClose = { appMainState.setSettingDrawerOpen(false) },
                            onRequestLogin = {
                                mainViewModel.onRequestLogin(
                                    context,
                                    onSuccess = {
                                        calendarViewModel.onReauthCompleted()
                                    }
                                )
                            }
                        )
                    }
                }
            }
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Scaffold(
                    topBar = {
                        CustomTopAppBar(
                            currentDestination = currentDestination,
                            layer = appMainState.currentLayer,
                            pageType = appMainState.currentPageType,
                            currentAddress = weatherUiState.currentAddress,
                            favoriteWeatherTitle = favoriteWeatherTitle,
                            calendarOverlayDate = calendarUiState.overlayDate,
                            onNavigate = onNavigate,
                            onOpenSetting = {
                                focusManager.clearFocus()
                                appMainState.setSettingDrawerOpen(true)
                            },
                        )
                    },
                    bottomBar = {
                        CustomNavigationBar(
                            layer = appMainState.currentLayer,
                            pageType = appMainState.currentPageType,
                            onNavigate = onNavigate,
                        )
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                ) { innerPadding ->
                    AppNavigation(
                        modifier = Modifier.padding(innerPadding),
                        appMainState = appMainState,
                        calendarViewModel = calendarViewModel,
                        favoriteListViewModel = favoriteListViewModel,
                        weatherUiState = weatherUiState,
                        onBack = onBack,
                        onRequestLogin = { onSuccess ->
                            mainViewModel.onRequestLogin(context, onSuccess = onSuccess)
                        },
                        onRefreshWeather = onRefreshWeather,
                        onOpenLocationSettings = onOpenLocationSettings,
                        onOpenGpsSettings = onOpenGpsSettings,
                    )
                }
            }
        }
    }

    // 位置情報権限 Dialog（初回チェック時のみ表示）
    // 「閉じる」 → 天気APIプロセス終了（GPS チェックへ進まない / Figma 147:570）
    // 「設定」 → アプリ権限詳細画面へ遷移、天気APIプロセスは保留（OnResume で再評価）
    if (globalUiManager.showLocationPermissionDialog) {
        LocationPermissionDialog(
            onSettings = {
                globalUiManager.setLocationPermissionDialog(false)
                context.startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        android.net.Uri.fromParts("package", context.packageName, null)
                    )
                )
            },
            onClose = {
                globalUiManager.setLocationPermissionDialog(false)
                mainViewModel.onWeatherProcessCompleted()
            }
        )
    }

    // GPS Dialog（SettingsClient Resolution が利用不可な端末向けフォールバック）
    // 「閉じる」 → onWeatherProcessCompleted で天気APIプロセス終了
    // 「設定」 → GPS 設定画面へ遷移、天気APIプロセスは保留（OnResume で再評価）
    if (globalUiManager.showGpsDialog) {
        GpsDialog(
            onSettings = {
                globalUiManager.setGpsDialog(false)
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            },
            onClose = {
                globalUiManager.setGpsDialog(false)
                mainViewModel.onWeatherProcessCompleted()
            }
        )
    }

}
