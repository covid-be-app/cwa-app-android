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
