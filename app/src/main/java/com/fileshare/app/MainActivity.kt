package com.fileshare.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fileshare.app.di.AppContainer
import com.fileshare.app.ui.navigation.Screen
import com.fileshare.app.ui.screens.add.AddEditDocumentScreen
import com.fileshare.app.ui.screens.detail.DocumentDetailScreen
import com.fileshare.app.ui.screens.lock.LockScreen
import com.fileshare.app.ui.screens.main.MainScreen
import com.fileshare.app.ui.screens.settings.CategoryManagementScreen
import com.fileshare.app.ui.screens.settings.SettingsScreen
import com.fileshare.app.ui.screens.login.LoginScreen
import com.fileshare.app.ui.theme.FileShareAppTheme
import com.fileshare.app.viewmodel.CategoryViewModel
import com.fileshare.app.viewmodel.DocumentViewModel
import com.fileshare.app.viewmodel.LockViewModel
import com.fileshare.app.viewmodel.LoginState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            FileShareAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val documentViewModel: DocumentViewModel = viewModel()
                    val categoryViewModel: CategoryViewModel = viewModel()
                    val lockViewModel: LockViewModel = viewModel()
                    
                    val appContainer = (application as FileShareApplication).container
                    val tokenManager = appContainer.tokenManager
                    val authRepository = remember { com.fileshare.app.data.repository.AuthRepository(appContainer.apiService, tokenManager) }
                    val loginViewModel = remember { com.fileshare.app.viewmodel.LoginViewModel(authRepository) }
                    
                    val userManagementViewModel = remember { 
                        com.fileshare.app.viewmodel.UserManagementViewModel(authRepository) 
                    }

                    val isLoggedIn = authRepository.isLoggedIn()
                    val startDestination = if (isLoggedIn) Screen.Main.route else Screen.Login.route
                    
                    // Admin 여부 확인 (화면 전환 시마다 갱신될 수 있도록)
                    val isAdmin = authRepository.isAdmin()

                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {
                        composable(Screen.Login.route) {
                            val loginState = loginViewModel.loginState
                            
                            LoginScreen(
                                loginState = loginState, 
                                onLoginClick = { username, password ->
                                    loginViewModel.login(username, password)
                                }
                            )
                            
                            LaunchedEffect(loginState) {
                                if (loginState is LoginState.Success) {
                                    navController.navigate(Screen.Main.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }
                                }
                            }
                        }

                        composable(Screen.Lock.route) {
                            LockScreen(
                                lockViewModel = lockViewModel,
                                onUnlocked = {
                                    navController.navigate(Screen.Main.route) {
                                        popUpTo(Screen.Lock.route) { inclusive = true }
                                    }
                                }
                            )
                        }
                        
                        composable(Screen.Main.route) {
                            MainScreen(
                                documentViewModel = documentViewModel,
                                categoryViewModel = categoryViewModel,
                                onNavigateToDetail = { documentId ->
                                    navController.navigate(Screen.DocumentDetail.createRoute(documentId))
                                },
                                onNavigateToAdd = {
                                    navController.navigate(Screen.AddEditDocument.createRoute())
                                },
                                onNavigateToSettings = {
                                    navController.navigate(Screen.Settings.route)
                                }
                            )
                        }
                        
                        composable(Screen.DocumentDetail.route) { backStackEntry ->
                            val documentId = backStackEntry.arguments?.getString("documentId")
                            if (documentId != null) {
                                DocumentDetailScreen(
                                    documentId = documentId,
                                    documentViewModel = documentViewModel,
                                    categoryViewModel = categoryViewModel,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToEdit = { id ->
                                        navController.navigate(Screen.AddEditDocument.createRoute(id))
                                    },
                                    isAdmin = isAdmin // 관리자 여부 전달
                                )
                            }
                        }
                        
                        composable(Screen.AddEditDocument.route) { backStackEntry ->
                            val documentId = backStackEntry.arguments?.getString("documentId")
                            AddEditDocumentScreen(
                                documentId = documentId,
                                documentViewModel = documentViewModel,
                                categoryViewModel = categoryViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        
                        composable(Screen.Settings.route) {
                            SettingsScreen(
                                lockViewModel = lockViewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToCategoryManagement = {
                                    navController.navigate(Screen.CategoryManagement.route)
                                },
                                onNavigateToUserManagement = {
                                    navController.navigate(Screen.UserManagement.route)
                                },
                                onLogout = {
                                    authRepository.logout()
                                    loginViewModel.resetState()
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                isAdmin = isAdmin
                            )
                        }

                        composable(Screen.UserManagement.route) {
                            com.fileshare.app.ui.screens.settings.UserListScreen(
                                viewModel = userManagementViewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToAddUser = { navController.navigate(Screen.AddUser.route) }
                            )
                        }

                        composable(Screen.AddUser.route) {
                            com.fileshare.app.ui.screens.settings.AddUserScreen(
                                viewModel = userManagementViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        
                        composable(Screen.CategoryManagement.route) {
                            CategoryManagementScreen(
                                categoryViewModel = categoryViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
