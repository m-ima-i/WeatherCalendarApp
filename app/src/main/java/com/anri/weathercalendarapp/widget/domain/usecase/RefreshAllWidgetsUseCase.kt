package com.anri.weathercalendarapp.widget.domain.usecase

import com.anri.weathercalendarapp.widget.data.datasource.WidgetUpdater
import javax.inject.Inject

/** Roomの内容を全Widgetに反映する（権限チェック・API呼び出しなし） */
class RefreshAllWidgetsUseCase @Inject constructor(
    private val widgetUpdater: WidgetUpdater
) {
    suspend operator fun invoke() {
        widgetUpdater.refreshWeatherWidgets()
    }
}
