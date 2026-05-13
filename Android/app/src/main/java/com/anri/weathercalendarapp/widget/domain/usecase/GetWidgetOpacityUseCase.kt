package com.anri.weathercalendarapp.widget.domain.usecase

import com.anri.weathercalendarapp.widget.domain.repository.WidgetRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** ウィジェット透明度をLocalから取得する */
class GetWidgetOpacityUseCase @Inject constructor(
    private val widgetRepository: WidgetRepository
) {
    operator fun invoke(): Flow<Int> = widgetRepository.getWidgetOpacity()
}
