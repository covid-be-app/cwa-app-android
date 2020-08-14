package be.sciensano.coronalert.util

import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import org.joda.time.LocalDate
import org.joda.time.chrono.GJChronology
import org.joda.time.format.DateTimeFormat
import java.util.Date

@Suppress("MagicNumber")
object TemporaryExposureKeyExtensions {

    fun List<TemporaryExposureKey>.inT0T3Range(t0: String, t3: String) = run {
        val format =
            DateTimeFormat.forPattern("yyyy-MM-dd").withChronology(GJChronology.getInstance())
                .withZoneUTC()

        val t0Date = format.parseLocalDate(t0)
        val t3Date = format.parseLocalDate(t3)

        this.filter { key -> !key.date().isBefore(t0Date) && !key.date().isAfter(t3Date) }
    }

    fun TemporaryExposureKey.date(): LocalDate {
        return LocalDate.fromDateFields(Date(this.rollingStartIntervalNumber * 60L * 10L * 1000L))
    }
}
