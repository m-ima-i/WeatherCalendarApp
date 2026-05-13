package com.anri.weathercalendarapp.weather.ui.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anri.weathercalendarapp.common.view.CustomElevateCared
import com.anri.weathercalendarapp.common.view.helper.TemperatureFormatter
import com.anri.weathercalendarapp.common.view.helper.formatPop
import com.anri.weathercalendarapp.common.view.helper.toDayOfWeek
import com.anri.weathercalendarapp.weather.domain.model.response.Daily
import com.anri.weathercalendarapp.weather.presentation.type.WeatherType
import com.anri.weathercalendarapp.weather.ui.graphics.WeatherAnimation
import kotlin.math.roundToInt

@Composable
fun Daily(
    modifier: Modifier = Modifier,
    daily: List<Daily>?,
) {
    val dailyList = daily?.take(8)

    // 週全体のmin/maxを算出（温度バーの相対位置用、UI表示のroundToIntと一致させる）
    val weekMin = dailyList?.minOfOrNull { it.temp.min.roundToInt() }?.toDouble() ?: 0.0
    val weekMax = dailyList?.maxOfOrNull { it.temp.max.roundToInt() }?.toDouble() ?: 0.0

    CustomElevateCared(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "週間予報",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            dailyList?.forEach {
                DailyRow(
                    modifier = Modifier.height(40.dp),
                    timeStamp = it.dt,
                    weatherType = WeatherType.fromId(it.weather.firstOrNull()?.icon),
                    pop = it.pop,
                    maxTemp = it.temp.max,
                    minTemp = it.temp.min,
                    weekMin = weekMin,
                    weekMax = weekMax,
                )
            }
        }
    }
}

@Composable
private fun DailyRow(
    modifier: Modifier = Modifier,
    timeStamp: Long,
    weatherType: WeatherType?,
    pop: Double,
    maxTemp: Double,
    minTemp: Double,
    weekMin: Double,
    weekMax: Double,
) {
    val dayOfWeek = timeStamp.toDayOfWeek()
    val popStr = formatPop(pop)
    val maxTempStr = TemperatureFormatter.formatTemperatureValue(maxTemp)
    val minTempStr = TemperatureFormatter.formatTemperatureValue(minTemp)

    Row(
        modifier = modifier
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Text(
            text = dayOfWeek,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.width(34.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        if (weatherType != null) {
            WeatherAnimation(
                type = weatherType.toAnimationType(),
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.width(44.dp)
        ) {
            Icon(
                imageVector = Icons.Default.WaterDrop,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = popStr,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.End,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(30.dp)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = minTempStr,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.End,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(34.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))

        TemperatureBar(
            dayMin = minTemp,
            dayMax = maxTemp,
            weekMin = weekMin,
            weekMax = weekMax,
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
        )

        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = maxTempStr,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Start,
            modifier = Modifier.width(34.dp)
        )
    }
}

@Composable
private fun TemperatureBar(
    dayMin: Double,
    dayMax: Double,
    weekMin: Double,
    weekMax: Double,
    modifier: Modifier = Modifier
) {
    // UI表示のroundToIntと同じ丸め方で位置を算出
    val roundedDayMin = dayMin.roundToInt().toDouble()
    val roundedDayMax = dayMax.roundToInt().toDouble()
    val range = weekMax - weekMin
    if (range <= 0) return

    val startFraction = ((roundedDayMin - weekMin) / range).toFloat().coerceIn(0f, 1f)
    val endFraction = ((roundedDayMax - weekMin) / range).toFloat().coerceIn(0f, 1f)

    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val errorColor = MaterialTheme.colorScheme.error

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
        ) {
            if (startFraction > 0f) {
                Spacer(modifier = Modifier.weight(startFraction))
            }
            Box(
                modifier = Modifier
                    .weight((endFraction - startFraction).coerceAtLeast(0.01f))
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(primaryColor, tertiaryColor, errorColor)
                        )
                    )
            )
            if (1f - endFraction > 0f) {
                Spacer(modifier = Modifier.weight((1f - endFraction).coerceAtLeast(0.001f)))
            }
        }
    }
}
