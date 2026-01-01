// File: screens/AuthScreens.kt
package com.example.catatankeuangan.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.catatankeuangan.*

@Composable
fun LoginScreen(navController: NavController, viewModel: MainViewModel) {
    var u by remember { mutableStateOf("") }
    var p by remember { mutableStateOf("") }
    val context = LocalContext.current
    val colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryColor, focusedLabelColor = PrimaryColor, cursorColor = PrimaryColor)

    Column(Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Login", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(u, { u = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth(), colors = colors)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(p, { p = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), colors = colors)
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                viewModel.login(u, p,
                    onSuccess = {
                        navController.navigate(Screen.Home.route) { popUpTo(Screen.Login.route) { inclusive = true } }
                    },
                    onError = { Toast.makeText(context, "Gagal: Cek Username/Password", Toast.LENGTH_SHORT).show() }
                )
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
        ) { Text("MASUK") }
        TextButton({ navController.navigate(Screen.Register.route) }) { Text("Daftar Akun", color = PrimaryColor) }
    }
}

@Composable
fun RegisterScreen(navController: NavController, viewModel: MainViewModel) {
    var u by remember { mutableStateOf("") }
    var p by remember { mutableStateOf("") }
    val context = LocalContext.current
    val colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryColor, focusedLabelColor = PrimaryColor, cursorColor = PrimaryColor)

    Column(Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Daftar", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(u, { u = it }, label = { Text("Username Baru") }, modifier = Modifier.fillMaxWidth(), colors = colors)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(p, { p = it }, label = { Text("Password Baru") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), colors = colors)
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                if (u.isNotEmpty() && p.isNotEmpty()) {
                    viewModel.register(u, p,
                        onSuccess = {
                            Toast.makeText(context, "Berhasil", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        },
                        onError = { Toast.makeText(context, "Gagal/Dilarang", Toast.LENGTH_SHORT).show() }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
        ) { Text("DAFTAR") }
    }
}