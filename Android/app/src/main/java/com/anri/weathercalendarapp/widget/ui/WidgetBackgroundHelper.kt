package com.anri.weathercalendarapp.widget.ui

import android.content.Context
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.glance.color.ColorProvider
import com.anri.weathercalendarapp.ui.theme.onSurfaceVariantDark
import com.anri.weathercalendarapp.ui.theme.onSurfaceVariantLight
import com.anri.weathercalendarapp.ui.theme.primaryDark
import com.anri.weathercalendarapp.ui.theme.primaryLight
import androidx.glance.unit.ColorProvider as ColorProviderType

object WidgetBackgroundHelper {

    fun createBackgroundColorProvider(context: Context, opacityPercent: Int): ColorProviderType {
        val alpha = opacityPercent.coerceIn(0, 100) / 100f
        val lightBg = dynamicLightColorScheme(context).background.copy(alpha = alpha)
        val darkBg = dynamicDarkColorScheme(context).background.copy(alpha = alpha)
        return ColorProvider(lightBg, darkBg)
    }

    /** アプリのカスタムテーマと同じprimary色（TopBarと同一） */
    fun createPrimaryColorProvider(): ColorProviderType {
        return ColorProvider(primaryLight, primaryDark)
    }

    /** Primaryより重要度が低い色（予定の日付・時間表示用） */
    fun createOnSurfaceVariantColorProvider(): ColorProviderType {
        return ColorProvider(onSurfaceVariantLight, onSurfaceVariantDark)
    }
}
