package com.anri.weathercalendarapp.common.view.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * MD3 Basic Dialog — Figmaデザイン準拠
 *
 * - 角丸 28dp
 * - surfaceContainerHigh 背景
 * - elevation level 3
 * - Icon → Title → Body → Actions の順
 * - Actions: 右寄せ TextButton
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDialog(
    body: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    icon: @Composable (() -> Unit)? = null,
    title: String? = null,
    dismissLabel: String? = null,
    onDismiss: (() -> Unit)? = null,
    confirmColor: Color = MaterialTheme.colorScheme.primary
) {
    val hasIcon = icon != null

    BasicAlertDialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            shadowElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = if (hasIcon) Alignment.CenterHorizontally else Alignment.Start
            ) {
                // Icon
                if (icon != null) {
                    icon()
                    Spacer(
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Title
                if (title != null) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = if (hasIcon) TextAlign.Center else TextAlign.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                }

                // Body
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = if (hasIcon) TextAlign.Center else TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                )

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (dismissLabel != null && onDismiss != null) {
                        TextButton(onClick = onDismiss) {
                            Text(
                                text = dismissLabel,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    TextButton(onClick = onConfirm) {
                        Text(
                            text = confirmLabel,
                            color = confirmColor
                        )
                    }
                }
            }
        }
    }
}
