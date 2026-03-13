package com.app.minder.presentation.navigation

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Home : Screen("home")
    object Metrics : Screen("metrics")
    object Profiles : Screen("profiles")
    object MedicationList : Screen("medication_list")
    object MedDetail : Screen("medication_detail/{id}/{mode}")
}