package com.anri.weathercalendarapp.calendar.remote.apiinterface

import com.anri.weathercalendarapp.calendar.remote.model.request.CreateEventReqRemote
import com.anri.weathercalendarapp.calendar.remote.model.response.CalendarResRemote
import com.anri.weathercalendarapp.calendar.remote.model.response.EventRemote
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface CalendarApiService {

    @GET("calendar/v3/calendars/{calendarId}/events")
    suspend fun getEvents(
        @Header("Authorization") authHeader: String,
        @Path("calendarId") calendarId: String,
        @Query("timeMin") timeMin: String? = null,
        @Query("timeMax") timeMax: String? = null,
        @Query("singleEvents") singleEvents: Boolean,
        @Query("orderBy") orderBy: String? = null,
        @Query("maxResults") maxResults: Int? = null,
        @Query("pageToken") pageToken: String? = null
    ): CalendarResRemote

    @POST("calendar/v3/calendars/{calendarId}/events")
    suspend fun createEvent(
        @Header("Authorization") authHeader: String,
        @Path("calendarId") calendarId: String,
        @Body body: CreateEventReqRemote
    ): EventRemote

    @PUT("calendar/v3/calendars/{calendarId}/events/{eventId}")
    suspend fun updateEvent(
        @Header("Authorization") authHeader: String,
        @Path("calendarId") calendarId: String,
        @Path("eventId") eventId: String,
        @Body body: CreateEventReqRemote
    ): EventRemote

    @DELETE("calendar/v3/calendars/{calendarId}/events/{eventId}")
    suspend fun deleteEvent(
        @Header("Authorization") authHeader: String,
        @Path("calendarId") calendarId: String,
        @Path("eventId") eventId: String
    )
}