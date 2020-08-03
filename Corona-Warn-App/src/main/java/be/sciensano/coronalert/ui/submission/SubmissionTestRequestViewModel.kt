package be.sciensano.coronalert.ui.submission

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import be.sciensano.coronalert.MobileTestId
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

    fun saveTestId(): LiveData<MobileTestId> {
        return _mobileTestId
    }
}

