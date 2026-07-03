package com.anri.weathercalendarapp.widget.data.datasource

import kotlinx.coroutines.flow.Flow

interface WidgetLocalDataSource {
    /** ウィジェット透明度をDataStoreからFlowで取得する */
    fun getWidgetOpacity(): Flow<Int>

    /** ウィジェット透明度をDataStoreに保存する（0..100 にクランプ） */
    suspend fun setWidgetOpacity(opacity: Int)
}
