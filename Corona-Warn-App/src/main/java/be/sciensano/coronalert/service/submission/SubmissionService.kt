package be.sciensano.coronalert.service.submission


import be.sciensano.coronalert.storage.r0
import be.sciensano.coronalert.storage.t0
import de.rki.coronawarnapp.exception.NoRegistrationTokenSetException
import de.rki.coronawarnapp.http.WebRequestBuilder
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.util.formatter.TestResult
import de.rki.coronawarnapp.service.submission.SubmissionService as DeSubmissionService

object SubmissionService {

    suspend fun asyncRequestTestResult(): TestResult {
        val registrationToken =
            LocalData.registrationToken() ?: throw NoRegistrationTokenSetException()

        val testResult = WebRequestBuilder.getInstance().beAsyncGetTestResult(registrationToken)
        LocalData

        return TestResult.fromInt(
            testResult.result
        )
    }

    fun deleteRegistrationToken() {
        DeSubmissionService.deleteRegistrationToken()
        LocalData.t0(null)
        LocalData.r0(null)
    }
}
