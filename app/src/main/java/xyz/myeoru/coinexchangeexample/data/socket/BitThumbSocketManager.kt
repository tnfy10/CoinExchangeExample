package xyz.myeoru.coinexchangeexample.data.socket

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import timber.log.Timber
import xyz.myeoru.coinexchangeexample.core.model.Ticker
import xyz.myeoru.coinexchangeexample.core.model.mapperToTicker
import xyz.myeoru.coinexchangeexample.data.di.OkHttpClientModule
import javax.inject.Inject

class BitThumbSocketManager @Inject constructor(
    @OkHttpClientModule.SocketOkHttpClient private val socketClient: OkHttpClient
) {
    private lateinit var webSocket: WebSocket

    private val _receiveTickerFlow = MutableSharedFlow<Ticker>(extraBufferCapacity = 1)
    val receiveTickerFlow = _receiveTickerFlow.asSharedFlow()

    enum class SubsType(val type: String) {
        Ticker(
            type = "ticker"
        ),
        Transaction(
            type = "transaction"
        ),
        OrderBookDepth(
            type = "orderbookdepth"
        ),
        OrderBookSnapshot(
            type = "orderbooksnapshot"
        )
    }

    fun connect(onState: ((state: SocketState) -> Unit)? = null) {
        val request = Request.Builder()
            .url("wss://pubwss.bithumb.com/pub/ws")
            .build()
        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Timber.v("BitThumbSocketManager onOpen")
                onState?.let { it(SocketState.Open(response)) }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Timber.v("BitThumbSocketManager onClosing [code:$code, reason:$reason]")
                onState?.let { it(SocketState.Closing(code, reason)) }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Timber.v("BitThumbSocketManager onClosed [code:$code, reason:$reason]")
                onState?.let { it(SocketState.Closed(code, reason)) }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val jsonObject = JSONObject(text)
                if (jsonObject.has("type")) {
                    val type = jsonObject.getString("type")
                    when (SubsType.entries.find { it.type == type }) {
                        SubsType.Ticker -> {
                            val content = jsonObject.getJSONObject("content")
                            val ticker = content.mapperToTicker()
                            _receiveTickerFlow.tryEmit(ticker)
                        }

                        SubsType.Transaction -> TODO()
                        SubsType.OrderBookDepth -> TODO()
                        SubsType.OrderBookSnapshot -> TODO()
                        null -> {}
                    }
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Timber.w(t, "receiveCoinCurrentPrice onFailure")
                onState?.let { it(SocketState.Failure(t, response)) }
            }
        }
        webSocket = socketClient.newWebSocket(request, listener)
    }

    fun close(code: Int, reason: String) {
        webSocket.close(code, reason)
    }

    /**
     * @property symbols 코인 기호 (예: BTC, XRP, ETH 등)
     * @property currencyUnit 통화 (예: KRW, USD 등)
     * @property tickTypes 틱 타입 (30M, 1H, 12H, 24H, MID)
     */
    fun requestSubsTicker(
        symbols: List<String>,
        currencyUnit: String,
        tickTypes: List<String>
    ) {
        val reqMap = mapOf(
            "type" to "ticker",
            "symbols" to symbols.map { "${it}_$currencyUnit" },
            "tickTypes" to tickTypes
        )
        val jsonObject = JSONObject(reqMap)
        val json = jsonObject.toString()
        webSocket.send(json)
    }
}