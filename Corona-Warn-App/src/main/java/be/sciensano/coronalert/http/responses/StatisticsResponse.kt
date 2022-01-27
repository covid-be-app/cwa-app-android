package be.sciensano.coronalert.http.responses

data class StatisticsResponse(
    val averageInfected: Int,
    val averageInfectedChangePercentage: Int,
    val averageHospitalised: Int,
    val averageHospitalisedChangePercentage: Int,
    val averageDeceased: Int,
    val averageDeceasedChangePercentage: Int,
    val atLeastPartiallyVaccinated: Long,
    val fullyVaccinated: Long,
    val boosterVaccinated: Long,
    val startDate: String,
    val endDate: String,
)
