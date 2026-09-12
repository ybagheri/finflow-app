package com.finflow.app.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.finflow.app.presentation.screens.addedit.AddEditScreen
import com.finflow.app.presentation.screens.categories.CategoriesScreen
import com.finflow.app.presentation.screens.home.HomeScreen
import com.finflow.app.presentation.screens.reports.ReportsPlaceholderScreen
import com.finflow.app.presentation.screens.settings.SettingsPlaceholderScreen
import com.finflow.app.presentation.screens.transactions.TransactionsScreen

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val TABS = listOf(
    Tab(Routes.HOME, "Home", Icons.Filled.Home),
    Tab(Routes.TRANSACTIONS, "Activity", Icons.Filled.Receipt),
    Tab(Routes.CATEGORIES, "Categories", Icons.Filled.Category),
    Tab(Routes.REPORTS, "Reports", Icons.Filled.PieChart)
)

/**
 * Root navigation graph with bottom bar + quick-add FAB skeleton.
 * Feature screens are filled in during Phases 2-4; placeholders keep
 * navigation compilable and demo-able from day one.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinFlowNavGraph() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                TABS.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(Routes.HOME) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Routes.addEdit()) }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add transaction")
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(onAddClick = { navController.navigate(Routes.addEdit()) })
            }
            composable(Routes.TRANSACTIONS) {
                TransactionsScreen(
                    onAddClick = { navController.navigate(Routes.addEdit()) },
                    onEditClick = { id -> navController.navigate(Routes.addEdit(id = id)) }
                )
            }
            composable(
                route = Routes.ADD_EDIT,
                arguments = listOf(
                    navArgument("id") { type = NavType.LongType; defaultValue = 0L },
                    navArgument("type") { type = NavType.StringType; defaultValue = "EXPENSE" }
                )
            ) {
                AddEditScreen(onDone = { navController.popBackStack() })
            }
            composable(Routes.CATEGORIES) { CategoriesScreen() }
            composable(Routes.REPORTS) { ReportsPlaceholderScreen() }
            composable(Routes.SETTINGS) { SettingsPlaceholderScreen() }
        }
    }
}
