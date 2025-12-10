package com.fileshare.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fileshare.app.util.SecurityUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LockViewModel(application: Application) : AndroidViewModel(application) {
    
    private val context = application.applicationContext
    
    val isLockEnabled: StateFlow<Boolean> = SecurityUtils.isLockEnabled(context)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
    
    val isBiometricEnabled: StateFlow<Boolean> = SecurityUtils.isBiometricEnabled(context)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
    
    private val _pinSetupState = MutableStateFlow<PinSetupState>(PinSetupState.Idle)
    val pinSetupState: StateFlow<PinSetupState> = _pinSetupState.asStateFlow()
    
    private val _failedAttempts = MutableStateFlow(0)
    val failedAttempts: StateFlow<Int> = _failedAttempts.asStateFlow()
    
    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()
    
    fun setLockEnabled(enabled: Boolean) {
        viewModelScope.launch {
            SecurityUtils.setLockEnabled(context, enabled)
        }
    }
    
    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            SecurityUtils.setBiometricEnabled(context, enabled)
        }
    }
    
    fun setupPin(pin: String, confirmPin: String) {
        viewModelScope.launch {
            if (pin != confirmPin) {
                _pinSetupState.value = PinSetupState.Error("PIN이 일치하지 않습니다")
                return@launch
            }
            
            if (pin.length != 4) {
                _pinSetupState.value = PinSetupState.Error("PIN은 4자리여야 합니다")
                return@launch
            }
            
            try {
                val pinHash = SecurityUtils.hashPin(pin)
                SecurityUtils.savePinHash(context, pinHash)
                SecurityUtils.setLockEnabled(context, true)
                _pinSetupState.value = PinSetupState.Success
            } catch (e: Exception) {
                _pinSetupState.value = PinSetupState.Error("PIN 설정 실패")
            }
        }
    }
    
    suspend fun verifyPin(pin: String): Boolean {
        var savedHash: String? = null
        SecurityUtils.getPinHash(context).first().let { savedHash = it }
        
        val inputHash = SecurityUtils.hashPin(pin)
        val isCorrect = savedHash == inputHash
        
        if (!isCorrect) {
            _failedAttempts.value += 1
            if (_failedAttempts.value >= 5) {
                _isLocked.value = true
            }
        } else {
            _failedAttempts.value = 0
            _isLocked.value = false
        }
        
        return isCorrect
    }
    
    fun resetPinSetupState() {
        _pinSetupState.value = PinSetupState.Idle
    }
    
    fun resetFailedAttempts() {
        _failedAttempts.value = 0
        _isLocked.value = false
    }
    
    fun isBiometricAvailable(): Boolean {
        return SecurityUtils.isBiometricAvailable(context)
    }
}

sealed class PinSetupState {
    object Idle : PinSetupState()
    object Success : PinSetupState()
    data class Error(val message: String) : PinSetupState()
}
