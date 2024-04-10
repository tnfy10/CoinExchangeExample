package xyz.myeoru.coinexchangeexample.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BitThumbAllTickerResponse(
    @Json(name = "data")
    val data: Map<String, Any>,
    @Json(name = "status")
    val status: String
)