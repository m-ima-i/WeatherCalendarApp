package com.anri.weathercalendarapp.setting.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.BeachAccess
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.anri.weathercalendarapp.R
import com.anri.weathercalendarapp.common.view.dialog.GoogleIcon
import com.anri.weathercalendarapp.common.view.dialog.LogoutDialog
import com.anri.weathercalendarapp.setting.presentation.state.SettingUiState
import com.anri.weathercalendarapp.setting.presentation.viewmodel.SettingUiEvent
import com.anri.weathercalendarapp.setting.presentation.viewmodel.SettingViewModel
import com.anri.weathercalendarapp.weather.presentation.type.WeatherAnimationType
import com.anri.weathercalendarapp.widget.ui.WeatherAnimationRenderer

@Composable
fun SettingScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingViewModel = hiltViewModel(),
    onLogout: () -> Unit = {},
    onClose: () -> Unit = {},
    onRequestLogin: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is SettingUiEvent.LogoutCompleted -> onLogout()
            }
        }
    }

    SettingContent(
        modifier = modifier,
        uiState = uiState,
        onGoogleLogin = onRequestLogin,
        onGoogleLogout = { showLogoutDialog = true },
        onClose = onClose,
        onWidgetOpacityChanged = viewModel::onWidgetOpacityChanged,
        onWidgetOpacityCommitted = {
            viewModel.onWidgetOpacityCommitted()
            onClose()
        },
        onShowHolidaysChanged = viewModel::onShowHolidaysChanged
    )

    if (showLogoutDialog) {
        LogoutDialog(
            email = uiState.accountEmail ?: "",
            onConfirm = {
                showLogoutDialog = false
                viewModel.onLogout()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }
}

@Composable
private fun SettingContent(
    modifier: Modifier = Modifier,
    uiState: SettingUiState,
    onGoogleLogin: () -> Unit,
    onGoogleLogout: () -> Unit,
    onClose: () -> Unit,
    onWidgetOpacityChanged: (Int) -> Unit = {},
    onWidgetOpacityCommitted: () -> Unit = {},
    onShowHolidaysChanged: (Boolean) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
        ) {
            GoogleAccountSection(
                isLoggedIn = uiState.isCalendarAuthorized,
                email = uiState.accountEmail,
                onLogin = onGoogleLogin,
                onLogout = onGoogleLogout
            )

            Spacer(modifier = Modifier.height(16.dp))

            CalendarSettingSection(
                showHolidays = uiState.showHolidays,
                onShowHolidaysChanged = onShowHolidaysChanged
            )

            Spacer(modifier = Modifier.height(16.dp))

            WidgetOpacitySection(
                opacity = uiState.widgetOpacity,
                onOpacityChanged = onWidgetOpacityChanged,
                onOpacityCommitted = onWidgetOpacityCommitted
            )
        }
    }
}

@Composable
private fun CalendarSettingSection(
    showHolidays: Boolean,
    onShowHolidaysChanged: (Boolean) -> Unit
) {
    SectionHeader(title = "カレンダー設定")
    Spacer(modifier = Modifier.height(8.dp))

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onShowHolidaysChanged(!showHolidays) }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.BeachAccess,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "祝日の表示",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Checkbox(
                checked = showHolidays,
                onCheckedChange = onShowHolidaysChanged
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WidgetOpacitySection(
    opacity: Int,
    onOpacityChanged: (Int) -> Unit,
    onOpacityCommitted: () -> Unit
) {
    SectionHeader(title = "ウィジェット")
    Spacer(modifier = Modifier.height(8.dp))

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.GridView,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "透明度を調節",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${opacity}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // スライダー（UI即時反映のみ、ウィジェットには同期ボタンで反映）
            Slider(
                value = opacity.toFloat(),
                onValueChange = { onOpacityChanged(it.toInt()) },
                valueRange = 0f..100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                ),
                track = { sliderState ->
                    SliderDefaults.Track(
                        sliderState = sliderState,
                        colors = SliderDefaults.colors(
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        ),
                        drawStopIndicator = null
                    )
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onOpacityCommitted,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Sync,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ウィジェットに反映",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = "ウィジェットプレビュー",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                SmallWidgetPreview(opacity = opacity)
            }
        }
    }
}

/**
 * 中ウィジェット(WeatherSmallWidget 2x2)のプレビュー
 * SmallWidgetContent のレイアウト・配色を Compose Material3 で再現（フォントサイズは160dp枠に合わせて調整、表示値は固定）
 * サイズは160dp正方形（実ウィジェット2x2セルに相当）
 */
@Composable
private fun SmallWidgetPreview(opacity: Int) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val wc = if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    val cs = MaterialTheme.colorScheme // アプリのカスタムテーマ色（TopBarと同一）

    // 天気画像をWeatherAnimationRendererで生成（実ウィジェットと同じ描画）
    val weatherBitmap = remember {
        WeatherAnimationRenderer.render(context, WeatherAnimationType.CLOUDY, 64)
    }

    Box(
        modifier = Modifier
            .size(160.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        // 背景レイヤー — WidgetBackgroundHelperと同じ色源
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(wc.background.copy(alpha = opacity / 100f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(text = "東京", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = cs.primary, maxLines = 1)

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Image(
                    bitmap = weatherBitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    contentScale = ContentScale.Fit
                )
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
                    Text(text = "24°", fontSize = 38.sp, color = cs.onSurface)
                }
            }

            Row(modifier = Modifier.fillMaxWidth().height(16.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "↑", fontSize = 11.sp, color = cs.error)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = "28°", fontSize = 11.sp, color = cs.onSurfaceVariant)
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "↓", fontSize = 11.sp, color = cs.primary)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = "18°", fontSize = 11.sp, color = cs.onSurfaceVariant)
                    }
                }
                Box(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painter = painterResource(id = R.drawable.ic_thermostat), contentDescription = null, modifier = Modifier.size(12.dp), tint = cs.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = "26°", fontSize = 11.sp, color = cs.onSurfaceVariant, maxLines = 1)
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painter = painterResource(id = R.drawable.ic_water_drop), contentDescription = null, modifier = Modifier.size(12.dp), tint = cs.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = "30%", fontSize = 11.sp, color = cs.onSurfaceVariant, maxLines = 1)
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painter = painterResource(id = R.drawable.ic_air), contentDescription = null, modifier = Modifier.size(12.dp), tint = cs.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = "12m/s", fontSize = 11.sp, color = cs.onSurfaceVariant, maxLines = 1)
                    }
                }
            }
        }
    }
}


@Composable
private fun GoogleAccountSection(
    isLoggedIn: Boolean,
    email: String?,
    onLogin: () -> Unit,
    onLogout: () -> Unit
) {
    SectionHeader(title = "Googleアカウント")
    Spacer(modifier = Modifier.height(8.dp))

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            if (isLoggedIn) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        GoogleIcon(modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Google連携済み",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = email ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                SettingItemDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onLogout)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.errorContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Google連携を解除",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onLogin)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                        contentAlignment = Alignment.Center
                    ) {
                        GoogleIcon(modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Googleでログイン",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "カレンダー連携にはログインが必要です",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp)
    )
}

@Composable
private fun SettingItemDivider() {
    HorizontalDivider(
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.13f)
    )
}


