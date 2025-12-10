package com.fileshare.app.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fileshare.app.viewmodel.LockViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    lockViewModel: LockViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCategoryManagement: () -> Unit = {},
    onNavigateToUserManagement: () -> Unit = {}, // 콜백 추가
    onLogout: () -> Unit,
    isAdmin: Boolean = false // Admin 여부 파라미터 추가
) {
    val isLockEnabled by lockViewModel.isLockEnabled.collectAsState()
    val isBiometricEnabled by lockViewModel.isBiometricEnabled.collectAsState()
    val isBiometricAvailable = lockViewModel.isBiometricAvailable()
    
    var showPinSetup by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("설정") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "뒤로")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Admin Section (Only visible to Admin)
            if (isAdmin) {
                Text(
                    text = "관리자",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )

                ListItem(
                    headlineContent = { Text("사용자 관리") },
                    supportingContent = { Text("사용자 추가 및 비밀번호 초기화") },
                    leadingContent = {
                        Icon(Icons.Default.People, null)
                    },
                    trailingContent = {
                        Icon(Icons.Default.ChevronRight, null)
                    },
                    modifier = Modifier.clickable { onNavigateToUserManagement() }
                )
                
                Divider()
            }

            // Category Section
            Text(
                text = "카테고리",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
            
            ListItem(
                headlineContent = { Text("카테고리 관리") },
                supportingContent = { Text("카테고리 추가, 수정, 삭제 및 순서 변경") },
                leadingContent = {
                    Icon(Icons.Default.Category, null)
                },
                trailingContent = {
                    Icon(Icons.Default.ChevronRight, null)
                },
                modifier = Modifier.clickable { onNavigateToCategoryManagement() }
            )
            
            Divider()
            
            // App Lock Section
            Text(
                text = "보안",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
            
            ListItem(
                headlineContent = { Text("앱 잠금") },
                supportingContent = { 
                    Text("민감한 정보 보호를 위해 앱 잠금을 설정하세요") 
                },
                leadingContent = {
                    Icon(Icons.Default.Lock, null)
                },
                trailingContent = {
                    Switch(
                        checked = isLockEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                showPinSetup = true
                            } else {
                                lockViewModel.setLockEnabled(false)
                            }
                        }
                    )
                }
            )
            
            if (isLockEnabled) {
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                
                ListItem(
                    headlineContent = { Text("PIN 변경") },
                    leadingContent = {
                        Icon(Icons.Default.Key, null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp)
                )
                
                if (isBiometricAvailable) {
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    
                    ListItem(
                        headlineContent = { Text("생체인증 사용") },
                        supportingContent = { Text("지문 또는 얼굴 인식으로 잠금 해제") },
                        leadingContent = {
                            Icon(Icons.Default.Fingerprint, null)
                        },
                        trailingContent = {
                            Switch(
                                checked = isBiometricEnabled,
                                onCheckedChange = { enabled ->
                                    lockViewModel.setBiometricEnabled(enabled)
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp)
                    )
                }
            }
            
            Divider()
            
            // App Info Section
            Spacer(Modifier.height(16.dp))
            
            Text(
                text = "앱 정보",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
            
            ListItem(
                headlineContent = { Text("버전") },
                supportingContent = { Text("1.0.0") },
                leadingContent = {
                    Icon(Icons.Default.Info, null)
                }
            )
            
            Divider()
            
            ListItem(
                headlineContent = { Text("개발자") },
                supportingContent = { Text("파일저장앱 팀") },
                leadingContent = {
                    Icon(Icons.Default.Code, null)
                }
            )

            Divider()

            ListItem(
                headlineContent = { Text("로그아웃") },
                leadingContent = {
                    Icon(Icons.Default.ExitToApp, null)
                },
                modifier = Modifier.clickable { onLogout() }
            )
        }
    }
    
    // PIN Setup Dialog
    if (showPinSetup) {
        AlertDialog(
            onDismissRequest = { showPinSetup = false },
            title = { Text("PIN 설정") },
            text = {
                Column {
                    Text("앱 보안을 위해 4자리 PIN을 설정해주세요.")
                    Spacer(Modifier.height(16.dp))
                    Text("PIN 설정 화면은 별도 화면으로 구현됩니다.", 
                        style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Navigate to PIN setup screen
                        showPinSetup = false
                        lockViewModel.setLockEnabled(true)
                    }
                ) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinSetup = false }) {
                    Text("취소")
                }
            }
        )
    }
}
