package xyz.myeoru.coinexchangeexample.core.model

import xyz.myeoru.coinexchangeexample.core.constant.CoinSymbols
import java.time.LocalDate
import java.time.LocalTime

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
