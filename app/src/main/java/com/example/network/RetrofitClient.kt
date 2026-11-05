package com.example.network

import com.example.config.AppConfig
import com.example.config.AppEnvironment
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    val BASE_URL: String
        get() = AppConfig.apiEndpoint

    val currentEnvironment: AppEnvironment
        get() = AppConfig.currentEnvironment

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    val retrofit: Retrofit
        get() = Retrofit.Builder()
            .baseUrl(AppConfig.activeDomain + "/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

    val apiService: Any
        get() = retrofit
}
