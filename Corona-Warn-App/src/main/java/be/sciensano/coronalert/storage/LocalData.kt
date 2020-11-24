package be.sciensano.coronalert.storage

import androidx.core.content.edit
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.storage.LocalData


fun LocalData.t0(): String? = getSharedPreferenceInstance().getString(
    CoronaWarnApplication.getAppContext()
        .getString(R.string.preference_t0),
    null
)

fun LocalData.t0(value: String?) {
    getSharedPreferenceInstance().edit(true) {
        putString(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_t0),
            value
        )
    }
}

fun LocalData.onsetSymptomsDate(): String? = getSharedPreferenceInstance().getString(
    "date_onset_symptoms",
    null
)

fun LocalData.onsetSymptomsDate(value: String?) {
    getSharedPreferenceInstance().edit(true) {
        putString(
            "date_onset_symptoms",
            value
        )
    }
}

fun LocalData.r0(): String? = getSharedPreferenceInstance().getString(
    CoronaWarnApplication.getAppContext()
        .getString(R.string.preference_r0),
    null
)

fun LocalData.r0(value: String?) {
    getSharedPreferenceInstance().edit(true) {
        putString(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_r0),
            value
        )
    }
}

fun LocalData.k(): String? = getSharedPreferenceInstance().getString(
    CoronaWarnApplication.getAppContext()
        .getString(R.string.preference_k),
    null
)

fun LocalData.k(value: String?) {
    getSharedPreferenceInstance().edit(true) {
        putString(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_k),
            value
        )
    }
}

fun LocalData.t3(): String? = getSharedPreferenceInstance().getString(
    CoronaWarnApplication.getAppContext()
        .getString(R.string.preference_t3),
    null
)

fun LocalData.t3(value: String?) {
    getSharedPreferenceInstance().edit(true) {
        putString(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_t3),
            value
        )
    }
}

fun LocalData.resultChannel(): Int = getSharedPreferenceInstance().getInt(
    CoronaWarnApplication.getAppContext()
        .getString(R.string.preference_result_channel),
    -1
)

fun LocalData.resultChannel(value: Int) {
    getSharedPreferenceInstance().edit(true) {
        putInt(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_result_channel),
            value
        )
    }
}

fun LocalData.dummyTestRequestsToSend(value: Int) {
    getSharedPreferenceInstance().edit(true) {
        putInt(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_dummy_test_requests_to_send),
            value
        )
    }
}

fun LocalData.dummyTestRequestsToSend(): Int = getSharedPreferenceInstance().getInt(
    CoronaWarnApplication.getAppContext()
        .getString(R.string.preference_dummy_test_requests_to_send),
    -1
)


fun LocalData.isTestResultNegative(isNegative: Boolean) {
    getSharedPreferenceInstance().edit(true) {
        putBoolean(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_negative_test_result),
            isNegative
        )
    }
}

fun LocalData.isTestResultNegative(): Boolean? {
    return getSharedPreferenceInstance().getBoolean(
        CoronaWarnApplication.getAppContext()
            .getString(R.string.preference_negative_test_result),
        false
    )
}

fun LocalData.lastAverageInfected(value: Int) {
    getSharedPreferenceInstance().edit(true) {
        putInt(
            "average_infected",
            value
        )
    }
}

fun LocalData.lastAverageInfected(): Int = getSharedPreferenceInstance().getInt(
    "average_infected",
    -1
)

fun LocalData.lastAverageInfectedChangePercent(value: Int) {
    getSharedPreferenceInstance().edit(true) {
        putInt(
            "average_infected_change_percent",
            value
        )
    }
}

fun LocalData.lastAverageInfectedChangePercent(): Int = getSharedPreferenceInstance().getInt(
    "average_infected_change_percent",
    -1
)

fun LocalData.lastAverageHospitalised(value: Int) {
    getSharedPreferenceInstance().edit(true) {
        putInt(
            "average_hospitalised",
            value
        )
    }
}

fun LocalData.lastAverageHospitalised(): Int = getSharedPreferenceInstance().getInt(
    "average_hospitalised",
    -1
)

fun LocalData.lastAverageHospitalisedChangePercent(value: Int) {
    getSharedPreferenceInstance().edit(true) {
        putInt(
            "average_hospitalised_change_percent",
            value
        )
    }
}

fun LocalData.lastAverageHospitalisedChangePercent(): Int = getSharedPreferenceInstance().getInt(
    "average_hospitalised_change_percent",
    -1
)

fun LocalData.lastAverageDeceased(value: Int) {
    getSharedPreferenceInstance().edit(true) {
        putInt(
            "average_deceased",
            value
        )
    }
}

fun LocalData.lastAverageDeceased(): Int = getSharedPreferenceInstance().getInt(
    "average_deceased",
    -1
)

fun LocalData.lastAverageDeceasedChangePercent(value: Int) {
    getSharedPreferenceInstance().edit(true) {
        putInt(
            "average_deceased_change_percent",
            value
        )
    }
}

fun LocalData.lastAverageDeceasedChangePercent(): Int = getSharedPreferenceInstance().getInt(
    "average_deceased_change_percent",
    -1
)

fun LocalData.statisticsStartDate(): Long = getSharedPreferenceInstance().getLong(
    "statistics_start_date",
    -1L
)

fun LocalData.statisticsStartDate(value: Long) {
    getSharedPreferenceInstance().edit(true) {
        putLong(
            "statistics_start_date",
            value
        )
    }
}

fun LocalData.statisticsEndDate(): Long = getSharedPreferenceInstance().getLong(
    "statistics_end_date",
    -1L
)

fun LocalData.statisticsEndDate(value: Long) {
    getSharedPreferenceInstance().edit(true) {
        putLong(
            "statistics_end_date",
            value
        )
    }
}

fun LocalData.statisticsDate(): Long = getSharedPreferenceInstance().getLong(
    "statistics_date",
    -1L
)

fun LocalData.statisticsDate(value: Long) {
    getSharedPreferenceInstance().edit(true) {
        putLong(
            "statistics_date",
            value
        )
    }
}

fun LocalData.dataTransfer(value: Boolean) {
    getSharedPreferenceInstance().edit(true) {
        putBoolean("data_transfer", value)
    }
}

fun LocalData.dataTransfer() =
    getSharedPreferenceInstance().getBoolean("data_transfer", true)

