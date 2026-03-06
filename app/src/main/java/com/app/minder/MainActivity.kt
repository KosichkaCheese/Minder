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
import com.app.minder.data.local.database.MedDB
import com.app.minder.data.repository.AuthRepImpl
import com.app.minder.data.repository.MedicationRepImpl
import com.app.minder.data.repository.ProfileRepImpl
import com.app.minder.domain.usecase.GetMedsUseCase
import com.app.minder.domain.usecase.GetTodayIntakesUseCase
import com.app.minder.domain.usecase.LoginUseCase
import com.app.minder.domain.usecase.RegisterUseCase
import com.app.minder.presentation.auth.AuthViewModel
import com.app.minder.presentation.home.HomeViewModel
import com.app.minder.presentation.medList.MedListViewModel
import com.app.minder.presentation.navigation.Screen
import com.app.minder.presentation.navigation.NavGraph
import com.app.minder.presentation.theme.MedTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = MedDB.getDB(applicationContext)
        val authRepository = AuthRepImpl(
            database.userDao(),
            database.profileDao(),
            database,
            applicationContext
        )
        val loginUseCase = LoginUseCase(authRepository)
        val registerUseCase = RegisterUseCase(authRepository)
        val medicationRepository = MedicationRepImpl(
            medicationDao = database.medicationDao(),
            scheduleDao = database.medicationScheduleDao(),
            intakeDao = database.medicationIntakeDao()
        )
        val profileRepository = ProfileRepImpl(
            profileDao = database.profileDao()
        )
        val getTodayIntakesUseCase = GetTodayIntakesUseCase(medicationRepository, profileRepository)
        val getMedsUseCase = GetMedsUseCase(medicationRepository, profileRepository)

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
                        val homeViewModel = HomeViewModel(getTodayIntakesUseCase)
                        val medListViewModel = MedListViewModel(getMedsUseCase)

                        NavGraph(
                            navController = navController,
                            authViewModel = authViewModel,
                            homeViewModel = homeViewModel,
                            medListViewModel = medListViewModel,
                            startDestination = destination
                        )
                    }
                }
            }
        }
    }
}