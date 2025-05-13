package com.example.plana

import android.util.Log
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.Events
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.security.GeneralSecurityException
import java.util.Collections


object CalendarUtil {
    private const val APPLICATION_NAME = "Plana"
    private val JSON_FACTORY: JsonFactory = GsonFactory.getDefaultInstance()
    private val SCOPES: kotlin.collections.List<String> = Collections.singletonList(CalendarScopes.CALENDAR_READONLY)

    @Throws(IOException::class, GeneralSecurityException::class)
    fun getCalendarService(credential: GoogleAccountCredential?): Calendar {
        return Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
            .setApplicationName(APPLICATION_NAME)
            .build()
    }



    @Throws(IOException::class)
    fun listUpcomingEvents(calendarService: Calendar?): kotlin.collections.List<Event> {
        val now = DateTime(System.currentTimeMillis())
        val events: Events = calendarService!!.events().list("primary")
            .setMaxResults(10)
            .setTimeMin(now)
            .setOrderBy("startTime")
            .setSingleEvents(true)
            .execute()

        val items: kotlin.collections.List<Event> = events.items ?: emptyList()
        if (items.isEmpty()) {
            Log.d("CalendarApiHelper", "No upcoming events found.")
        } else {
            Log.d("CalendarApiHelper", "Upcoming events")
            for (event in items) {
                var start: DateTime? = event.start.dateTime
                if (start == null) {
                    start = event.start.date
                }
                Log.d("CalendarApiHelper", "${event.summary} (${start})")
            }

        }
        return items
    }
}