package be.sciensano.coronalert.http.service

import be.sciensano.coronalert.http.responses.DynamicTexts
import retrofit2.http.GET

interface DynamicTextsService {

    @GET("dynamictext/dynamicTexts.json")
    suspend fun getDynamicTexts(): DynamicTexts
}
