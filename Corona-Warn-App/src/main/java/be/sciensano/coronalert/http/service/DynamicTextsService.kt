package be.sciensano.coronalert.http.service

import be.sciensano.coronalert.http.responses.DynamicNews
import be.sciensano.coronalert.http.responses.DynamicTexts
import retrofit2.http.GET

interface DynamicTextsService {

    @GET("dynamictext/dynamicTextsV2.json")
    suspend fun getDynamicTexts(): DynamicTexts

    @GET("dynamictext/dynamicNews.json")
    suspend fun getDynamicNews(): DynamicNews
}
