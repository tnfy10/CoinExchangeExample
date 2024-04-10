package xyz.myeoru.coinexchangeexample.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import retrofit2.HttpException
import timber.log.Timber
import xyz.myeoru.coinexchangeexample.core.model.Ticker
import xyz.myeoru.coinexchangeexample.core.model.mapperToTicker
import xyz.myeoru.coinexchangeexample.data.api.BitThumbApi
import xyz.myeoru.coinexchangeexample.data.di.OkHttpClientModule
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

class CoinInfoRepositoryImpl @Inject constructor(
    @OkHttpClientModule.SocketOkHttpClient private val socketClient: OkHttpClient,
    private val bitThumbApi: BitThumbApi
) : CoinInfoRepository {

    override fun receiveCoinCurrentPrice(
        symbols: List<String>,
        currencyUnit: String,
        requestIntervalMs: Long
    ): Flow<Ticker> = callbackFlow {
        var isConnected = false
        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                isConnected = true
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                isConnected = false
                webSocket.close(1000, "Coin current price socket closed.")
                webSocket.cancel()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                isConnected = false
                channel.close()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val jsonObject = JSONObject(text)
                if (jsonObject.has("type")) {
                    val type = jsonObject.getString("type")
                    if (type == "ticker") {
                        val content = jsonObject.getJSONObject("content")
                        val ticker = content.mapperToTicker()
                        trySendBlocking(ticker).onFailure {
                            Timber.e(it)
                        }
                    }
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                isConnected = false
            }
        }
        val request = Request.Builder()
            .url("wss://pubwss.bithumb.com/pub/ws")
            .build()
        var webSocket = socketClient.newWebSocket(request, listener)
        launch {
            while (true) {
                if (isConnected) {
                    val reqMap = mapOf(
                        "type" to "ticker",
                        "symbols" to symbols.map { "${it}_$currencyUnit" },
                        "tickTypes" to listOf("MID")
                    )
                    val jsonObject = JSONObject(reqMap)
                    val json = jsonObject.toString()
                    webSocket.send(json)
                    delay(requestIntervalMs)
                } else {
                    webSocket = socketClient.newWebSocket(request, listener)
                    delay(1000)
                }
            }
        }
        awaitClose {
            webSocket.close(1000, "Coin current price socket closed.")
            webSocket.cancel()
        }
    }.flowOn(Dispatchers.IO)

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