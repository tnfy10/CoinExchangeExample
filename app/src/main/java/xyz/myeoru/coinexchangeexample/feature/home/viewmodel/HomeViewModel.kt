package xyz.myeoru.coinexchangeexample.feature.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
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
    private val _coinMapState = MutableStateFlow<Map<CoinSymbols, Ticker>>(emptyMap())
    val coinMapState = _coinMapState.asStateFlow()

    private val _coinChangeMapState = MutableStateFlow(CoinSymbols.entries.associateWith { CoinChangeType.None })
    val coinChangeMapState = _coinChangeMapState.asStateFlow()

    init {
        receiveCoinCurrentPrice()
    }

    private fun receiveCoinCurrentPrice() {
        viewModelScope.launch {
            val symbols = CoinSymbols.entries.map { it.name }
            coinInfoRepository.receiveCoinCurrentPrice(
                symbols = symbols,
                currencyUnit = "KRW",
                requestIntervalMs = 100
            ).catch { error ->
                Timber.e(error)
            }.onEach {
                updateCoinChangeMapState(it)
            }.collect { ticker ->
                val copyMap = coinMapState.value.toMutableMap()
                copyMap[ticker.coinSymbols] = ticker
                _coinMapState.emit(copyMap)
            }
        }
    }

    private fun updateCoinChangeMapState(newTicker: Ticker) {
        viewModelScope.launch {
            val oldTicker = coinMapState.value[newTicker.coinSymbols]
            val oldClosePrice = oldTicker?.closePrice ?: return@launch
            val coinChangeType = when {
                oldClosePrice < newTicker.closePrice -> CoinChangeType.Up
                oldClosePrice > newTicker.closePrice -> CoinChangeType.Down
                else -> CoinChangeType.None
            }

            if (coinChangeType != CoinChangeType.None) {
                var newCoinChangeMap = coinChangeMapState.value.toMutableMap()
                newCoinChangeMap[newTicker.coinSymbols] = coinChangeType
                _coinChangeMapState.emit(newCoinChangeMap)
                delay(300)
                newCoinChangeMap = coinChangeMapState.value.toMutableMap()
                newCoinChangeMap[newTicker.coinSymbols] = CoinChangeType.None
                _coinChangeMapState.emit(newCoinChangeMap)
            }
        }
    }
}