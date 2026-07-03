package com.anri.weathercalendarapp.widget.domain.repository

import kotlinx.coroutines.flow.Flow

interface WidgetRepository {
    /** ウィジェット透明度をDataStoreからFlowで取得する */
    fun getWidgetOpacity(): Flow<Int>

    /** ウィジェット透明度をDataStoreに保存する（0..100 にクランプ） */
    suspend fun setWidgetOpacity(opacity: Int)
}
