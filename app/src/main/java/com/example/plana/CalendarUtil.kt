package com.example.plana

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Events
import java.io.IOException
import java.security.GeneralSecurityException
import java.util.Collections
import com.example.plana.data.EventItem
import java.time.OffsetDateTime


object CalendarUtil {
    private const val APPLICATION_NAME = "Plana"
    private const val EVENT_NUMBER = 10
    private val JSON_FACTORY: JsonFactory = GsonFactory.getDefaultInstance()
    private val SCOPES: kotlin.collections.List<String> = Collections.singletonList(CalendarScopes.CALENDAR_READONLY)

    @Throws(IOException::class, GeneralSecurityException::class)
    fun getCalendarService(credential: GoogleAccountCredential?): Calendar {
        val transport = AndroidHttp.newCompatibleTransport()
        return Calendar.Builder(transport, JSON_FACTORY, credential)
            .setApplicationName(APPLICATION_NAME)
            .build()
    }



    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(IOException::class)
    fun listUpcomingEvents(calendarService: Calendar?): List<EventItem> {
        val now = DateTime(System.currentTimeMillis())

        val events: Events = calendarService!!.events().list("primary")
            .setMaxResults(EVENT_NUMBER)
            .setTimeMin(now)
            .setOrderBy("startTime")
            .setSingleEvents(true)
            .execute()

        val googleEvents = events.items ?: emptyList()

        if (googleEvents.isEmpty()) {
            Log.d("CalendarApiHelper", "No upcoming events found.")
        } else {
            Log.d("CalendarApiHelper", "Upcoming events")
            for (event in googleEvents) {
                val start = event.start.dateTime ?: event.start.date
                Log.d("CalendarApiHelper", "${event.summary} ($start)")
            }
        }

        // Map Google Events to your custom Event class
        return googleEvents.map { googleEvent ->
            val startTime = googleEvent.start.dateTime?.toStringRfc3339()
                ?: googleEvent.start.date?.toStringRfc3339()

            val endTime = googleEvent.end.dateTime?.toStringRfc3339()
                ?: googleEvent.end.date?.toStringRfc3339()

            EventItem(
                eventName = googleEvent.summary,
                startTime = OffsetDateTime.parse(startTime),
                endTime = OffsetDateTime.parse(endTime),
                eventLocation = googleEvent.location
            )
        }
    }
}