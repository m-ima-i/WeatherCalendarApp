package com.anri.weathercalendarapp.common.view.dialog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.anri.weathercalendarapp.R

/**
 * 予定削除確認Dialog
 */
@Composable
fun DeleteEventConfirmDialog(
    eventTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AppDialog(
        icon = {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )
        },

        body = stringResource(R.string.dialog_delete_event_body, eventTitle),
        dismissLabel = stringResource(R.string.dialog_delete_event_dismiss),
        onDismiss = onDismiss,
        confirmLabel = stringResource(R.string.dialog_delete_event_confirm),
        onConfirm = onConfirm,
        confirmColor = MaterialTheme.colorScheme.error,
        onDismissRequest = onDismiss
    )
}
