package be.sciensano.coronalert.http.service

import be.sciensano.coronalert.http.responses.StatisticsResponse
import retrofit2.http.GET

interface StatisticsService {

    @GET("statistics/statistics.json")
    suspend fun getStatistics(): StatisticsResponse

}
