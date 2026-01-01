// File: screens/SplashScreen.kt
package com.example.catatankeuangan.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.catatankeuangan.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController, viewModel: MainViewModel) {
    LaunchedEffect(Unit) {
        delay(1500)
        viewModel.checkLoginStatus { isLoggedIn ->
            val route = if (isLoggedIn) Screen.Home.route else Screen.Login.route
            navController.navigate(route) { popUpTo(Screen.Splash.route) { inclusive = true } }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PrimaryColor, SecondaryColor))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.AccountBalanceWallet, null, tint = Color.White, modifier = Modifier.size(100.dp))
            Text("Catatan Keuangan", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}