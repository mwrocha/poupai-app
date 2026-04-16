package io.poupai.app.core.di

import io.poupai.app.data.remote.api.AuthApi
import io.poupai.app.data.remote.api.FinanceApi
import io.poupai.app.data.remote.api.InvestmentApi
import io.poupai.app.data.remote.api.TransactionApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // 10.0.2.2 = localhost do PC visto pelo emulador Android
    // Quando for para produção, trocar pela URL real da API
    private const val BASE_URL = "http://192.168.0.5:8080/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideTransactionApi(retrofit: Retrofit): TransactionApi =
        retrofit.create(TransactionApi::class.java)

    @Provides
    @Singleton
    fun provideFinanceApi(retrofit: Retrofit): FinanceApi =
        retrofit.create(FinanceApi::class.java)

    @Provides
    @Singleton
    fun provideInvestmentApi(retrofit: Retrofit): InvestmentApi =
        retrofit.create(InvestmentApi::class.java)
}