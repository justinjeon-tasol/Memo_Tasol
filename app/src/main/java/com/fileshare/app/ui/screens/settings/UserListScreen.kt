package com.fileshare.app.ui.screens.settings

import androidx.compose.foundation.clickable
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("사용자 관리") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "뒤로")
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
        if (viewModel.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
