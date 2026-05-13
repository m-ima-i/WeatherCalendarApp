package com.anri.weathercalendarapp.calendar.data.datasource

import com.anri.weathercalendarapp.calendar.domain.model.response.CalendarEvent
import kotlinx.coroutines.flow.Flow

interface CalendarLocalDataSource {

    /** 全件削除 */
    suspend fun deleteAll()

    /** 全件削除 → 一括保存 */
    suspend fun replaceAll(events: List<CalendarEvent>)

    /** ウィジェット用: ローカルに保存された全予定を一度だけ取得（suspend 版） */
    suspend fun getAllOnce(): List<CalendarEvent>
}
