package com.anri.weathercalendarapp.widget.domain.model

import com.anri.weathercalendarapp.calendar.domain.model.response.CalendarEvent

/** 中ウィジェットのカレンダー部分の3状態 */
sealed class WidgetCalendarState {

    /** Google 未連携（accountEmail なし） */
    data object NotAuthorized : WidgetCalendarState()

    /** 直近の予定なし */
    data object NoEvents : WidgetCalendarState()

    /** 直近の予定あり */
    data class HasEvents(val events: List<CalendarEvent>) : WidgetCalendarState()
}
