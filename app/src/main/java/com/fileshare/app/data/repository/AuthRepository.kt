package com.fileshare.app.data.repository

import com.fileshare.app.data.local.TokenManager
import com.fileshare.app.data.remote.ApiService
import com.fileshare.app.data.remote.LoginRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AuthRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    fun login(username: String, passwordHash: String): Flow<Result<Unit>> = flow {
        try {
            val response = apiService.login(LoginRequest(username, passwordHash))
            if (response.isSuccessful && response.body() != null) {
                tokenManager.saveToken(response.body()!!.access_token, response.body()!!.role)
                emit(Result.success(Unit))
            } else {
                emit(Result.failure(Exception("Login failed: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    fun isLoggedIn(): Boolean {
        return tokenManager.getToken() != null
    }

    fun isAdmin(): Boolean {
        return tokenManager.getRole() == "ADMIN"
    }
    
    fun logout() {
        tokenManager.clearToken()
    }

    // User Management (Admin Only)
    fun getUsers() = flow {
        if (!isAdmin()) throw SecurityException("Not Admin (Role: ${tokenManager.getRole()})")
        val response = apiService.getUsers()
        if (response.isSuccessful) emit(response.body() ?: emptyList())
        else {
            val errorMsg = response.errorBody()?.string() ?: "Unknown error"
            throw Exception("Fetch failed: ${response.code()} - $errorMsg")
        }
    }

    suspend fun createUser(request: com.fileshare.app.data.remote.CreateUserDto): Result<Unit> {
        return try {
            val response = apiService.createUser(request)
            if (response.isSuccessful) Result.success(Unit)
            else {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception("Create failed: ${response.code()} - $errorMsg"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(id: String, request: com.fileshare.app.data.remote.UpdateUserDto): Result<Unit> {
        return try {
            val response = apiService.updateUser(id, request)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Failed to update user"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
