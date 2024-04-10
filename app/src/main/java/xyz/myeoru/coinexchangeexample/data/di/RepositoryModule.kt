package xyz.myeoru.coinexchangeexample.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import xyz.myeoru.coinexchangeexample.data.repository.CoinInfoRepository
import xyz.myeoru.coinexchangeexample.data.repository.CoinInfoRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindCoinInfoRepository(coinInfoRepositoryImpl: CoinInfoRepositoryImpl): CoinInfoRepository
}