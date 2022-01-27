package be.sciensano.coronalert.service

import be.sciensano.coronalert.http.responses.StatisticsResponse

data class Statistics(
    val averageInfected: Int,
    val averageInfectedChangePercentage: Int,
    val averageHospitalised: Int,
    val averageHospitalisedChangePercentage: Int,
    val averageDeceased: Int,
    val averageDeceasedChangePercentage: Int,
    val atLeastPartiallyVaccinated: Long,
    val fullyVaccinated: Long,
    val boosterVaccinated: Long,
    val startDate: Long,
    val endDate: Long,
    val lastUpdatedDate: Long,
)
