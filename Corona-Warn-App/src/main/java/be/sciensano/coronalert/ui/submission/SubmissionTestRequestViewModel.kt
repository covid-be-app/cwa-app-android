package be.sciensano.coronalert.ui.submission

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import be.sciensano.coronalert.MobileTestId
import be.sciensano.coronalert.storage.r0
import be.sciensano.coronalert.storage.k
import be.sciensano.coronalert.storage.t0
import de.rki.coronawarnapp.storage.LocalData
import java.util.*

class SubmissionTestRequestViewModel : ViewModel() {

    companion object {
        private val TAG: String? = SubmissionTestRequestViewModel::class.simpleName
    }

    val submissionDate: MutableLiveData<Date> = MutableLiveData(Date())

    private val _mobileTestId: MutableLiveData<MobileTestId> = MutableLiveData()

    fun setSubmissionDate(date: Date) = run { submissionDate.value = date }

    fun generateTestId(): MobileTestId {
        val testId = MobileTestId.generate(submissionDate.value!!)
        _mobileTestId.postValue(testId)
        return testId
    }

    fun saveTestId() =
        _mobileTestId.value?.let { mobileTestId ->
            LocalData.t0(mobileTestId.t0)
            LocalData.r0(mobileTestId.r0)
            LocalData.k(mobileTestId.k)
            LocalData.registrationToken(mobileTestId.registrationToken())
        }

}

