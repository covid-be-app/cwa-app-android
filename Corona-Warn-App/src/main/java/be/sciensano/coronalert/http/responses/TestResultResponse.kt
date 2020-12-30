package be.sciensano.coronalert.http.responses

data class TestResultResponse(
    val result: Int,
    val resultChannel: Int,
    val datePatientInfectious: String,
    val dateSampleCollected: String,
    val dateTestCommunicated: String,
    val responsePadding: String
)
