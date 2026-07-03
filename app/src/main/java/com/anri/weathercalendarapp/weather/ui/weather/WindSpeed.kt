package com.anri.weathercalendarapp.weather.ui.weather

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anri.weathercalendarapp.R
import com.anri.weathercalendarapp.common.view.CustomElevateCared
import com.anri.weathercalendarapp.common.view.helper.formatDouble

@Composable
fun WindSpeed(
    modifier: Modifier = Modifier,
    windSpeed: Double
) {
    val feelsLikeStr = "${formatDouble(windSpeed)}m/s"

    CustomElevateCared(modifier = modifier.fillMaxHeight()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            Text(
                text = stringResource(R.string.weather_wind_speed),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.TopStart)
            )
            Text(
                text = feelsLikeStr,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 8.dp, end = 10.dp)
            )
        }
    }
}