package com.anri.weathercalendarapp.weather.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.anri.weathercalendarapp.common.view.CustomElevateCared
import com.anri.weathercalendarapp.common.view.helper.TemperatureFormatter
import com.anri.weathercalendarapp.common.view.helper.formatPop
import com.anri.weathercalendarapp.common.view.helper.toTimeStr
import com.anri.weathercalendarapp.weather.domain.model.response.Hourly
import com.anri.weathercalendarapp.weather.presentation.type.WeatherType
import com.anri.weathercalendarapp.weather.ui.graphics.WeatherAnimation

@Composable
fun HourlyWeatherRow(
    hourlyWeather: List<Hourly>?,
) {
    val currentEpoch = System.currentTimeMillis() / 1000
    val displayList = (hourlyWeather ?: emptyList())
        .filter { it.dt >= currentEpoch - 3600 } // 現在時刻の1時間前以降（現在の時間帯を含む）
        .take(24)

    CustomElevateCared(
        modifier = Modifier
            .height(120.dp)
            .fillMaxWidth()
            // 親HorizontalPagerへの水平スクロール伝播を防止
            .scrollable(
                state = rememberScrollState(),
                orientation = Orientation.Horizontal
            )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            LazyRow {
                itemsIndexed(displayList, key = { _, item -> item.dt }) { index, item ->
                    HourlyWeatherItem(
                        time = item.dt.toTimeStr(),
                        weatherType = WeatherType.fromId(item.weather.firstOrNull()?.icon),
                        temp = item.temp,
                        pop = item.pop,
                        isCurrentHour = index == 0
                    )
                }
            }
        }
    }
}

@Composable
private fun HourlyWeatherItem(
    time: String,
    weatherType: WeatherType?,
    temp: Double,
    pop: Double,
    isCurrentHour: Boolean
) {
    val tempStr = TemperatureFormatter.formatTemperatureValue(temp)
    val popStr = formatPop(pop)

    val bgModifier = if (isCurrentHour) {
        Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
    } else {
        Modifier
    }

    val contentColor = if (isCurrentHour) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(53.dp)
            .then(bgModifier)
            ,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isCurrentHour) "今" else time,
            style = MaterialTheme.typography.bodySmall,
            color = contentColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        if (weatherType != null) {
            WeatherAnimation(
                type = weatherType.toAnimationType(),
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = tempStr,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.WaterDrop,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = contentColor
            )
            Text(
                text = popStr,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor
            )
        }
    }
}
