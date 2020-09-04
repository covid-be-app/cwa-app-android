package be.sciensano.coronalert.service

data class Statistics(
    val averageInfected: Int,
    val averageInfectedChangePercentage: Int,
    val averageHospitalised: Int,
    val averageHospitalisedChangePercentage: Int,
    val averageDeceased: Int,
    val averageDeceasedChangePercentage: Int,
    val startDate: Long,
    val endDate: Long,
    val lastUpdatedDate: Long
)
