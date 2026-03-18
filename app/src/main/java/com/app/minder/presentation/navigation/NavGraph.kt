package com.app.minder.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.app.minder.data.repository.MedicationRepository
import com.app.minder.domain.usecase.DeleteMedUseCase
import com.app.minder.domain.usecase.GetCurrentProfileUseCase
import com.app.minder.domain.usecase.SaveMedUseCase
import com.app.minder.domain.usecase.TakeMedicationUseCase
import com.app.minder.presentation.auth.AuthScreen
import com.app.minder.presentation.auth.AuthViewModel
import com.app.minder.presentation.home.HomeViewModel
import com.app.minder.presentation.main.MainScreen
import com.app.minder.presentation.medList.MedListViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel,
    medListViewModel: MedListViewModel,
    saveMedicationUseCase: SaveMedUseCase,
    deleteMedicationUseCase: DeleteMedUseCase,
    getCurrentProfileUseCase: GetCurrentProfileUseCase,
    takeMedicationUseCase: TakeMedicationUseCase,
    medicationRep: MedicationRepository,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Auth.route) {
            AuthScreen(
                viewModel = authViewModel,
                onAuthSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            MainScreen(
                homeViewModel = homeViewModel,
                medListViewModel = medListViewModel,
                saveMedicationUseCase = saveMedicationUseCase,
                deleteMedicationUseCase = deleteMedicationUseCase,
                getCurrentProfileUseCase = getCurrentProfileUseCase,
                takeMedicationUseCase = takeMedicationUseCase,
                medicationRep = medicationRep
            )
        }
    }
}