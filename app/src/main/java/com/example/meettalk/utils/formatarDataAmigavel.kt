package com.example.meettalk.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
fun formatarDataAmigavel(dataStr: String): String {
    val formatter = DateTimeFormatter.ISO_DATE_TIME
    val data = LocalDateTime.parse(dataStr, formatter)
    val hoje = LocalDate.now()
    val dataLocal = data.toLocalDate()

    val dias = ChronoUnit.DAYS.between(dataLocal, hoje).toInt()
    val meses = ChronoUnit.MONTHS.between(dataLocal, hoje).toInt()
    val anos = ChronoUnit.YEARS.between(dataLocal, hoje).toInt()

    return when {
        dataLocal.isEqual(hoje) -> data.format(DateTimeFormatter.ofPattern("HH:mm"))
        dataLocal.isEqual(hoje.minusDays(1)) -> "Ontem"
        dias in 2..6 -> data.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("pt", "BR")).replaceFirstChar { it.uppercase() }
        dias in 7..30 -> "$dias dias"
        meses in 1..11 -> "$meses mês" + if (meses > 1) "es" else ""
        else -> "$anos ano" + if (anos > 1) "s" else ""
    }
}
