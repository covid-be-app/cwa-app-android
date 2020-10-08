package be.sciensano.coronalert.service

import be.sciensano.coronalert.http.responses.StatisticsResponse
import be.sciensano.coronalert.storage.lastAverageDeceased
import be.sciensano.coronalert.storage.lastAverageDeceasedChangePercent
import be.sciensano.coronalert.storage.lastAverageHospitalised
import be.sciensano.coronalert.storage.lastAverageHospitalisedChangePercent
import be.sciensano.coronalert.storage.lastAverageInfected
import be.sciensano.coronalert.storage.lastAverageInfectedChangePercent
import be.sciensano.coronalert.storage.statisticsDate
import be.sciensano.coronalert.storage.statisticsEndDate
import be.sciensano.coronalert.storage.statisticsStartDate
import be.sciensano.coronalert.util.DateUtil
import de.rki.coronawarnapp.http.WebRequestBuilder
import de.rki.coronawarnapp.storage.LocalData
import timber.log.Timber
import java.util.Date

object StatisticsService {

    suspend fun fetchStatistics(): Statistics? {
        try {
            val response = WebRequestBuilder.getInstance().getStatistics()
            storeStatistics(response)
        } catch (e: Exception) {
            Timber.e(e)
        }

        return getStatistics()
    }

    private fun storeStatistics(statistics: StatisticsResponse) {
        LocalData.statisticsDate(Date().time)
        LocalData.statisticsStartDate(DateUtil.parseServerDate(statistics.startDate).toDate().time)
        LocalData.statisticsEndDate(DateUtil.parseServerDate(statistics.endDate).toDate().time)
        LocalData.lastAverageInfected(statistics.averageInfected)
        LocalData.lastAverageInfectedChangePercent(statistics.averageInfectedChangePercentage)
        LocalData.lastAverageHospitalised(statistics.averageHospitalised)
        LocalData.lastAverageHospitalisedChangePercent(statistics.averageHospitalisedChangePercentage)
        LocalData.lastAverageDeceased(statistics.averageDeceased)
        LocalData.lastAverageDeceasedChangePercent(statistics.averageDeceasedChangePercentage)
    }

    private fun getStatistics(): Statistics? {
        return if (LocalData.statisticsDate() != -1L) {
            Statistics(
                LocalData.lastAverageInfected(),
                LocalData.lastAverageInfectedChangePercent(),
                LocalData.lastAverageHospitalised(),
                LocalData.lastAverageHospitalisedChangePercent(),
                LocalData.lastAverageDeceased(),
                LocalData.lastAverageDeceasedChangePercent(),
                LocalData.statisticsStartDate(),
                LocalData.statisticsEndDate(),
                LocalData.statisticsDate()
            )
        } else {
            null
        }
    }
}
