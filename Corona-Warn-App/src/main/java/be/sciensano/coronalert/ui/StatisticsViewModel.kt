package be.sciensano.coronalert.ui

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.sciensano.coronalert.service.Statistics
import be.sciensano.coronalert.service.StatisticsService
import kotlinx.coroutines.launch

class StatisticsViewModel : ViewModel() {

    companion object {
        private val TAG: String? = StatisticsViewModel::class.simpleName
    }


    private val _statistics: MutableLiveData<Statistics?> = MutableLiveData()
    val statistics: LiveData<Statistics?> = _statistics


    fun refreshStatistics(context: Context) = viewModelScope.launch {
        val statistics = StatisticsService.fetchStatistics(context)
        _statistics.postValue(statistics)
    }

}

