package com.example.plana.data

import java.time.OffsetDateTime


data class EventItem(
    var eventName: String? = null,
    var startTime: OffsetDateTime,
    var endTime: OffsetDateTime,
    var eventLocation: String? = null,
)