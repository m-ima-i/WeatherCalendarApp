package com.anri.weathercalendarapp.widget.domain.usecase

import com.anri.weathercalendarapp.widget.data.datasource.WidgetUpdater
import javax.inject.Inject

/** Glance stateにopacityを同期し、全ウィジェットを再描画する */
class UpdateAllWidgetsUseCase @Inject constructor(
    private val widgetUpdater: WidgetUpdater
) {
    suspend operator fun invoke(opacity: Int) {
        widgetUpdater.updateAllWidgets(opacity)
    }
}
