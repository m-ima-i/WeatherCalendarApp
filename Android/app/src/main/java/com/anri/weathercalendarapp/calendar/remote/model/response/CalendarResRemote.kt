package com.anri.weathercalendarapp.calendar.remote.model.response

import com.google.gson.annotations.SerializedName

data class CalendarResRemote(
    @SerializedName("items")
    val items: List<EventRemote>?,
    @SerializedName("nextPageToken")
    val nextPageToken: String?
)

data class EventRemote(
    @SerializedName("id")
    val id: String?,
    @SerializedName("summary")
    val summary: String?,
    @SerializedName("start")
    val start: EventDateTime?,
    @SerializedName("end")
    val end: EventDateTime?,
    @SerializedName("colorId")
    val colorId: String?
)

data class EventDateTime(
    @SerializedName("dateTime")
    val dateTime: String?,
    @SerializedName("date")
    val date: String?
)
