// File: screens/WalletScreen.kt
package com.example.catatankeuangan.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.catatankeuangan.*
import com.example.catatankeuangan.Components.formatRupiah

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(navController: NavController, viewModel: MainViewModel) {
    val wallets by viewModel.wallets.collectAsState()
    val list by viewModel.allTransaksi.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Dompet Saya", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { p ->
        LazyColumn(
            modifier = Modifier.padding(p).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(wallets) { wallet ->
                val wIn = list.filter { it.walletName == wallet.name && it.type == "PEMASUKAN" }.sumOf { it.amount }
                val wOut = list.filter { it.walletName == wallet.name && it.type == "PENGELUARAN" }.sumOf { it.amount }

                Card(colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp), shape = RoundedCornerShape(16.dp)) {
                    Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        val icon = when (wallet.type) { "BANK" -> Icons.Default.AccountBalance; "E-WALLET" -> Icons.Default.Smartphone; else -> Icons.Default.Money }
                        Box(Modifier.size(48.dp).background(PrimaryColor.copy(alpha = 0.1f), CircleShape), Alignment.Center) { Icon(icon, null, tint = PrimaryColor) }
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(wallet.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(wallet.type, fontSize = 12.sp, color = Color.Gray)
                        }
                        Text(formatRupiah(wIn - wOut), fontWeight = FontWeight.Bold, color = if ((wIn - wOut) >= 0) PrimaryColor else Color.Red)
                    }
                }
            }
        }
    }
}