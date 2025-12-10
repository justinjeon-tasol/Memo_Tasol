package com.fileshare.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fileshare.app.data.remote.CreateUserDto
import com.fileshare.app.data.remote.UpdateUserDto
import com.fileshare.app.data.remote.UserDto
import com.fileshare.app.data.remote.UserRole
import com.fileshare.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserManagementViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _users = MutableStateFlow<List<UserDto>>(emptyList())
    val users = _users.asStateFlow()

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                authRepository.getUsers().collect {
                    _users.value = it
                }
            } catch (e: Exception) {
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun createUser(username: String, password: String, role: UserRole, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
             try {
                val result = authRepository.createUser(
                    CreateUserDto(username = username, password = password, role = role)
                )
                if (result.isSuccess) {
                    loadUsers()
                    onSuccess()
                } else {
                    error = result.exceptionOrNull()?.message
                }
            } finally {
                isLoading = false
            }
        }
    }

    fun resetPassword(userId: String, newPassword: String, onSuccess: () -> Unit) {
         viewModelScope.launch {
            isLoading = true
             try {
                val result = authRepository.updateUser(
                    userId,
                    UpdateUserDto(password = newPassword)
                )
                if (result.isSuccess) {
                    onSuccess()
                } else {
                    error = result.exceptionOrNull()?.message
                }
            } finally {
                isLoading = false
            }
        }
    }
}
