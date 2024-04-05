package xyz.myeoru.coinexchangeexample.data.socket

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import timber.log.Timber
import xyz.myeoru.coinexchangeexample.core.constant.CoinSymbols
import xyz.myeoru.coinexchangeexample.core.model.Ticker
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class BitThumbWebSocketManager(
    private val onConnectionState: ((state: WebSocketConnectionState) -> Unit)? = null,
    private val onReceiveTicker: ((ticker: Ticker) -> Unit)? = null
) {
    private lateinit var webSocket: WebSocket
    private val listener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Timber.v("WebSocket is open. [code=${response.code}, message=${response.message}]")
            onConnectionState?.let { it(WebSocketConnectionState.Open) }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Timber.v("WebSocket is closing. [code=$code, reason:$reason]")
            onConnectionState?.let { it(WebSocketConnectionState.Closing) }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Timber.v("WebSocket is closed. [code=$code, reason:$reason]")
            onConnectionState?.let { it(WebSocketConnectionState.Closed) }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            convertData(text)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Timber.e(t)
        }
    }

    fun openConnection() {
        val client = OkHttpClient.Builder().apply {
            readTimeout(30, TimeUnit.SECONDS)
            writeTimeout(30, TimeUnit.SECONDS)
            pingInterval(30, TimeUnit.SECONDS)
        }.build()
        val request = Request.Builder()
            .url("wss://pubwss.bithumb.com/pub/ws")
            .build()
        webSocket = client.newWebSocket(request, listener)
    }

    fun closeConnection() {
        webSocket.close(1000, "Goodbye, WebSocket!")
    }

    fun requestTicker(symbols: List<String>, currencyUnit: String = "KRW") {
        val reqMap = mapOf(
            "type" to "ticker",
            "symbols" to symbols.map { "${it}_$currencyUnit" },
            "tickTypes" to listOf("24H")
        )
        try {
            val jsonObject = JSONObject(reqMap)
            val json = jsonObject.toString()
            webSocket.send(json)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun convertData(text: String) {
        try {
            val jsonObject = JSONObject(text)
            if (jsonObject.has("type")) {
                val type = jsonObject.getString("type")
                when (type) {
                    "ticker" -> {
                        val content = jsonObject.getJSONObject("content")
                        val ticker = mapperToTicker(content)
                        onReceiveTicker?.let { it(ticker) }
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, text)
        }
    }

    private fun mapperToTicker(content: JSONObject): Ticker {
        val symbolSplit = content.getString("symbol").split("_")
        val coinSymbols = CoinSymbols.entries.find { it.name == symbolSplit[0] } ?: throw Exception(
            "CoinSymbol not found."
        )
        val currencyUnit = symbolSplit[1]
        val tickType = content.getString("tickType")
        val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        val date = LocalDate.parse(content.getString("date"), dateFormatter)
        val timeFormatter = DateTimeFormatter.ofPattern("HHmmss")
        val time = LocalTime.parse(content.getString("time"), timeFormatter)
        val openPrice = content.getString("openPrice").toDouble()
        val closePrice = content.getString("closePrice").toDouble()
        val lowPrice = content.getString("lowPrice").toDouble()
        val highPrice = content.getString("highPrice").toDouble()
        val value = content.getString("value").toDouble()
        val volume = content.getString("volume").toDouble()
        val sellVolume = content.getString("sellVolume").toDouble()
        val buyVolume = content.getString("buyVolume").toDouble()
        val prevClosePrice = content.getString("prevClosePrice").toDouble()
        val chgRate = content.getString("chgRate").toDouble()
        val chgAmt = content.getString("chgAmt").toDouble()
        val volumePower = content.getString("volumePower").toDouble()

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
}

@Composable
fun rememberBitThumbSocketManager(
    onConnectionState: ((state: WebSocketConnectionState) -> Unit)? = null,
    onReceiveTicker: ((ticker: Ticker) -> Unit)? = null
): BitThumbWebSocketManager {
    val bitThumbWebSocketManager = remember {
        BitThumbWebSocketManager(onConnectionState, onReceiveTicker)
    }
    return bitThumbWebSocketManager
}