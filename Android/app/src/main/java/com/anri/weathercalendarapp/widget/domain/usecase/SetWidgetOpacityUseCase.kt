package com.anri.weathercalendarapp.widget.domain.usecase

import com.anri.weathercalendarapp.widget.domain.repository.WidgetRepository
import javax.inject.Inject

/** ウィジェット透明度をLocalに保存する */
class SetWidgetOpacityUseCase @Inject constructor(
    private val widgetRepository: WidgetRepository
) {
    suspend operator fun invoke(opacity: Int) = widgetRepository.setWidgetOpacity(opacity)
}
