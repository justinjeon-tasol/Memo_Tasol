package com.fileshare.app.data.local

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_USER_ROLE = "user_role" // Role 저장 키 추가
        private const val KEY_USER_ID = "user_id"
    }

    // Role 파라미터 추가
    fun saveToken(token: String, role: String?) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, token)
            .putString(KEY_USER_ROLE, role)
            .apply()
    }

    fun getToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    // Role 조회 메서드 추가
    fun getRole(): String? {
        return prefs.getString(KEY_USER_ROLE, null)
    }

    fun clearToken() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_USER_ROLE)
            .apply()
    }
}
