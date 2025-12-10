package com.fileshare.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fileshare.app.data.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    var loginState by mutableStateOf<LoginState>(LoginState.Idle)
        private set

    fun login(username: String, passwordHash: String) {
        viewModelScope.launch {
            loginState = LoginState.Loading
            repository.login(username, passwordHash).collect { result ->
                loginState = if (result.isSuccess) {
                    LoginState.Success
                } else {
                    LoginState.Error(result.exceptionOrNull()?.message ?: "Unknown Error")
                }
            }
        }
    }
    
    fun resetState() {
        loginState = LoginState.Idle
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}
