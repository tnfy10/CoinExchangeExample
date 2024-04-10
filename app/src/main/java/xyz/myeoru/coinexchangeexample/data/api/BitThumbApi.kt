package xyz.myeoru.coinexchangeexample.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import xyz.myeoru.coinexchangeexample.data.dto.BitThumbAllTickerResponse

interface BitThumbApi {
    @GET("public/ticker/{paymentCurrency}")
    suspend fun getAllTicker(
        @Path("paymentCurrency") paymentCurrency: String
    ): Response<BitThumbAllTickerResponse>
}