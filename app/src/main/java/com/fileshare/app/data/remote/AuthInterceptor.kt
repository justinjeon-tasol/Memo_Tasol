package com.fileshare.app.data.remote

import com.fileshare.app.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = tokenManager.getToken()

        // 토큰이 없는 경우 원래 요청 그대로 진행
        if (token == null) {
            return chain.proceed(originalRequest)
        }

        // 토큰이 있으면 헤더에 추가
        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        val response = chain.proceed(newRequest)
        
        // 401 Unauthorized 응답 시 토큰 삭제 (만료된 토큰)
        if (response.code == 401) {
            android.util.Log.w("AuthInterceptor", "Received 401, clearing token")
            tokenManager.clearToken()
        }

        return response
    }
}
