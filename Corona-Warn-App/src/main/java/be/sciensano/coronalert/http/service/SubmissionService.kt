package be.sciensano.coronalert.http.service

import KeyExportFormat
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

interface SubmissionService {

    @POST
    suspend fun submitKeys(
        @Url url: String,
        @Header("Secret-Key") k: String,
        @Header("Random-String") r0: String,
        @Header("Date-Patient-Infectious") t0: String,
        @Header("Date-Test-Communicated") t3: String,
        @Header("Result-Channel") channel: Int,
        @Body requestBody: KeyExportFormat.SubmissionPayload
    ): ResponseBody
}
