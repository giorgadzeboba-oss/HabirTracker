package com.habittracker.presentation.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.habittracker.presentation.viewmodel.HabitViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddHabit : Screen("add_habit")
    object EditHabit : Screen("edit_habit/{habitId}") {
        fun createRoute(habitId: Int) = "edit_habit/$habitId"
    }
    object Calendar : Screen("calendar/{habitId}") {
        fun createRoute(habitId: Int) = "calendar/$habitId"
    }
    object Statistics : Screen("statistics")
    object Settings : Screen("settings")
}

@Composable
fun HabitNavHost(
    navController: NavHostController,
    viewModel: HabitViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onAddHabit = { navController.navigate(Screen.AddHabit.route) },
                onEditHabit = { habitId ->
                    navController.navigate(Screen.EditHabit.createRoute(habitId))
                },
                onNavigateToStats = { navController.navigate(Screen.Statistics.route) },
                onNavigateToCalendar = { habitId ->
                    navController.navigate(Screen.Calendar.createRoute(habitId))
                }
            )
        }

        composable(Screen.AddHabit.route) {
            AddEditHabitScreen(
                viewModel = viewModel,
                habitId = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditHabit.route,
            arguments = listOf(navArgument("habitId") { type = NavType.IntType })
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getInt("habitId")
            AddEditHabitScreen(
                viewModel = viewModel,
                habitId = habitId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Calendar.route,
            arguments = listOf(navArgument("habitId") { type = NavType.IntType })
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getInt("habitId") ?: 0
            CalendarScreen(
                viewModel = viewModel,
                habitId = habitId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Statistics.route) {
            StatisticsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}