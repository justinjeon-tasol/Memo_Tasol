package com.fileshare.app.ui.screens.lock

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.fileshare.app.viewmodel.LockViewModel
import kotlinx.coroutines.launch

@Composable
fun LockScreen(
    lockViewModel: LockViewModel,
    onUnlocked: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var pin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val failedAttempts by lockViewModel.failedAttempts.collectAsState()
    val isLocked by lockViewModel.isLocked.collectAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(Modifier.height(32.dp))
            
            Text(
                text = "PIN을 입력하세요",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(Modifier.height(8.dp))
            
            if (failedAttempts > 0) {
                Text(
                    text = "실패 횟수: $failedAttempts/5",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            Spacer(Modifier.height(32.dp))
            
            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= 4) pin = it },
                label = { Text("PIN (4자리)") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                enabled = !isLocked,
                modifier = Modifier.width(200.dp)
            )
            
            errorMessage?.let {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            Spacer(Modifier.height(24.dp))
            
            Button(
                onClick = {
                    scope.launch {
                        if (pin.length == 4) {
                            val isCorrect = lockViewModel.verifyPin(pin)
                            if (isCorrect) {
                                onUnlocked()
                            } else {
                                errorMessage = "잘못된 PIN입니다"
                                pin = ""
                            }
                        } else {
                            errorMessage = "PIN은 4자리여야 합니다"
                        }
                    }
                },
                enabled = !isLocked && pin.length == 4,
                modifier = Modifier.width(200.dp)
            ) {
                Text("확인")
            }
            
            if (isLocked) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "시도 횟수를 초과했습니다.\n잠시 후 다시 시도하세요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun PinSetupScreen(
    lockViewModel: LockViewModel,
    onSetupComplete: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(1) } // 1: enter PIN, 2: confirm PIN
    val pinSetupState by lockViewModel.pinSetupState.collectAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.LockOpen,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(Modifier.height(32.dp))
            
            Text(
                text = if (step == 1) "PIN을 설정하세요 (4자리)" else "PIN을 다시 입력하세요",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(Modifier.height(32.dp))
            
            OutlinedTextField(
                value = if (step == 1) pin else confirmPin,
                onValueChange = {
                    if (it.length <= 4) {
                        if (step == 1) pin = it else confirmPin = it
                    }
                },
                label = { Text("PIN (4자리)") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                modifier = Modifier.width(200.dp)
            )
            
            Spacer(Modifier.height(24.dp))
            
            Button(
                onClick = {
                    if (step == 1) {
                        if (pin.length == 4) {
                            step = 2
                        }
                    } else {
                        lockViewModel.setupPin(pin, confirmPin)
                    }
                },
                enabled = if (step == 1) pin.length == 4 else confirmPin.length == 4,
                modifier = Modifier.width(200.dp)
            ) {
                Text(if (step == 1) "다음" else "완료")
            }
            
            // Handle PIN setup state
            LaunchedEffect(pinSetupState) {
                when (pinSetupState) {
                    is com.fileshare.app.viewmodel.PinSetupState.Success -> {
                        lockViewModel.resetPinSetupState()
                        onSetupComplete()
                    }
                    is com.fileshare.app.viewmodel.PinSetupState.Error -> {
                        // Show error and reset
                        step = 1
                        pin = ""
                        confirmPin = ""
                    }
                    else -> {}
                }
            }
        }
    }
}
