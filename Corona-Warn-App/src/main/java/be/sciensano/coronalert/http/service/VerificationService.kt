package be.sciensano.coronalert.http.service

import be.sciensano.coronalert.http.requests.TestResultRequest
import be.sciensano.coronalert.http.responses.TestResultResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface VerificationService {

    @POST
    suspend fun getTestResult(
        @Url url: String,
        @Body request: TestResultRequest
    ): TestResultResponse

    @POST
    suspend fun ackResult(
        @Url url: String,
        @Body request: TestResultRequest
    ): Response<Unit>

}
