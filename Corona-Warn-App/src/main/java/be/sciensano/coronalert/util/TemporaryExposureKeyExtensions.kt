package be.sciensano.coronalert.util

import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import org.joda.time.LocalDate
import java.util.Date

@Suppress("MagicNumber")
object TemporaryExposureKeyExtensions {

    fun List<TemporaryExposureKey>.inT0T3Range(t0: String, t3: String) = run {
        val t0Date = DateUtil.parseServerDate(t0)
        val t3Date = DateUtil.parseServerDate(t3)

        this.filter { key -> !key.date().isBefore(t0Date) && !key.date().isAfter(t3Date) }
    }

    fun TemporaryExposureKey.date(): LocalDate {
        return LocalDate.fromDateFields(Date(this.rollingStartIntervalNumber * 60L * 10L * 1000L))
    }
}
