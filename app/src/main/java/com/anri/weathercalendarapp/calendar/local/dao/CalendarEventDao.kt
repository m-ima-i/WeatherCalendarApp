package com.anri.weathercalendarapp.calendar.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.anri.weathercalendarapp.calendar.local.entity.CalendarEventEntity

@Dao
interface CalendarEventDao {

    /** 全件削除 */
    @Query("DELETE FROM calendar_events")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<CalendarEventEntity>)

    /** 全件削除 → 一括挿入（トランザクションで原子的に実行） */
    @Transaction
    suspend fun replaceAll(events: List<CalendarEventEntity>) {
        deleteAll()
        insertAll(events)
    }

    /** ウィジェット用: ローカルに保存された全予定を一度だけ取得 */
    @Query("SELECT * FROM calendar_events")
    suspend fun getAllOnce(): List<CalendarEventEntity>
}
