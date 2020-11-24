package be.sciensano.coronalert.service.submission

import be.sciensano.coronalert.http.responses.TestResultResponse
import be.sciensano.coronalert.storage.isTestResultNegative
import be.sciensano.coronalert.storage.k
import be.sciensano.coronalert.storage.onsetSymptomsDate
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

    suspend fun asyncRequestTestResult(): TestResultResponse {
        val registrationToken =
            LocalData.registrationToken() ?: throw NoRegistrationTokenSetException()

        return WebRequestBuilder.getInstance().beAsyncGetTestResult(registrationToken)
    }

    suspend fun asyncSendAck(testResult: TestResultResponse) {

        if (TestResult.fromInt(testResult.result) == TestResult.POSITIVE) {
            LocalData.t3(Date().toServerFormat())
            LocalData.resultChannel(testResult.resultChannel)
        }

        val registrationToken =
            LocalData.registrationToken() ?: throw NoRegistrationTokenSetException()

        WebRequestBuilder.getInstance().beAsyncAckTestResult(registrationToken)
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
        LocalData.isTestResultNegative(false)
        LocalData.t0(null)
        LocalData.onsetSymptomsDate(null)
        LocalData.r0(null)
        LocalData.k(null)
        LocalData.t3(null)
        LocalData.resultChannel(-1)
        LocalData.initialPollingForTestResultTimeStamp(0L)
        LocalData.initialTestResultReceivedTimestamp(0L)
        LocalData.isAllowedToSubmitDiagnosisKeys(false)
        LocalData.isTestResultNotificationSent(false)
    }
}
