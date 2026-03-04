package com.app.minder.presentation.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline

data class BottomNavItem (
    val route: String,
    val active: ImageVector,
    val inactive: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = Screen.Profiles.route,
        active = Icons.Default.Person,
        inactive = Icons.Default.PersonOutline,
        label = "Профили"
    ),
    BottomNavItem(
        route = Screen.Home.route,
        active = Icons.Default.AccessTimeFilled,
        inactive = Icons.Default.AccessTime,
        label = "Главная"
    ),
    BottomNavItem(
        route = Screen.Metrics.route,
        active = Icons.Default.Favorite,
        inactive = Icons.Default.FavoriteBorder,
        label = "Показатели"
    )
)