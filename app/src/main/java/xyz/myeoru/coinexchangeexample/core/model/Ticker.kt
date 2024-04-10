package xyz.myeoru.coinexchangeexample.core.model

import org.json.JSONObject
import xyz.myeoru.coinexchangeexample.core.constant.CoinSymbols
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class Ticker(
    val coinSymbols: CoinSymbols,
    val currencyUnit: String,
    val tickType: String,
    val date: LocalDate,
    val time: LocalTime,
    val openPrice: Double,
    val closePrice: Double,
    val lowPrice: Double,
    val highPrice: Double,
    val value: Double,
    val volume: Double,
    val sellVolume: Double,
    val buyVolume: Double,
    val prevClosePrice: Double,
    val chgRate: Double,
    val chgAmt: Double,
    val volumePower: Double
)

fun JSONObject.mapperToTicker(): Ticker {
    val symbolSplit = this.getString("symbol").split("_")
    val coinSymbols = CoinSymbols.entries.find { it.name == symbolSplit[0] } ?: throw Exception(
        "CoinSymbol not found."
    )
    val currencyUnit = symbolSplit[1]
    val tickType = this.getString("tickType")
    val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    val date = LocalDate.parse(this.getString("date"), dateFormatter)
    val timeFormatter = DateTimeFormatter.ofPattern("HHmmss")
    val time = LocalTime.parse(this.getString("time"), timeFormatter)
    val openPrice = this.getString("openPrice").toDouble()
    val closePrice = this.getString("closePrice").toDouble()
    val lowPrice = this.getString("lowPrice").toDouble()
    val highPrice = this.getString("highPrice").toDouble()
    val value = this.getString("value").toDouble()
    val volume = this.getString("volume").toDouble()
    val sellVolume = this.getString("sellVolume").toDouble()
    val buyVolume = this.getString("buyVolume").toDouble()
    val prevClosePrice = this.getString("prevClosePrice").toDouble()
    val chgRate = this.getString("chgRate").toDouble()
    val chgAmt = this.getString("chgAmt").toDouble()
    val volumePower = this.getString("volumePower").toDouble()

    return Ticker(
        coinSymbols = coinSymbols,
        currencyUnit = currencyUnit,
        tickType = tickType,
        date = date,
        time = time,
        openPrice = openPrice,
        closePrice = closePrice,
        lowPrice = lowPrice,
        highPrice = highPrice,
        value = value,
        volume = volume,
        sellVolume = sellVolume,
        buyVolume = buyVolume,
        prevClosePrice = prevClosePrice,
        chgRate = chgRate,
        chgAmt = chgAmt,
        volumePower = volumePower
    )
}