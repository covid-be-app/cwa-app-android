package be.sciensano.coronalert.ui.submission

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import be.sciensano.coronalert.MobileTestId
import be.sciensano.coronalert.storage.k
import be.sciensano.coronalert.storage.r0
import be.sciensano.coronalert.storage.t0
import de.rki.coronawarnapp.storage.LocalData
import java.util.Calendar
import java.util.Date

class SubmissionTestRequestViewModel : ViewModel() {

    companion object {
        private val TAG: String? = SubmissionTestRequestViewModel::class.simpleName
        private const val DAYS_MINUS = 2
    }

    val submissionDate: MutableLiveData<Date> = MutableLiveData(Date())

    fun setSubmissionDate(date: Date) = run { submissionDate.value = date }

    fun generateTestId(): MobileTestId {
        val cal = Calendar.getInstance()
        cal.time = submissionDate.value!!
        cal.add(Calendar.DATE, -DAYS_MINUS)
        val submissionDateMinus2 = cal.time

        val testId = MobileTestId.generate(submissionDateMinus2)
        saveTestId(testId)
        return testId
    }

    private fun saveTestId(mobileTestId: MobileTestId) {
        LocalData.t0(mobileTestId.t0)
        LocalData.r0(mobileTestId.r0)
        LocalData.k(mobileTestId.k)
        LocalData.registrationToken(mobileTestId.registrationToken())
        LocalData.initialPollingForTestResultTimeStamp(System.currentTimeMillis())
    }
}

