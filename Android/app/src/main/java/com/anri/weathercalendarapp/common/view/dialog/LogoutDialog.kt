package com.anri.weathercalendarapp.common.view.dialog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.anri.weathercalendarapp.R

/**
 * ログアウト（連携解除）確認Dialog
 */
@Composable
fun LogoutDialog(
    email: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AppDialog(
        icon = {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Logout,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )
        },

        body = stringResource(R.string.dialog_logout_body, email),
        dismissLabel = stringResource(R.string.dialog_logout_dismiss),
        onDismiss = onDismiss,
        confirmLabel = stringResource(R.string.dialog_logout_confirm),
        onConfirm = onConfirm,
        confirmColor = MaterialTheme.colorScheme.error,
        onDismissRequest = onDismiss
    )
}
