package com.example.catatankeuangan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
// IMPORT PENTING DI SINI:
import com.example.catatankeuangan.screens.*
import com.example.catatankeuangan.Components.BottomNavigationBar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: MainViewModel = viewModel(factory = MainViewModelFactory(application))

            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            Scaffold(
                bottomBar = {
                    if (currentRoute in listOf(Screen.Home.route, Screen.Add.route, Screen.Profile.route)) {
                        BottomNavigationBar(currentRoute) { route ->
                            navController.navigate(route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true; restoreState = true
                            }
                        }
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = Screen.Splash.route,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable(Screen.Splash.route) { SplashScreen(navController, viewModel) }
                    composable(Screen.Login.route) { LoginScreen(navController, viewModel) }
                    composable(Screen.Register.route) { RegisterScreen(navController, viewModel) }
                    composable(Screen.Home.route) { HomeScreen(navController, viewModel) }
                    composable(Screen.Add.route) { AddScreen(navController, viewModel) }
                    composable(Screen.Profile.route) { ProfileScreen(navController, viewModel) }
                    composable(Screen.History.route) { HistoryScreen(viewModel) }
                    composable(Screen.Stats.route) { StatsScreen(viewModel) }
                    composable(Screen.Wallet.route) { WalletScreen(navController, viewModel) }
                }
            }
        }
    }
}