package xyz.myeoru.coinexchangeexample.data.repository

import kotlinx.coroutines.flow.Flow
import xyz.myeoru.coinexchangeexample.core.model.Ticker

interface CoinInfoRepository {
    fun receiveCoinCurrentPrice(
        symbols: List<String>,
        currencyUnit: String
    ): Flow<Ticker>

    fun getAllCoinCurrentPrice(symbols: List<String>, currencyUnit: String): Flow<List<Ticker>>
}