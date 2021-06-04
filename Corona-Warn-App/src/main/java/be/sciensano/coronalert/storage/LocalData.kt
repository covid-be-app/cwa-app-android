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

fun LocalData.dataTransfer(value: Boolean) {
    getSharedPreferenceInstance().edit(true) {
        putBoolean("data_transfer", value)
    }
}

fun LocalData.dataTransfer() =
    getSharedPreferenceInstance().getBoolean("data_transfer", true)

fun LocalData.lastExposureDate(value: Long) {
    getSharedPreferenceInstance().edit(true) {
        putLong("last_exposure_date", value)
    }
}

fun LocalData.lastExposureDate() =
    getSharedPreferenceInstance().getLong("last_exposure_date", 0)


fun LocalData.matchedKeyCount(value: Int) {
    getSharedPreferenceInstance().edit(true) {
        putInt("matched_key_count", value)
    }
}

fun LocalData.matchedKeyCount() =
    getSharedPreferenceInstance().getInt("matched_key_count", 0)

