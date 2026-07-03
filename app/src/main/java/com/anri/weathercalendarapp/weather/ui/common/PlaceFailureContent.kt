package com.anri.weathercalendarapp.weather.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anri.weathercalendarapp.R
import com.anri.weathercalendarapp.weather.presentation.type.PlaceFailureType

@Composable
fun PlaceFailureContent(
    failureType: PlaceFailureType?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = failureMessage(failureType),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun failureMessage(failureType: PlaceFailureType?): String {
    val resId = when (failureType) {
        PlaceFailureType.API_UNAUTHORIZED -> R.string.place_failure_api_unauthorized
        PlaceFailureType.API_QUOTA_EXCEEDED -> R.string.place_failure_api_quota_exceeded
        PlaceFailureType.API_NETWORK_ERROR -> R.string.place_failure_api_network_error
        PlaceFailureType.API_SERVER_ERROR -> R.string.place_failure_api_server_error
        PlaceFailureType.API_UNKNOWN, null -> R.string.place_failure_api_unknown
    }
    return stringResource(resId)
}
