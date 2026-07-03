package com.anri.weathercalendarapp.weather.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anri.weathercalendarapp.weather.domain.model.response.FavoriteLocationWeatherSummary
import com.anri.weathercalendarapp.weather.presentation.state.FavoriteListUiState
import com.anri.weathercalendarapp.weather.presentation.viewmodel.FavoriteListViewModel
import com.anri.weathercalendarapp.weather.ui.favorite.FavoriteLocationCard
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun FavoriteListScreen(
    viewModel: FavoriteListViewModel = hiltViewModel(),
    onNavigateToSearch: () -> Unit = {},
    onNavigateToFavoriteWeather: (Long, String, String, Double, Double) -> Unit = { _, _, _, _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FavoriteListContent(
        uiState = uiState,
        onDeleteFavorite = viewModel::onDeleteFavorite,
        onNavigateToSearch = onNavigateToSearch,
        onFavoriteClick = { summary ->
            val loc = summary.favoriteLocation
            onNavigateToFavoriteWeather(
                loc.id,
                loc.name,
                loc.secondaryName,
                loc.latitude,
                loc.longitude
            )
        },
        onRetry = viewModel::retryFavoriteWeather
    )
}

@Composable
private fun FavoriteListContent(
    uiState: FavoriteListUiState,
    onDeleteFavorite: (Long) -> Unit,
    onNavigateToSearch: () -> Unit,
    onFavoriteClick: (FavoriteLocationWeatherSummary) -> Unit,
    onRetry: (Long) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isAddingFavorite) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.favorites.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "お気に入りの地域がありません\n＋ボタンから追加してください",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 10.dp, start = 10.dp, end = 10.dp)
                    .padding(bottom = 80.dp)
            ) {
                items(
                    items = uiState.favorites,
                    key = { it.favoriteLocation.id }
                ) { summary ->
                    DraggableFavoriteItem(
                        summary = summary,
                        onDelete = { onDeleteFavorite(summary.favoriteLocation.id) },
                        onClick = { onFavoriteClick(summary) },
                        onRetry = { onRetry(summary.favoriteLocation.id) },
                        modifier = Modifier
                            .animateItem()
                            .padding(bottom = 10.dp)
                    )
                }
            }
        }

        // FAB（右下）— 検索画面へ遷移
        FloatingActionButton(
            onClick = onNavigateToSearch,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun DraggableFavoriteItem(
    summary: FavoriteLocationWeatherSummary,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val offsetAnimatable = remember { Animatable(0f) }
    var isDragging by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val scale by animateFloatAsState(
        targetValue = if (isDragging) 1.03f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "scale"
    )

    // 長押しタイムアウトを 200ms に短縮（システムデフォルト 500ms を上書き）
    val baseViewConfig = LocalViewConfiguration.current
    val shortLongPressViewConfig = remember(baseViewConfig) {
        object : ViewConfiguration by baseViewConfig {
            override val longPressTimeoutMillis: Long = 200L
        }
    }

    CompositionLocalProvider(LocalViewConfiguration provides shortLongPressViewConfig) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (isDragging) 8.dp else 0.dp,
                shape = MaterialTheme.shapes.medium
            )
    ) {
        // 背景（ゴミ箱アイコン — カード10%以上スワイプで表示）
        BoxWithConstraints(modifier = Modifier.matchParentSize()) {
            val totalWidth = constraints.maxWidth.toFloat()
            val fraction = if (totalWidth > 0f) abs(offsetAnimatable.value) / totalWidth else 0f

            if (fraction >= 0.1f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.error),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.padding(end = 24.dp)
                    )
                }
            }
        }

        // カード（長押し+ドラッグで左に移動）
        FavoriteLocationCard(
            summary = summary,
            onClick = onClick,
            onRetry = onRetry,
            modifier = Modifier
                .offset { IntOffset(offsetAnimatable.value.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isDragging = true
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val newOffset =
                                (offsetAnimatable.value + dragAmount.x).coerceAtMost(0f)
                            scope.launch { offsetAnimatable.snapTo(newOffset) }
                        },
                        onDragEnd = {
                            isDragging = false
                            val swipeFraction =
                                if (size.width > 0) abs(offsetAnimatable.value) / size.width.toFloat() else 0f
                            scope.launch {
                                if (swipeFraction >= 0.5f) {
                                    offsetAnimatable.animateTo(
                                        targetValue = -size.width.toFloat(),
                                        animationSpec = tween(200)
                                    )
                                    onDelete()
                                } else {
                                    offsetAnimatable.animateTo(0f, spring())
                                }
                            }
                        },
                        onDragCancel = {
                            isDragging = false
                            scope.launch {
                                offsetAnimatable.animateTo(0f, spring())
                            }
                        }
                    )
                }
        )
    }
    }
}
