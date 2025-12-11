package com.fileshare.app.di

import android.content.Context
import com.fileshare.app.BuildConfig // BuildConfig import 추가
import com.fileshare.app.data.local.TokenManager
import com.fileshare.app.data.remote.ApiService
import com.fileshare.app.data.remote.AuthInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {
    // BuildConfig에서 API URL 읽기 (local.properties에 설정)
    private val baseUrl = BuildConfig.API_BASE_URL

    // TokenManager를 AppContainer가 관리 (Singleton 역할)
    val tokenManager = TokenManager(context)

    private val authInterceptor = AuthInterceptor(tokenManager)

    // 로깅 인터셉터 다시 활성화 (디버깅용)
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
         level = HttpLoggingInterceptor.Level.BODY
    }

    // Coil에서도 사용할 수 있도록 public으로 노출
    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor) // 토큰 자동 추가
        .addInterceptor(loggingInterceptor)
        .connectTimeout(5, TimeUnit.SECONDS) // 타임아웃 약간 늘림
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
