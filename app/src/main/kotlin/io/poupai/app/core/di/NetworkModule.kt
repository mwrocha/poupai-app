package io.poupai.app.core.di

import android.os.Build
import io.poupai.app.core.network.AuthInterceptor
import io.poupai.app.data.remote.api.AuthApi
import io.poupai.app.data.remote.api.FinanceApi
import io.poupai.app.data.remote.api.GamificationApi
import io.poupai.app.data.remote.api.GoalApi
import io.poupai.app.data.remote.api.InvestmentApi
import io.poupai.app.data.remote.api.TagApi
import io.poupai.app.data.remote.api.TransactionApi
import io.poupai.app.data.remote.api.UploadApi
import io.poupai.app.data.remote.api.UserApi
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

    private const val EMULATOR_URL = "http://10.0.2.2:8080/"
    private const val DEVICE_URL   = "http://192.168.0.4:8080/"

    private val BASE_URL: String
        get() = if (isEmulator()) EMULATOR_URL else DEVICE_URL

    private fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.FINGERPRINT.contains("emulator")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.BRAND.startsWith("generic")
                || Build.DEVICE.startsWith("generic")
                || Build.PRODUCT.contains("sdk")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu"))
    }

    @Provides @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @Provides @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi = retrofit.create(UserApi::class.java)

    @Provides @Singleton
    fun provideUploadApi(retrofit: Retrofit): UploadApi = retrofit.create(UploadApi::class.java)

    @Provides @Singleton
    fun provideTransactionApi(retrofit: Retrofit): TransactionApi = retrofit.create(TransactionApi::class.java)

    @Provides @Singleton
    fun provideFinanceApi(retrofit: Retrofit): FinanceApi = retrofit.create(FinanceApi::class.java)

    @Provides @Singleton
    fun provideInvestmentApi(retrofit: Retrofit): InvestmentApi = retrofit.create(InvestmentApi::class.java)

    @Provides @Singleton
    fun provideGoalApi(retrofit: Retrofit): GoalApi = retrofit.create(GoalApi::class.java)

    @Provides @Singleton
    fun provideTagApi(retrofit: Retrofit): TagApi = retrofit.create(TagApi::class.java)

    @Provides @Singleton
    fun provideGamificationApi(retrofit: Retrofit): GamificationApi = retrofit.create(GamificationApi::class.java)
}