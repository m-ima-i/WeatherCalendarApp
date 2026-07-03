package com.anri.weathercalendarapp

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.anri.weathercalendarapp.main.presentation.GlobalUiManager
import com.anri.weathercalendarapp.main.presentation.state.StartupStep
import com.anri.weathercalendarapp.main.presentation.viewmodel.MainViewModel
import com.anri.weathercalendarapp.main.view.AppScreen
import com.anri.weathercalendarapp.widget.domain.usecase.RefreshAllWidgetsUseCase
import javax.inject.Inject
import com.anri.weathercalendarapp.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    @Inject lateinit var globalUiManager: GlobalUiManager

    @Inject lateinit var refreshAllWidgetsUseCase: RefreshAllWidgetsUseCase

    // GPS Resolutionの結果ランチャー
    private val gpsResolutionLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        mainViewModel.onGpsResolutionResult(result.resultCode == Activity.RESULT_OK)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        // Local 天気の判定が終わるまでスプラッシュを保持
        splashScreen.setKeepOnScreenCondition {
            mainViewModel.uiState.value.startupStep == StartupStep.LOAD_LOCAL_WEATHER
        }
        super.onCreate(savedInstanceState)

        // Roomの内容を全Widgetに反映
        lifecycleScope.launch { refreshAllWidgetsUseCase() }

        // 起動プロセス開始
        mainViewModel.startApp()

        // GPS Resolution リクエスト監視
        lifecycleScope.launch {
            mainViewModel.requestGpsResolution.collect { intentSender ->
                gpsResolutionLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
        }

        enableEdgeToEdge()
        setContent {
            AppTheme {
                AppScreen(
                    mainViewModel = mainViewModel,
                    globalUiManager = globalUiManager
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // OnResume プロセス（startupStep に応じて分岐）
        mainViewModel.onAppResumed()
    }
}
