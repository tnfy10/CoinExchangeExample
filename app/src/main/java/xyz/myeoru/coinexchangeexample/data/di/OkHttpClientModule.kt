package xyz.myeoru.coinexchangeexample.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier

@Module
@InstallIn(SingletonComponent::class)
object OkHttpClientModule {

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class SocketOkHttpClient

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class RestApiOkHttpClient

    @Provides
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    @SocketOkHttpClient
    @Provides
    fun provideSocketOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder().apply {
        readTimeout(30, TimeUnit.SECONDS)
        writeTimeout(30, TimeUnit.SECONDS)
        pingInterval(30, TimeUnit.SECONDS)
        addInterceptor(loggingInterceptor)
    }.build()

    @RestApiOkHttpClient
    @Provides
    fun provideRestApiOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder().apply {
        readTimeout(30, TimeUnit.SECONDS)
        writeTimeout(30, TimeUnit.SECONDS)
        addInterceptor(loggingInterceptor)
    }.build()
}