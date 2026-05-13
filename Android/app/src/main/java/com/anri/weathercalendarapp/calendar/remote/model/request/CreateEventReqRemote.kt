package com.anri.weathercalendarapp.calendar.remote.model.request

import com.google.gson.annotations.SerializedName

data class CreateEventReqRemote(
    @SerializedName("summary")
    val summary: String? = null,
    @SerializedName("start")
    val start: EventDateTimeRemote,
    @SerializedName("end")
    val end: EventDateTimeRemote,
    @SerializedName("colorId")
    val colorId: String? = null
)

data class EventDateTimeRemote(
    @SerializedName("dateTime")
    val dateTime: String? = null,
    @SerializedName("date")
    val date: String? = null,
    @SerializedName("timeZone")
    val timeZone: String? = null
)
