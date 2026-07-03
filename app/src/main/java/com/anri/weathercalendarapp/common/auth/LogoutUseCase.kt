package com.anri.weathercalendarapp.common.auth

import com.anri.weathercalendarapp.calendar.domain.repository.CalendarRepository
import javax.inject.Inject

/**
 * ログアウト処理。
 * Google認可を取り消し、トークンキャッシュをクリアし、ローカルのカレンダー予定を削除し、
 * accountEmail を削除する。
 */
class LogoutUseCase @Inject constructor(
    private val authPreferences: AuthPreferences,
    private val googleAuthTokenProvider: GoogleAuthTokenProvider,
    private val calendarRepository: CalendarRepository
) {
    suspend operator fun invoke() {
        googleAuthTokenProvider.revokeAuthorization()
        googleAuthTokenProvider.clearCachedToken()
        // accountEmail を先にクリアしてから予定削除する。
        // deleteAllLocalEvents() 内で refreshCalendarWidget() が呼ばれた際に
        // accountEmail==null を見て Widget が NotAuthorized 表示に切り替わるようにするため。
        authPreferences.logout()
        calendarRepository.deleteAllLocalEvents()
    }
}
