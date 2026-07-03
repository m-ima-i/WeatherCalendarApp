package com.anri.weathercalendarapp.common.view.dialog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.anri.weathercalendarapp.R

/**
 * GPS Dialog — SettingsClient Resolution が利用不可な端末向けのフォールバック。
 * 「閉じる」 → onClose、「設定」 → onSettings（GPS 設定画面遷移）。
 */
@Composable
fun GpsDialog(
    onSettings: () -> Unit,
    onClose: () -> Unit
) {
    AppDialog(
        icon = {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )
        },
        body = stringResource(R.string.dialog_gps_body),
        confirmLabel = stringResource(R.string.dialog_gps_confirm),
        dismissLabel = stringResource(R.string.dialog_gps_close),
        onConfirm = onSettings,
        onDismiss = onClose,
        onDismissRequest = onClose
    )
}
