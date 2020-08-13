package be.sciensano.coronalert.transaction

import be.sciensano.coronalert.transaction.SubmitDiagnosisKeysTransaction.SubmitDiagnosisKeysTransactionState.CLOSE
import be.sciensano.coronalert.transaction.SubmitDiagnosisKeysTransaction.SubmitDiagnosisKeysTransactionState.RETRIEVE_TEMPORARY_EXPOSURE_KEY_HISTORY
import be.sciensano.coronalert.transaction.SubmitDiagnosisKeysTransaction.SubmitDiagnosisKeysTransactionState.STORE_SUCCESS
import be.sciensano.coronalert.transaction.SubmitDiagnosisKeysTransaction.SubmitDiagnosisKeysTransactionState.SUBMIT_KEYS
import be.sciensano.coronalert.ui.submission.Country
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.transaction.Transaction
import de.rki.coronawarnapp.transaction.TransactionState
import de.rki.coronawarnapp.util.ProtoFormatConverterExtensions.transformKeyHistoryToExternalFormat
import be.sciensano.coronalert.service.diagnosiskey.DiagnosisKeyService as BeDiagnosisKeyService
import be.sciensano.coronalert.service.submission.SubmissionService as BeSubmissionService

/**
 * The SubmitDiagnosisKeysTransaction is used to define an atomic Transaction for Key Reports. Its states allow an
 * isolated work area that can recover from failures and keep a consistent key state even through an
 * unclear, potentially dangerous state within the transaction itself. It is guaranteed that the Key Files
 * that are used in the transaction will be generated, submitted and accepted from the Google API once the transaction
 * has completed its work and returned from the start() coroutine.
 *
 * There is currently a simple rollback behavior needed / identified.
 *
 * The Transaction undergoes multiple States:
 * 1. RETRIEVE_TAN - Fetch the TAN with the provided Registration Token
 * 2. RETRIEVE_TEMPORARY_EXPOSURE_KEY_HISTORY - Get the TEKs from the exposure notification framework
 * 3. SUBMIT_KEYS - Submission of the diagnosis keys to the Server
 * 4. CLOSE - Transaction Closure
 *
 * This transaction is special in terms of concurrent entry-calls (e.g. calling the transaction again before it closes and
 * releases its internal mutex. The transaction will not queue up like a normal mutex, but instead completely omit the last
 * execution. Execution Privilege is First In.
 *
 * @see Transaction
 *
 * @throws de.rki.coronawarnapp.exception.TransactionException An Exception thrown when an error occurs during Transaction Execution
 */
object SubmitDiagnosisKeysTransaction : Transaction() {

    override val TAG: String? = SubmitDiagnosisKeysTransaction::class.simpleName

    /** possible transaction states */
    private enum class SubmitDiagnosisKeysTransactionState :
        TransactionState {
        RETRIEVE_TEMPORARY_EXPOSURE_KEY_HISTORY,
        SUBMIT_KEYS,
        STORE_SUCCESS,
        CLOSE
    }

    /** initiates the transaction. This suspend function guarantees a successful transaction once completed. */
    suspend fun start(keys: List<Pair<TemporaryExposureKey, Country>>) =
        lockAndExecuteUnique {

            /****************************************************
             * RETRIEVE TEMPORARY EXPOSURE KEY HISTORY
             ****************************************************/
            val temporaryExposureKeyList = executeState(RETRIEVE_TEMPORARY_EXPOSURE_KEY_HISTORY) {
                keys.map { it.first }.transformKeyHistoryToExternalFormat()
                    .mapIndexed { index, temporaryExposureKey ->
                        Pair(
                            temporaryExposureKey,
                            keys[index].second.code3
                        )
                    }
            }

            /****************************************************
             * SUBMIT KEYS
             ****************************************************/
            executeState(SUBMIT_KEYS) {
                BeDiagnosisKeyService.asyncSubmitKeys(temporaryExposureKeyList)
            }
            /****************************************************
             * STORE SUCCESS
             ****************************************************/
            executeState(STORE_SUCCESS) {
                BeSubmissionService.submissionSuccessful()
            }
            /****************************************************
             * CLOSE TRANSACTION
             ****************************************************/
            executeState(CLOSE) {}
        }
}
