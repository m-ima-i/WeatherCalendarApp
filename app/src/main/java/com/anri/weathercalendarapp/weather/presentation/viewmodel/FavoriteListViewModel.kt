package com.anri.weathercalendarapp.weather.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anri.weathercalendarapp.main.presentation.GlobalUiManager
import com.anri.weathercalendarapp.weather.domain.model.response.FavoriteLocation
import com.anri.weathercalendarapp.weather.domain.model.response.FavoriteLocationWeatherSummary
import com.anri.weathercalendarapp.weather.domain.model.response.PlaceSuggestion
import com.anri.weathercalendarapp.weather.domain.model.response.Weather
import com.anri.weathercalendarapp.weather.domain.repository.FavoriteLocationRepository
import com.anri.weathercalendarapp.weather.domain.usecase.AddFavoriteUseCase
import com.anri.weathercalendarapp.weather.domain.usecase.DeleteFavoriteUseCase
import com.anri.weathercalendarapp.weather.domain.usecase.FetchFavoriteWeatherUseCase
import com.anri.weathercalendarapp.weather.domain.usecase.GetFavoritesStreamUseCase
import com.anri.weathercalendarapp.weather.domain.usecase.SearchPlaces
import com.anri.weathercalendarapp.weather.presentation.state.FavoriteListUiState
import com.anri.weathercalendarapp.weather.presentation.type.PlaceFailureType
import com.anri.weathercalendarapp.weather.presentation.type.WeatherFailureType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteListViewModel @Inject constructor(
    private val getFavoritesStreamUseCase: GetFavoritesStreamUseCase,
    private val addFavoriteUseCase: AddFavoriteUseCase,
    private val deleteFavoriteUseCase: DeleteFavoriteUseCase,
    private val fetchFavoriteWeatherUseCase: FetchFavoriteWeatherUseCase,
    private val searchPlaces: SearchPlaces,
    private val favoriteLocationRepository: FavoriteLocationRepository,
    private val globalUiManager: GlobalUiManager,
) : ViewModel() {

    private val _miscState = MutableStateFlow(FavoriteListUiState())
    private val _searchQuery = MutableStateFlow("")
    // お気に入りごとの天気データ（API直接反映）
    private val _weatherMap = MutableStateFlow<Map<Long, Weather>>(emptyMap())
    // 詳細画面が読み取る公開Flow（Figma 99:1520: リスト画面で取得済みの天気を表示するだけ）
    val weatherMap: StateFlow<Map<Long, Weather>> = _weatherMap
    // ローディング中のfavoriteId管理
    private val _loadingIds = MutableStateFlow<Set<Long>>(emptySet())
    // 失敗状態からのRefresh押下リトライ中のfavoriteId管理（回転アイコン表示用）
    private val _retryingIds = MutableStateFlow<Set<Long>>(emptySet())
    // お気に入りごとのAPI失敗種別（成功時/削除時にクリア）
    private val _failureMap = MutableStateFlow<Map<Long, WeatherFailureType>>(emptyMap())
    val failureMap: StateFlow<Map<Long, WeatherFailureType>> = _failureMap

    private val _searchCloseEvent = Channel<Unit>()
    val searchCloseEvent: Flow<Unit> = _searchCloseEvent.receiveAsFlow()

    private val loadingFlow: Flow<Pair<Set<Long>, Set<Long>>> =
        combine(_loadingIds, _retryingIds) { loading, retrying -> loading to retrying }

    val uiState: StateFlow<FavoriteListUiState> = combine(
        getFavoritesStreamUseCase(),
        _miscState,
        _weatherMap,
        loadingFlow,
        _failureMap
    ) { favorites, misc, weatherMap, (loadingIds, retryingIds), failureMap ->
        misc.copy(
            favorites = favorites.map { fav ->
                buildSummary(
                    fav = fav,
                    weather = weatherMap[fav.id],
                    isLoading = loadingIds.contains(fav.id),
                    isRetrying = retryingIds.contains(fav.id),
                    failureType = failureMap[fav.id]
                )
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FavoriteListUiState()
    )

    init {
        observeSearchQuery()
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchQuery() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isNotEmpty()) {
                        try {
                            val results = searchPlaces(query)
                            _miscState.update {
                                it.copy(
                                    searchSuggestions = results,
                                    searchFailureType = null
                                )
                            }
                        } catch (e: Exception) {
                            _miscState.update {
                                it.copy(
                                    searchSuggestions = emptyList(),
                                    searchFailureType = PlaceFailureType.fromApiError(e)
                                )
                            }
                        }
                    } else {
                        _miscState.update {
                            it.copy(
                                searchSuggestions = emptyList(),
                                searchFailureType = null
                            )
                        }
                    }
                }
        }
    }

    fun fetchAllFavoriteWeather() {
        viewModelScope.launch {
            val favorites = getFavoritesStreamUseCase().firstOrNull() ?: return@launch
            val jobs = favorites.map { fav -> fetchWeatherForFavorite(fav) }
            jobs.joinAll()
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        if (query.isEmpty()) {
            _miscState.update { it.copy(searchSuggestions = emptyList()) }
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _miscState.update {
            it.copy(searchSuggestions = emptyList(), searchFailureType = null)
        }
    }

    fun onSuggestionSelected(suggestion: PlaceSuggestion) {
        viewModelScope.launch {
            _miscState.update { it.copy(isAddingFavorite = true) }
            val result = addFavoriteUseCase(suggestion)
            result.onSuccess { newId ->
                _miscState.update {
                    it.copy(
                        isAddingFavorite = false,
                        searchSuggestions = emptyList()
                    )
                }
                _searchQuery.value = ""
                _searchCloseEvent.send(Unit)

                // 追加直後に天気を取得（Room Flowから座標を取得）
                favoriteLocationRepository.getFavoriteByIdStream(newId)
                    .firstOrNull()?.let { fav -> fetchWeatherForFavorite(fav) }
            }
            result.onFailure { e ->
                _miscState.update {
                    it.copy(
                        isAddingFavorite = false,
                    )
                }
                e.message?.let { globalUiManager.emitToast(it) }
            }
        }
    }

    fun onDeleteFavorite(id: Long) {
        viewModelScope.launch {
            val result = deleteFavoriteUseCase(id)
            result.onSuccess {
                _weatherMap.update { it - id }
                _failureMap.update { it - id }
            }
            result.onFailure { e ->
                e.message?.let { globalUiManager.emitToast(it) }
            }
        }
    }

    /** 失敗状態のお気に入り行から再取得を起動する（Refreshアイコン押下時） */
    fun retryFavoriteWeather(favoriteId: Long) {
        if (favoriteId in _retryingIds.value) return
        viewModelScope.launch {
            val favorites = getFavoritesStreamUseCase().firstOrNull() ?: return@launch
            val fav = favorites.find { it.id == favoriteId } ?: return@launch
            _retryingIds.update { it + favoriteId }
            try {
                fetchWeatherForFavorite(fav).join()
            } finally {
                _retryingIds.update { it - favoriteId }
            }
        }
    }

    private fun fetchWeatherForFavorite(fav: FavoriteLocation): Job {
        _loadingIds.update { it + fav.id }
        return viewModelScope.launch {
            val result = fetchFavoriteWeatherUseCase(fav.latitude, fav.longitude)
            result.onSuccess { weather ->
                _weatherMap.update { it + (fav.id to weather) }
                _failureMap.update { it - fav.id }
            }
            result.onFailure { e ->
                _failureMap.update { it + (fav.id to WeatherFailureType.fromApiError(e)) }
            }
            _loadingIds.update { it - fav.id }
        }
    }

    private fun buildSummary(
        fav: FavoriteLocation,
        weather: Weather?,
        isLoading: Boolean,
        isRetrying: Boolean,
        failureType: WeatherFailureType?
    ): FavoriteLocationWeatherSummary {
        return FavoriteLocationWeatherSummary(
            favoriteLocation = fav,
            currentTemp = weather?.current?.temp,
            maxTemp = weather?.daily?.firstOrNull()?.temp?.max,
            minTemp = weather?.daily?.firstOrNull()?.temp?.min,
            weatherIcon = weather?.current?.weather?.firstOrNull()?.icon,
            timezone = weather?.timezone,
            isLoading = isLoading,
            isRetrying = isRetrying,
            failureType = failureType
        )
    }
}
