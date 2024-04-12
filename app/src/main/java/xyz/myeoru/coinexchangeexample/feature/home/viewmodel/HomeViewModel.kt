package xyz.myeoru.coinexchangeexample.feature.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.launch
import timber.log.Timber
import xyz.myeoru.coinexchangeexample.core.constant.CoinSymbols
import xyz.myeoru.coinexchangeexample.core.model.Ticker
import xyz.myeoru.coinexchangeexample.data.repository.CoinInfoRepository
import xyz.myeoru.coinexchangeexample.data.socket.BitThumbSocketManager
import xyz.myeoru.coinexchangeexample.data.socket.SocketState
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val coinInfoRepository: CoinInfoRepository,
    private val bitThumbSocketManager: BitThumbSocketManager
) : ViewModel() {
    private val symbols = CoinSymbols.entries.map { it.name }

    private val _coinMapState = MutableStateFlow<Map<String, Ticker>>(emptyMap())
    val coinMapState = _coinMapState.asStateFlow()

    fun fetchCoinCurrentPrice() {
        viewModelScope.launch {
            coinInfoRepository.getAllCoinCurrentPrice(
                symbols = symbols,
                currencyUnit = "KRW"
            ).retry(3) { error ->
                Timber.e(error)
                delay(1000L)
                true
            }.catch { error ->
                Timber.e(error)
            }.collectLatest { tickers ->
                val coinMap = tickers.associateBy { it.symbol }
                _coinMapState.emit(coinMap)
            }
        }
    }

    fun connectSocket() {
        bitThumbSocketManager.connect(
            onState = { state ->
                if (state is SocketState.Open) {
                    startReceiveCoinCurrentPrice()
                }
            }
        )
    }

    fun closeSocket() {
        bitThumbSocketManager.close(1000, "Socket closed.")
    }

    private fun startReceiveCoinCurrentPrice() {
        viewModelScope.launch(Dispatchers.IO) {
            bitThumbSocketManager.requestSubsTicker(
                symbols = symbols,
                currencyUnit = "KRW",
                tickTypes = listOf("MID")
            )
            bitThumbSocketManager.receiveTickerFlow.collect { ticker ->
                val copyMap = coinMapState.value.toMutableMap()
                copyMap[ticker.symbol] = ticker
                _coinMapState.emit(copyMap)
            }
        }
    }
}