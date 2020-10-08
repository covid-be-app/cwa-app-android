package be.sciensano.coronalert.service.diagnosiskey

import KeyExportFormat
import be.sciensano.coronalert.storage.k
import be.sciensano.coronalert.storage.r0
import be.sciensano.coronalert.storage.resultChannel
import be.sciensano.coronalert.storage.t0
import be.sciensano.coronalert.storage.t3
import de.rki.coronawarnapp.exception.DiagnosisKeyRetrievalException
import de.rki.coronawarnapp.exception.DiagnosisKeySubmissionException
import de.rki.coronawarnapp.http.WebRequestBuilder
import de.rki.coronawarnapp.storage.LocalData
import timber.log.Timber

/**
 * The Diagnosis Key Service is used to interact with the Server to submit and retrieve keys through
 * predefined structures.
 *
 * @throws DiagnosisKeyRetrievalException An Exception thrown when an error occurs during Key Retrieval from the Server
 * @throws DiagnosisKeySubmissionException An Exception thrown when an error occurs during Key Reporting to the Server
 */
object DiagnosisKeyService {

    private val TAG: String? = DiagnosisKeyService::class.simpleName

    /**
     * Asynchronously submits keys to the Server with the WebRequestBuilder by retrieving
     * keys out of the Google API.
     *
     *
     * @throws de.rki.coronawarnapp.exception.DiagnosisKeySubmissionException An Exception thrown when an error occurs during Key Reporting to the Server
     *
     * @param keysToReport - KeyList in the Server Format to submit to the Server
     */
    suspend fun asyncSubmitKeys(
        keysToReport: List<Pair<KeyExportFormat.TemporaryExposureKey, String>>
    ) {
        Timber.d("Diagnosis Keys will be submitted.")
        val k = LocalData.k() ?: throw IllegalStateException()
        val r0 = LocalData.r0() ?: throw IllegalStateException()
        val t0 = LocalData.t0() ?: throw IllegalStateException()
        val t3 = LocalData.t3() ?: throw IllegalStateException()
        val resultChannel = LocalData.resultChannel()
        if (resultChannel == -1) {
            throw IllegalStateException()
        }

        WebRequestBuilder.getInstance().beAsyncSubmitKeysToServer(
            k, r0, t0, t3, resultChannel,
            keysToReport
        )
    }
}
