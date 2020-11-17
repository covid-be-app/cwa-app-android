package be.sciensano.coronalert.ui

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.sciensano.coronalert.http.responses.DynamicTexts
import be.sciensano.coronalert.service.DynamicTextsService
import kotlinx.coroutines.launch

class DynamicTextsViewModel : ViewModel() {

    companion object {
        private val TAG: String? = DynamicTextsViewModel::class.simpleName
    }

    private val _dynamicTexts: MutableLiveData<DynamicTexts> = MutableLiveData()
    val dynamicTexts: LiveData<DynamicTexts> = _dynamicTexts

    fun getDynamicTexts(context: Context) = viewModelScope.launch {
        val dynamicTexts = DynamicTextsService.fetchDynamicTexts(context)
        _dynamicTexts.postValue(dynamicTexts)
    }
}
