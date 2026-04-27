package com.app.minder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.navigation.compose.rememberNavController
import com.app.minder.data.local.database.MedDB
import com.app.minder.data.repository.AuthRepImpl
import com.app.minder.data.repository.MeasurementRepImpl
import com.app.minder.data.repository.MedicationRepImpl
import com.app.minder.data.repository.ProfileRepImpl
import com.app.minder.domain.usecase.CreateProfileUseCase
import com.app.minder.domain.usecase.DeleteMedUseCase
import com.app.minder.domain.usecase.GetCurrentProfileUseCase
import com.app.minder.domain.usecase.GetMeasurementAnalysisUseCase
import com.app.minder.domain.usecase.GetMedsUseCase
import com.app.minder.domain.usecase.GetTodayIntakesUseCase
import com.app.minder.domain.usecase.LoginUseCase
import com.app.minder.domain.usecase.RegisterUseCase
import com.app.minder.domain.usecase.SaveMeasurementUseCase
import com.app.minder.domain.usecase.SaveMedUseCase
import com.app.minder.domain.usecase.SwitchProfileUseCase
import com.app.minder.domain.usecase.TakeMedicationUseCase
import com.app.minder.presentation.auth.AuthViewModel
import com.app.minder.presentation.correlation.CorrelationViewModel
import com.app.minder.presentation.home.HomeViewModel
import com.app.minder.presentation.medList.MedListViewModel
import com.app.minder.presentation.navigation.Screen
import com.app.minder.presentation.navigation.NavGraph
import com.app.minder.presentation.profile.ProfileViewModel
import com.app.minder.presentation.theme.Background
import com.app.minder.presentation.theme.MedTheme
import com.app.minder.presentation.theme.OnContainer
import com.app.minder.util.notifications.NotificationScheduler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                scrim = Background.toArgb(),
                darkScrim = OnContainer.toArgb()
            )
        )
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
            intakeDao = database.medicationIntakeDao(),
            database = database
        )
        val profileRepository = ProfileRepImpl(
            profileDao = database.profileDao(),
            database = database
        )
        val measurementRepository = MeasurementRepImpl(
            measurementDao = database.measurementDao(),
            measurementGoalDao = database.measurementGoalDao(),
            measurementTypeDao = database.measurementTypeDao(),
            database = database
        )

        val notificationScheduler = NotificationScheduler(applicationContext, medicationRepository)

        val getTodayIntakesUseCase = GetTodayIntakesUseCase(medicationRepository, profileRepository)
        val getMedsUseCase = GetMedsUseCase(medicationRepository, profileRepository)
        val deleteMedUseCase = DeleteMedUseCase(medicationRepository, notificationScheduler)
        val getCurrentProfileUseCase = GetCurrentProfileUseCase(profileRepository)
        val saveMedUseCase = SaveMedUseCase(medicationRepository, notificationScheduler)
        val takeMedicationUseCase = TakeMedicationUseCase(medicationRepository, notificationScheduler)
        val switchProfileUseCase = SwitchProfileUseCase(profileRepository)
        val createProfileUseCase = CreateProfileUseCase(profileRepository)
        val getMeasurementAnalysisUseCase = GetMeasurementAnalysisUseCase(measurementRepository)
        val saveMeasurementUseCase = SaveMeasurementUseCase(measurementRepository)


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
                        val profileViewModel = ProfileViewModel(
                            getCurrentProfileUseCase,
                            switchProfileUseCase,
                            createProfileUseCase,
                            profileRepository,
                            authRepository
                        )
                        val correlationViewModel = CorrelationViewModel(
                            measurementRepository,
                            medicationRepository,
                            profileRepository
                        )

                        NavGraph(
                            navController = navController,
                            authViewModel = authViewModel,
                            homeViewModel = homeViewModel,
                            medListViewModel = medListViewModel,
                            profileViewModel = profileViewModel,
                            correlationViewModel = correlationViewModel,
                            startDestination = destination,
                            saveMedicationUseCase = saveMedUseCase,
                            deleteMedicationUseCase = deleteMedUseCase,
                            getCurrentProfileUseCase = getCurrentProfileUseCase,
                            takeMedicationUseCase = takeMedicationUseCase,
                            medicationRep = medicationRepository,
                            measurementRep = measurementRepository,
                            profileRep = profileRepository,
                            getMeasurementAnalysisUseCase = getMeasurementAnalysisUseCase,
                            saveMeasurementUseCase = saveMeasurementUseCase
                        )
                    }
                }
            }
        }
    }
}