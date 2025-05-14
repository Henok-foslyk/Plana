package com.example.plana.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import com.example.plana.data.EventItem
import java.time.LocalDate


class CalendarViewModel: ViewModel() {

    private val _eventsMap = mutableStateMapOf<LocalDate, List<EventItem>?>()
    var selectedDate: LocalDate? = null
    val eventsMap: Map<LocalDate, List<EventItem>?>
        get() = _eventsMap

    @RequiresApi(Build.VERSION_CODES.O)
    fun setEvents(events: List<EventItem>?) {
        _eventsMap.clear()
        _eventsMap.putAll(
            events!!.groupBy { it.startTime.toLocalDate() }
        )

    }

}