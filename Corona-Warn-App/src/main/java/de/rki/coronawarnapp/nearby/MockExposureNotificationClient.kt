package de.rki.coronawarnapp.nearby

import com.google.android.gms.common.api.Api
import com.google.android.gms.common.api.internal.ApiKey
import com.google.android.gms.nearby.exposurenotification.ExposureConfiguration
import com.google.android.gms.nearby.exposurenotification.ExposureInformation
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import com.google.android.gms.nearby.exposurenotification.ExposureSummary
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import java.io.File

class MockExposureNotificationClient : ExposureNotificationClient {

    private var enabled: Boolean = false

    override fun isEnabled(): Task<Boolean> {
        return Tasks.forResult(enabled)
    }

    override fun provideDiagnosisKeys(
        p0: List<File>?,
        p1: ExposureConfiguration?,
        p2: String?
    ): Task<Void> {
        return Tasks.forResult(null)
    }

    override fun getExposureSummary(p0: String?): Task<ExposureSummary> {

        val builder = ExposureSummary.ExposureSummaryBuilder()

        builder.setAttenuationDurations(
            intArrayOf(
                0,
                0,
                0
            )
        )
        builder.setDaysSinceLastExposure(0)
        builder.setMatchedKeyCount(0)
        builder.setMaximumRiskScore(0)
        builder.setSummationRiskScore(0)

        return Tasks.forResult(builder.build())
    }

    override fun start(): Task<Void> {
        enabled = true
        return Tasks.forResult(null)
    }

    override fun stop(): Task<Void> {
        enabled = false
        return Tasks.forResult(null)
    }

    override fun getExposureInformation(p0: String?): Task<List<ExposureInformation>> {
        return Tasks.forResult(null)
    }

    override fun getApiKey(): ApiKey<Api.ApiOptions.NoOptions> {
        throw NotImplementedError()
    }

    override fun getTemporaryExposureKeyHistory(): Task<List<TemporaryExposureKey>> {
        return Tasks.forResult(emptyList())
    }
}
