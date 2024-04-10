package xyz.myeoru.coinexchangeexample.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import timber.log.Timber
import xyz.myeoru.coinexchangeexample.core.model.Ticker
import xyz.myeoru.coinexchangeexample.core.model.mapperToTicker
import xyz.myeoru.coinexchangeexample.data.di.NetworkModule
import javax.inject.Inject

class CoinInfoRepositoryImpl @Inject constructor(
    @NetworkModule.SocketOkHttpClient private val client: OkHttpClient
) : CoinInfoRepository {
    private val request = Request.Builder()
        .url("wss://pubwss.bithumb.com/pub/ws")
        .build()

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
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                isConnected = false
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
        var webSocket = client.newWebSocket(request, listener)
        launch {
            while (true) {
                if (isConnected) {
                    val reqMap = mapOf(
                        "type" to "ticker",
                        "symbols" to symbols.map { "${it}_$currencyUnit" },
                        "tickTypes" to listOf("24H")
                    )
                    val jsonObject = JSONObject(reqMap)
                    val json = jsonObject.toString()
                    webSocket.send(json)
                } else {
                    webSocket = client.newWebSocket(request, listener)
                }
                delay(requestIntervalMs)
            }
        }
        awaitClose { webSocket.close(1000, "Close receive coin current price web socket.") }
    }.flowOn(Dispatchers.IO)
}