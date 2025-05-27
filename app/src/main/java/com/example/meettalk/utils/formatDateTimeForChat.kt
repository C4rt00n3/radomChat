package com.example.meettalk.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
fun formatDateTimeForChat(isoDateString: String): Pair<String, String> {
    val zonedDateTime = ZonedDateTime.parse(isoDateString)
        .withZoneSameInstant(ZoneId.systemDefault())

    val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM. dd", Locale.ENGLISH)
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)

    val formattedDate = zonedDateTime.format(dateFormatter)
    val formattedTime = zonedDateTime.format(timeFormatter)

    return formattedDate to formattedTime
}

