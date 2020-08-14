package be.sciensano.coronalert.service.submission

import be.sciensano.coronalert.storage.k
import be.sciensano.coronalert.storage.r0
import be.sciensano.coronalert.storage.resultChannel
import be.sciensano.coronalert.storage.t0
import be.sciensano.coronalert.storage.t3
import be.sciensano.coronalert.transaction.SubmitDiagnosisKeysTransaction
import be.sciensano.coronalert.ui.submission.Country
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.exception.NoRegistrationTokenSetException
import de.rki.coronawarnapp.http.WebRequestBuilder
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toServerFormat
import de.rki.coronawarnapp.util.formatter.TestResult
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler
import java.util.Date
import de.rki.coronawarnapp.service.submission.SubmissionService as DeSubmissionService

object SubmissionService {

    suspend fun asyncRequestTestResult(): TestResult {
        val registrationToken =
            LocalData.registrationToken() ?: throw NoRegistrationTokenSetException()

        val testResult = WebRequestBuilder.getInstance().beAsyncGetTestResult(registrationToken)
        val testResultStatus = TestResult.fromInt(testResult.result)

        if (testResultStatus === TestResult.POSITIVE) {
            LocalData.t3(Date().toServerFormat())
            LocalData.resultChannel(testResult.resultChannel)
        }

        if (testResultStatus !== TestResult.PENDING) {
            WebRequestBuilder.getInstance().beAsyncAckTestResult(registrationToken)
        }

        return testResultStatus
    }

    suspend fun asyncSubmitExposureKeys(keys: List<Pair<TemporaryExposureKey, Country>>) {
        SubmitDiagnosisKeysTransaction.start(keys)
    }


    fun submissionSuccessful() {
        BackgroundWorkScheduler.stopWorkScheduler()
        deleteRegistrationToken()
        LocalData.numberOfSuccessfulSubmissions(1)
    }

    fun deleteRegistrationToken() {
        DeSubmissionService.deleteRegistrationToken()
        LocalData.t0(null)
        LocalData.r0(null)
        LocalData.k(null)
        LocalData.t3(null)
        LocalData.resultChannel(-1)
    }
}
