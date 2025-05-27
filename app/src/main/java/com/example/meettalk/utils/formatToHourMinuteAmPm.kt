package com.example.meettalk.utils

import java.text.SimpleDateFormat
import java.util.*

fun formatToHourMinuteAmPm(dateString: String): String {
    val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    isoFormat.timeZone = TimeZone.getTimeZone("UTC") // importante, porque vem com 'Z'

    val date = isoFormat.parse(dateString)

    val outputFormat = SimpleDateFormat("hh:mma", Locale.getDefault())
    outputFormat.timeZone = TimeZone.getDefault() // opcional, depende do fuso desejado

    return outputFormat.format(date ?: Date()).lowercase(Locale.getDefault())
}