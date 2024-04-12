package xyz.myeoru.coinexchangeexample.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONObject
import retrofit2.HttpException
import xyz.myeoru.coinexchangeexample.core.model.Ticker
import xyz.myeoru.coinexchangeexample.data.api.BitThumbApi
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

class CoinInfoRepositoryImpl @Inject constructor(
    private val bitThumbApi: BitThumbApi
) : CoinInfoRepository {

    override fun getAllCoinCurrentPrice(
        symbols: List<String>,
        currencyUnit: String
    ): Flow<List<Ticker>> = flow {
        val resp = bitThumbApi.getAllTicker("ALL_$currencyUnit")
        val body = resp.body()

        if (resp.isSuccessful && body != null) {
            val timeInMillis = body.data["date"].toString().toLongOrNull()
            val localDateTime = timeInMillis?.let {
                Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime()
            }
            val filteredList = body.data.filter { symbols.contains(it.key) }
            val tickers = buildList {
                for (dataMap in filteredList) {
                    val jsonObject = JSONObject(dataMap.value.toString())
                    add(
                        Ticker(
                            symbol = dataMap.key,
                            currencyUnit = currencyUnit,
                            tickType = "",
                            date = localDateTime?.toLocalDate() ?: LocalDate.now(),
                            time = localDateTime?.toLocalTime() ?: LocalTime.now(),
                            openPrice = jsonObject.getDouble("opening_price"),
                            closePrice = jsonObject.getDouble("closing_price"),
                            lowPrice = jsonObject.getDouble("min_price"),
                            highPrice = jsonObject.getDouble("max_price"),
                            value = jsonObject.getDouble("acc_trade_value"),
                            volume = jsonObject.getDouble("units_traded"),
                            sellVolume = 0.0,
                            buyVolume = 0.0,
                            prevClosePrice = jsonObject.getDouble("prev_closing_price"),
                            chgRate = jsonObject.getDouble("fluctate_rate_24H"),
                            chgAmt = jsonObject.getDouble("fluctate_24H"),
                            volumePower = 0.0
                        )
                    )
                }
            }
            emit(tickers)
        } else {
            throw HttpException(resp)
        }
    }.flowOn(Dispatchers.IO)
}