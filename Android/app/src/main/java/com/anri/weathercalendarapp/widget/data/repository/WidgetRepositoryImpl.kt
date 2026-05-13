package com.anri.weathercalendarapp.widget.data.repository

import com.anri.weathercalendarapp.widget.data.datasource.WidgetLocalDataSource
import com.anri.weathercalendarapp.widget.domain.repository.WidgetRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WidgetRepositoryImpl @Inject constructor(
    private val widgetLocalDataSource: WidgetLocalDataSource
) : WidgetRepository {
    override fun getWidgetOpacity(): Flow<Int> = widgetLocalDataSource.getWidgetOpacity()
    override suspend fun setWidgetOpacity(opacity: Int) = widgetLocalDataSource.setWidgetOpacity(opacity)
}
