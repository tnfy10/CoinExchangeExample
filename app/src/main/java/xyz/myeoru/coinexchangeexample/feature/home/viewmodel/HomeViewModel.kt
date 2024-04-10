package xyz.myeoru.coinexchangeexample.feature.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.launch
import timber.log.Timber
import xyz.myeoru.coinexchangeexample.core.constant.CoinChangeType
import xyz.myeoru.coinexchangeexample.core.constant.CoinSymbols
import xyz.myeoru.coinexchangeexample.core.model.Ticker
import xyz.myeoru.coinexchangeexample.data.repository.CoinInfoRepository
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val coinInfoRepository: CoinInfoRepository
) : ViewModel() {
    private val symbols = CoinSymbols.entries.map { it.name }

    private val _coinMapState = MutableStateFlow<Map<String, Ticker>>(emptyMap())
    val coinMapState = _coinMapState.asStateFlow()

    private val _coinChangeMapState =
        MutableStateFlow(CoinSymbols.entries.associate { Pair(it.name, CoinChangeType.None) })
    val coinChangeMapState = _coinChangeMapState.asStateFlow()

    private var receiveCoinCurrentPriceJob: Job? = null

    override fun onCleared() {
        stopReceiveCoinCurrentPrice()
        super.onCleared()
    }

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

    fun startReceiveCoinCurrentPrice() {
        if (receiveCoinCurrentPriceJob?.isActive == true || receiveCoinCurrentPriceJob != null) return

        receiveCoinCurrentPriceJob = viewModelScope.launch {
            coinInfoRepository.receiveCoinCurrentPrice(
                symbols = symbols,
                currencyUnit = "KRW",
                requestIntervalMs = 10
            ).catch { error ->
                Timber.e(error)
            }.onEach {
                updateCoinChangeMapState(it)
            }.collect { ticker ->
                val copyMap = coinMapState.value.toMutableMap()
                copyMap[ticker.symbol] = ticker
                _coinMapState.emit(copyMap)
            }
        }
    }

    fun stopReceiveCoinCurrentPrice() {
        receiveCoinCurrentPriceJob?.cancel()
        receiveCoinCurrentPriceJob = null
    }

    private fun updateCoinChangeMapState(newTicker: Ticker) {
        viewModelScope.launch {
            val oldTicker = coinMapState.value[newTicker.symbol]
            val oldClosePrice = oldTicker?.closePrice ?: return@launch
            val coinChangeType = when {
                oldClosePrice < newTicker.closePrice -> CoinChangeType.Up
                oldClosePrice > newTicker.closePrice -> CoinChangeType.Down
                else -> CoinChangeType.None
            }

            if (coinChangeType != CoinChangeType.None) {
                var newCoinChangeMap = coinChangeMapState.value.toMutableMap()
                newCoinChangeMap[newTicker.symbol] = coinChangeType
                _coinChangeMapState.emit(newCoinChangeMap)
                delay(150)
                newCoinChangeMap = coinChangeMapState.value.toMutableMap()
                newCoinChangeMap[newTicker.symbol] = CoinChangeType.None
                _coinChangeMapState.emit(newCoinChangeMap)
            }
        }
    }
}