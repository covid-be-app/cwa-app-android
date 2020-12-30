package be.sciensano.coronalert.ui.submission

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import be.sciensano.coronalert.MobileTestId
import be.sciensano.coronalert.storage.k
import be.sciensano.coronalert.storage.onsetSymptomsDate
import be.sciensano.coronalert.storage.r0
import be.sciensano.coronalert.storage.t0
import de.rki.coronawarnapp.storage.LocalData
import java.util.Date

class SubmissionTestRequestViewModel : ViewModel() {

    companion object {
        private val TAG: String? = SubmissionTestRequestViewModel::class.simpleName
    }

    private val symptomsDate: MutableLiveData<Date?> = MutableLiveData(null)

    fun setSymptomsDate(date: Date?) = run {
        symptomsDate.value = date
    }

    fun generateTestId(): MobileTestId {
        val testId = MobileTestId.generate(symptomsDate.value)
        saveTestId(testId)
        return testId
    }

    private fun saveTestId(mobileTestId: MobileTestId) {
        LocalData.t0(mobileTestId.t0)
        LocalData.onsetSymptomsDate(mobileTestId.onsetSymptomsDate)
        LocalData.r0(mobileTestId.r0)
        LocalData.k(mobileTestId.k)
        LocalData.registrationToken(mobileTestId.registrationToken())
        LocalData.initialPollingForTestResultTimeStamp(System.currentTimeMillis())
    }
}

