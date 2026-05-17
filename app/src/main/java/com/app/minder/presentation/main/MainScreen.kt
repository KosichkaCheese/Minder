package com.app.minder.presentation.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.app.minder.presentation.home.HomeViewModel
import com.app.minder.presentation.navigation.bottomNavItems
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.app.minder.domain.interfaces.MeasurementRepository
import com.app.minder.domain.interfaces.MedicationRepository
import com.app.minder.domain.interfaces.ProfileRepository
import com.app.minder.domain.usecase.DeleteMedUseCase
import com.app.minder.domain.usecase.GetCurrentProfileUseCase
import com.app.minder.domain.usecase.GetMeasurementAnalysisUseCase
import com.app.minder.domain.usecase.SaveMeasurementUseCase
import com.app.minder.domain.usecase.SaveMedUseCase
import com.app.minder.domain.usecase.TakeMedicationUseCase
import com.app.minder.presentation.correlation.CorrelationScreen
import com.app.minder.presentation.correlation.CorrelationViewModel
import com.app.minder.presentation.home.HomeScreen
import com.app.minder.presentation.measurementDetail.MeasurementDetailScreen
import com.app.minder.presentation.measurementDetail.MeasurementDetailViewModel
import com.app.minder.presentation.medDetail.MedDetailScreen
import com.app.minder.presentation.medDetail.MedDetailViewModel
import com.app.minder.presentation.medDetail.MedScreenMode
import com.app.minder.presentation.medList.MedListScreen
import com.app.minder.presentation.medList.MedListViewModel
import com.app.minder.presentation.metrics.MetricsScreen
import com.app.minder.presentation.navigation.Screen
import com.app.minder.presentation.profile.ProfileScreen
import com.app.minder.presentation.profile.ProfileViewModel
import com.app.minder.presentation.theme.Mint
import com.app.minder.presentation.theme.NavBar
import com.app.minder.presentation.theme.onMint

@Composable
fun MainScreen(
    homeViewModel: HomeViewModel,
    medListViewModel: MedListViewModel,
    profileViewModel: ProfileViewModel,
    correlationViewModel: CorrelationViewModel,
    saveMedicationUseCase: SaveMedUseCase,
    deleteMedicationUseCase: DeleteMedUseCase,
    getCurrentProfileUseCase: GetCurrentProfileUseCase,
    takeMedicationUseCase: TakeMedicationUseCase,
    getMeasurementAnalysisUseCase: GetMeasurementAnalysisUseCase,
    saveMeasurementUseCase: SaveMeasurementUseCase,
    medicationRep: MedicationRepository,
    measurementRep: MeasurementRepository,
    profileRep: ProfileRepository,
    onLogout: () -> Unit = {}
) {
    val navController = rememberNavController()
    var isFabExpanded by remember{mutableStateOf(false)}
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val screensWithBottomBar = listOf(
        Screen.Home.route,
        Screen.Metrics.route,
        Screen.Profiles.route
    )

    Scaffold(
        bottomBar = {
            if (currentRoute in screensWithBottomBar) {
                NavigationBar(
                    containerColor = NavBar,
                    modifier = Modifier.height(85.dp)
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    bottomNavItems.forEach { item ->
                        val isSelected =
                            currentDestination?.hierarchy?.any { it.route == item.route } == true

                        NavigationBarItem(
                            icon = {
                                Icon(
                                    if (isSelected) item.active else item.inactive,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(35.dp)
                                )
                            },
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color.Transparent,
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                unselectedIconColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }
        },

        floatingActionButton = {
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            if (currentRoute == Screen.Home.route) {
                FloatingActionButtonMenu(
                    expanded = isFabExpanded,
                    onExpandChange = {isFabExpanded = it},
                    onMedListClick = {
                        navController.navigate(Screen.MedicationList.route)
                        isFabExpanded = false
                    },
                    onAddMedClick = {
                        navController.navigate("medication_detail/new/create")
                        isFabExpanded = false
                    }
                )
            } else if (currentRoute== Screen.MedicationList.route){
                FloatingActionButton(
                    onClick = {navController.navigate("medication_detail/new/create")},
                    containerColor = MaterialTheme.colorScheme.onTertiary,
                    contentColor = MaterialTheme.colorScheme.tertiary,
                    elevation = FloatingActionButtonDefaults.elevation(2.dp, 0.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Добавить"
                    )
                }
            }
        }
    ) {
        innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onIntakeClick = { medicationId ->
                        navController.navigate("medication_detail/$medicationId/intake")
                    }
                )
            }

            composable(Screen.Metrics.route) {
                MetricsScreen(
                    onSelect = { type ->
                        navController.navigate("measurement_detail/${type.id}")
                    },
                    onCorrelationClick = {
                        navController.navigate(Screen.Correlation.route)
                    }
                )
            }

            composable(Screen.Correlation.route) {
                CorrelationScreen(
                    onBack = { navController.popBackStack() },
                    viewModel = correlationViewModel
                )
            }

            composable(
                route = Screen.MeasurementDetail.route,
                arguments = listOf(
                    navArgument("id") { type = NavType.StringType }
                )
            ){ backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: return@composable
                val viewModel = remember(id) {
                    MeasurementDetailViewModel(
                        measurementTypeId = id,
                        measurementRep = measurementRep,
                        profileRep = profileRep,
                        getMeasurementAnalysisUseCase = getMeasurementAnalysisUseCase,
                        saveMeasurementUseCase = saveMeasurementUseCase
                    )
                }
                MeasurementDetailScreen(
                    viewModel = viewModel,
                    measurementTypeId = id,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Profiles.route) {
                ProfileScreen(
                    viewModel = profileViewModel,
                    onLogout = onLogout
                )
            }

            composable(Screen.MedicationList.route) {
                MedListScreen(
                    viewModel = medListViewModel,
                    onBack = { navController.popBackStack() },
                    onMedicationClick = { medicationId ->
                        navController.navigate("medication_detail/$medicationId/view")
                    }
                )
            }

            composable(
                route = Screen.MedDetail.route,
                arguments = listOf(
                    navArgument("id") { type = NavType.StringType },
                    navArgument("mode") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val medicationId = backStackEntry.arguments?.getString("id")?.takeIf { it != "new" }
                val modeStr = backStackEntry.arguments?.getString("mode") ?: "view"
                val mode = MedScreenMode.valueOf(modeStr.uppercase())

                val viewModel = remember(medicationId) {
                    MedDetailViewModel(
                        medicationId = medicationId,
                        saveMedicationUseCase = saveMedicationUseCase,
                        deleteMedicationUseCase = deleteMedicationUseCase,
                        getCurrentProfileUseCase = getCurrentProfileUseCase,
                        medicationRep = medicationRep,
                        takeMedicationUseCase = takeMedicationUseCase,
                    )
                }

                MedDetailScreen(
                    mode = mode,
                    onBack = { navController.popBackStack() },
                    viewModel = viewModel,
                    onEdit = {
                        medicationId?.let {id ->
                            navController.navigate("medication_detail/$id/edit")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun FloatingActionButtonMenu(
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onMedListClick: () -> Unit,
    onAddMedClick: () -> Unit
){
    Column(
        horizontalAlignment = Alignment.End
    ) {
        if (expanded){
            SmallFloatingActionButton(
                onClick = onMedListClick,
                containerColor = Mint,
                contentColor = onMint,
                elevation = FloatingActionButtonDefaults.elevation(1.dp, 0.dp),
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = "Список лекарств",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            SmallFloatingActionButton(
                onClick = onAddMedClick,
                containerColor = Mint,
                contentColor = onMint,
                elevation = FloatingActionButtonDefaults.elevation(1.dp, 0.dp),
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text="Добавить",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        FloatingActionButton(
            onClick = {onExpandChange(!expanded)},
            containerColor = MaterialTheme.colorScheme.onTertiary,
            contentColor = MaterialTheme.colorScheme.tertiary,
            elevation = FloatingActionButtonDefaults.elevation(2.dp, 0.dp),
            shape = CircleShape
        ) {
            Icon(
                imageVector = if (expanded) Icons.Default.Close else Icons.Default.Menu,
                contentDescription = if (expanded) "Закрыть" else "Меню",
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

