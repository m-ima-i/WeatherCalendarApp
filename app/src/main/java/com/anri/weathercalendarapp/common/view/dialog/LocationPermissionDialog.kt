package com.anri.weathercalendarapp.common.view.dialog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.anri.weathercalendarapp.R

/**
 * 位置情報権限 Dialog — 初回チェック時のみ表示。
 * 「閉じる」 → onClose（天気APIプロセス終了）、「設定」 → onSettings（アプリ権限詳細画面遷移）。
 */
@Composable
fun LocationPermissionDialog(
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
        body = stringResource(R.string.dialog_location_permission_body),
        confirmLabel = stringResource(R.string.dialog_location_permission_confirm),
        dismissLabel = stringResource(R.string.dialog_location_permission_close),
        onConfirm = onSettings,
        onDismiss = onClose,
        onDismissRequest = onClose
    )
}
