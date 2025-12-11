package com.fileshare.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fileshare.app.data.remote.UserDto
import com.fileshare.app.data.remote.UserRole
import com.fileshare.app.viewmodel.UserManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListScreen(
    viewModel: UserManagementViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAddUser: () -> Unit
) {
    val users by viewModel.users.collectAsState()
    var showPasswordResetDialog by remember { mutableStateOf<UserDto?>(null) }
    
    // 화면 진입 시 데이터 로드
    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("사용자 관리") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "뒤로")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadUsers() }) {
                        Icon(Icons.Default.Refresh, "새로고침")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddUser) {
                Icon(Icons.Default.Add, "사용자 추가")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (viewModel.error != null) {
                // 에러 메시지 표시
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = viewModel.error ?: "알 수 없는 오류",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { viewModel.loadUsers() }) {
                        Text("다시 시도")
                    }
                }
            } else if (users.isEmpty()) {
                // 빈 목록 표시
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("등록된 사용자가 없습니다.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(users) { user ->
                        ListItem(
                            headlineContent = { Text(user.username) },
                            supportingContent = { Text("Role: ${user.role} | Active: ${user.isActive}") },
                            leadingContent = {
                                Icon(
                                    if (user.role == UserRole.ADMIN) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                    null
                                )
                            },
                            trailingContent = {
                                IconButton(onClick = { showPasswordResetDialog = user }) {
                                    Icon(Icons.Default.LockReset, "비밀번호 초기화")
                                }
                            }
                        )
                        Divider()
                    }
                }
            }
        }

        // Password Reset Dialog
        showPasswordResetDialog?.let { user ->
            var newPassword by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showPasswordResetDialog = null },
                title = { Text("비밀번호 초기화: ${user.username}") },
                text = {
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("새 비밀번호") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newPassword.isNotBlank()) {
                                viewModel.resetPassword(user.id, newPassword) {
                                    showPasswordResetDialog = null
                                }
                            }
                        }
                    ) {
                        Text("변경")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPasswordResetDialog = null }) {
                        Text("취소")
                    }
                }
            )
        }
    }
}
