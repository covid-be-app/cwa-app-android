package be.sciensano.coronalert.ui.covicode

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.sciensano.coronalert.service.submission.SubmissionService
import be.sciensano.coronalert.util.TemporaryExposureKeyExtensions.inT0T3Range
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.TransactionException
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.util.Event
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toServerFormat
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

private const val INFECTIOUS_DAYS_MINUS = 2
private const val INFECTIOUS_DAYS_MINUS_WITHOUT_SYMPTOMS = 7

class CovicodeViewModel : ViewModel() {

    companion object {
        private val TAG: String? = CovicodeViewModel::class.simpleName
    }

    private val symptomsDate: MutableLiveData<Date?> = MutableLiveData(null)
    private val covicode: MutableLiveData<String> = MutableLiveData(null)

    private val _submissionState = MutableLiveData(ApiRequestState.IDLE)
    private val _submissionError = MutableLiveData<Event<CwaWebException>>(null)
    val submissionState: LiveData<ApiRequestState> = _submissionState
    val submissionError: LiveData<Event<CwaWebException>> = _submissionError
    private val _uiStateState = MutableLiveData(ApiRequestState.IDLE)
    val uiStateState: LiveData<ApiRequestState> = _uiStateState

    fun setSymptomsDate(date: Date?) = run {
        symptomsDate.value = date
    }

    fun setCovicode(code: String) = run {
        covicode.value = code
    }

    fun beSubmitDiagnosisKeysForCovicode(
        keys: List<TemporaryExposureKey>
    ) =
        viewModelScope.launch {
            try {
                _submissionState.value = ApiRequestState.STARTED

                val onsetSymptomsDate = symptomsDate.value
                val cal = Calendar.getInstance()
                cal.time = onsetSymptomsDate ?: Date()
                cal.add(
                    Calendar.DATE,
                    if (onsetSymptomsDate != null) -INFECTIOUS_DAYS_MINUS
                    else -INFECTIOUS_DAYS_MINUS_WITHOUT_SYMPTOMS
                )
                val t0 = (cal.time).toServerFormat()
                val t3 = Date().toServerFormat()

                SubmissionService.asyncSubmitExposureKeysForCovicode(
                    t0,
                    onsetSymptomsDate,
                    covicode.value ?: "",
                    keys.inT0T3Range(t0, t3)
                )
                _submissionState.value = ApiRequestState.SUCCESS
            } catch (err: CwaWebException) {
                _submissionError.value = Event(err)
                _submissionState.value = ApiRequestState.FAILED
            } catch (err: TransactionException) {
                if (err.cause is CwaWebException) {
                    _submissionError.value = Event(err.cause)
                } else {
                    err.report(ExceptionCategory.INTERNAL)
                }
                _submissionState.value = ApiRequestState.FAILED
            } catch (err: Exception) {
                _submissionState.value = ApiRequestState.FAILED
                err.report(ExceptionCategory.INTERNAL)
            }
        }

    fun submitWithNoDiagnosisKeys() {
        de.rki.coronawarnapp.service.submission.SubmissionService.submissionSuccessful()
    }

}

