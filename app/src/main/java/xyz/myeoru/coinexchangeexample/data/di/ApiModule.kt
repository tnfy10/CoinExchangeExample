package xyz.myeoru.coinexchangeexample.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import xyz.myeoru.coinexchangeexample.data.api.BitThumbApi

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    fun provideBitThumbApi(@RetrofitModule.BitThumbRetrofit retrofit: Retrofit): BitThumbApi =
        retrofit.create(BitThumbApi::class.java)
}
