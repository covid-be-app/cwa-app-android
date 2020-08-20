package be.sciensano.coronalert.util

import org.joda.time.LocalDate
import org.joda.time.chrono.GJChronology
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

object DateUtil {

    private val format: DateTimeFormatter =
        DateTimeFormat.forPattern("yyyy-MM-dd").withChronology(GJChronology.getInstance())
            .withZoneUTC()

    fun parseServerDate(date: String): LocalDate {
        return format.parseLocalDate(date)
    }
}