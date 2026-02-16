package com.app.minder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
//import com.app.minder.data.local.database.MedDB
import com.app.minder.data.repository.AuthRepMock
import com.app.minder.domain.usecase.LoginUseCase
import com.app.minder.domain.usecase.RegisterUseCase
import com.app.minder.presentation.auth.AuthViewModel
import com.app.minder.presentation.navigation.Screen
import com.app.minder.presentation.navigation.NavGraph
import com.app.minder.presentation.theme.MedTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val database = MedDB.getDB(applicationContext)
        val authRepository = AuthRepMock()
        val loginUseCase = LoginUseCase(authRepository)
        val registerUseCase = RegisterUseCase(authRepository)

        setContent {
            MedTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    var startDestination by remember { mutableStateOf<String?>(null) }

                    // Проверяем, авторизован ли пользователь
                    LaunchedEffect(Unit) {
                        launch {
                            val user = authRepository.getCurrentUser()
                            startDestination = if (user.first()?.id != null) {
                                Screen.Home.route
                            } else {
                                Screen.Auth.route
                            }
                        }
                    }

                    startDestination?.let { destination ->
                        val authViewModel = AuthViewModel(loginUseCase, registerUseCase)

                        NavGraph(
                            navController = navController,
                            authViewModel = authViewModel,
                            startDestination = destination
                        )
                    }
                }
            }
        }
    }
}