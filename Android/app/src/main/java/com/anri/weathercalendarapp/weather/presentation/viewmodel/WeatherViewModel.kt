package com.anri.weathercalendarapp.weather.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anri.weathercalendarapp.common.Resource
import com.anri.weathercalendarapp.common.auth.AuthPreferences
import com.anri.weathercalendarapp.common.geocoder.Address
import com.anri.weathercalendarapp.common.location.Location
import com.anri.weathercalendarapp.common.location.LocationTracker
import com.anri.weathercalendarapp.common.network.NetworkAvailabilityChecker
import com.anri.weathercalendarapp.weather.domain.model.response.WeatherWithAddress
import com.anri.weathercalendarapp.weather.domain.usecase.GetCachedAddressUseCase
import com.anri.weathercalendarapp.weather.domain.usecase.GetCachedWeatherUseCase
import com.anri.weathercalendarapp.weather.domain.usecase.UpdateWeatherUseCase
import com.anri.weathercalendarapp.weather.presentation.state.WeatherUiState
import com.anri.weathercalendarapp.weather.presentation.type.WeatherFailureType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val updateWeatherUseCase: UpdateWeatherUseCase,
    private val locationTracker: LocationTracker,
    private val getCachedWeatherUseCase: GetCachedWeatherUseCase,
    private val getCachedAddressUseCase: GetCachedAddressUseCase,
    private val authPreferences: AuthPreferences,
    private val networkAvailabilityChecker: NetworkAvailabilityChecker,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    /** 起動時: Localキャッシュ（天気＋地名）を取得してUI表示（メイン画面表示前に呼ぶ） */
    fun loadLocalWeather(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val cached = getCachedWeatherUseCase()
            val cachedAddress = getCachedAddressUseCase()
            _uiState.update {
                it.copy(
                    weather = cached ?: it.weather,
                    currentAddress = cachedAddress ?: Address()
                )
            }
            onComplete()
        }
    }

    /**
     * 天気APIプロセス（Figma準拠 + 3フラグ管理）。
     *
     * 分岐順:
     * 1. 位置情報権限チェック（locationEvaluated を true 化）
     *    - 権限OFF + 過去未評価 → onLocationPermissionRequired()
     *    - 権限OFF + 過去評価済 → 次の GPS チェックへ進む
     *    - 権限ON → locationEvaluated を消費して GPS チェックへ
     * 2. GPSチェック（gpsEvaluated を true 化）
     *    - GPS OFF + 過去未評価 → onGpsRequired()
     *    - GPS OFF + 過去評価済 → onComplete()（処理終了）
     *    - GPS ON → gpsEvaluated を消費して executeWeatherProcess()
     */
    fun runWeatherProcess(
        onLocationPermissionRequired: () -> Unit = {},
        onGpsRequired: () -> Unit = {},
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val hasPermission = locationTracker.hasLocationPermission()
            val gpsEnabled = locationTracker.isGpsEnabled()

            // === 位置情報権限分岐 ===
            val locationWasEvaluated = authPreferences.locationEvaluated.first()
            authPreferences.setLocationEvaluated()
            if (!hasPermission) {
                _uiState.update { it.copy(failureType = computePermissionFailureType(false, gpsEnabled)) }
                if (!locationWasEvaluated) {
                    onLocationPermissionRequired()
                    return@launch
                }
                // 既に過去 Dialog を出した: GPS チェックへ進む
            }

            // === GPS分岐 ===
            val gpsWasEvaluated = authPreferences.gpsEvaluated.first()
            authPreferences.setGpsEvaluated()
            if (!gpsEnabled) {
                _uiState.update { it.copy(failureType = computePermissionFailureType(hasPermission, false)) }
                if (gpsWasEvaluated) {
                    onComplete()
                } else {
                    onGpsRequired()
                }
                return@launch
            }

            // === GPS ON だが権限なし ===
            if (!hasPermission) {
                _uiState.update { it.copy(failureType = WeatherFailureType.LOCATION_PERMISSION_OFF) }
                onComplete()
                return@launch
            }

            // === 全条件満了 → API実行 ===
            executeWeatherProcess(onComplete)
        }
    }

    private suspend fun executeWeatherProcess(onComplete: () -> Unit) {
        executeWeatherApiCall()
        onComplete()
    }

    /**
     * OnResume用: 天気API実行のみ。
     * 位置情報/GPS の状態を最新化し、足りていない場合は failureType を更新（Dialog 非表示）。
     * 両方OKの場合は API を呼んで成功時に weather を更新する。
     */
    fun runWeatherApiOnly() {
        viewModelScope.launch {
            val hasPermission = locationTracker.hasLocationPermission()
            val gpsEnabled = locationTracker.isGpsEnabled()

            // 評価済フラグを消費（OnResume でも分岐評価したことを記録）
            authPreferences.setLocationEvaluated()
            authPreferences.setGpsEvaluated()

            if (!hasPermission || !gpsEnabled) {
                _uiState.update {
                    it.copy(
                        failureType = computePermissionFailureType(hasPermission, gpsEnabled),
                        isLoading = false
                    )
                }
                return@launch
            }

            executeWeatherApiCall()
        }
    }

    /**
     * 天気API実行の共通処理 (Process / ApiOnly 共通):
     * 機内モード判定 → Loading state → 現在地取得 → 天気/Geocoder API → state 反映。
     */
    private suspend fun executeWeatherApiCall() {
        if (networkAvailabilityChecker.isOfflineDueToAirplaneMode()) {
            _uiState.update { it.copy(isLoading = false, failureType = WeatherFailureType.API_NETWORK_ERROR) }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        val location = try {
            locationTracker.getCurrentLocation()
        } catch (_: Exception) {
            null
        }
        if (location == null) {
            _uiState.update { it.copy(isLoading = false, failureType = WeatherFailureType.LOCATION_FAILED) }
            return
        }

        when (val result = fetchWeatherResult(location)) {
            is WeatherFetchResult.Success -> {
                _uiState.update {
                    it.copy(
                        weather = result.data.weather,
                        currentAddress = result.data.address ?: it.currentAddress,
                        failureType = if (result.data.address == null) WeatherFailureType.GEOCODER else null,
                        isLoading = false
                    )
                }
            }
            is WeatherFetchResult.Error -> {
                _uiState.update {
                    it.copy(
                        failureType = result.failureType,
                        isLoading = false
                    )
                }
            }
        }
    }

    /** 位置情報権限/GPS の有無から失敗タイプを算出（両方OKの場合は呼び出し元で別処理） */
    private fun computePermissionFailureType(hasPermission: Boolean, gpsEnabled: Boolean): WeatherFailureType {
        return when {
            !hasPermission && !gpsEnabled -> WeatherFailureType.LOCATION_AND_GPS_OFF
            !hasPermission -> WeatherFailureType.LOCATION_PERMISSION_OFF
            else -> WeatherFailureType.GPS_OFF
        }
    }

    private suspend fun fetchWeatherResult(location: Location): WeatherFetchResult {
        var result: WeatherFetchResult = WeatherFetchResult.Error(WeatherFailureType.API_UNKNOWN)
        updateWeatherUseCase(location).collect { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { result = WeatherFetchResult.Success(it) }
                }
                is Resource.Error -> {
                    result = WeatherFetchResult.Error(WeatherFailureType.fromApiError(resource.cause))
                }
                is Resource.Loading -> {}
            }
        }
        return result
    }

    private sealed class WeatherFetchResult {
        data class Success(val data: WeatherWithAddress) : WeatherFetchResult()
        data class Error(val failureType: WeatherFailureType) : WeatherFetchResult()
    }
}
