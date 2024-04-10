package xyz.myeoru.coinexchangeexample.data.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Qualifier

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class BitThumbRetrofit

    @Provides
    fun provideMoshiConverterFactory(): MoshiConverterFactory =
        MoshiConverterFactory.create(Moshi.Builder().apply {
            addLast(KotlinJsonAdapterFactory())
        }.build())

    @BitThumbRetrofit
    @Provides
    fun provideBitThumbRetrofit(
        moshiConverterFactory: MoshiConverterFactory,
        @OkHttpClientModule.RestApiOkHttpClient okHttpClient: OkHttpClient
    ): Retrofit = Retrofit.Builder().apply {
        baseUrl("https://api.bithumb.com")
        addConverterFactory(moshiConverterFactory)
        client(okHttpClient)
    }.build()
}