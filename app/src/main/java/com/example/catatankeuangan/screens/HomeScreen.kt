// File: screens/HomeScreen.kt
package com.example.catatankeuangan.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.catatankeuangan.*
import com.example.catatankeuangan.Components.SummaryCardTransparent
import com.example.catatankeuangan.Components.formatRupiah
import java.util.Calendar

@Composable
fun HomeScreen(navController: NavController, viewModel: MainViewModel) {
    val list by viewModel.allTransaksi.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)
    val thisMonthList = list.filter {
        val c = Calendar.getInstance().apply { timeInMillis = it.date }
        c.get(Calendar.MONTH) == currentMonth && c.get(Calendar.YEAR) == currentYear
    }

    val totalSaldoGlobal = list.filter { it.type == "PEMASUKAN" }.sumOf { it.amount } - list.filter { it.type == "PENGELUARAN" }.sumOf { it.amount }

    Column(Modifier.fillMaxSize().background(Color(0xFFF5F7FA))) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(PrimaryColor, SecondaryColor)),
                    RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                )
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                Column(
                    Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Total Kekayaan Bersih", color = Color.White.copy(0.8f), fontSize = 12.sp)
                    Text(formatRupiah(totalSaldoGlobal), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                }

                val masukBulanIni = thisMonthList.filter { it.type == "PEMASUKAN" }.sumOf { it.amount }
                val keluarBulanIni = thisMonthList.filter { it.type == "PENGELUARAN" }.sumOf { it.amount }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCardTransparent("Pemasukan", masukBulanIni, Icons.Default.ArrowDownward, Color(0xFF4CAF50), Modifier.weight(1f))
                    SummaryCardTransparent("Pengeluaran", keluarBulanIni, Icons.Default.ArrowUpward, Color(0xFFE91E63), Modifier.weight(1f))
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Statistik Bulan Ini", fontWeight = FontWeight.Bold, color = PrimaryColor)
                        Spacer(Modifier.height(12.dp))
                        DonutChartDetail(thisMonthList)
                    }
                }
            }
            item { Text("Transaksi Bulan Ini", fontWeight = FontWeight.Bold, color = Color.DarkGray); Spacer(Modifier.height(8.dp)) }
            if (thisMonthList.isEmpty()) {
                item { Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) { Text("Belum ada transaksi.", fontSize = 12.sp, color = Color.Gray) } }
            } else {
                items(thisMonthList) { item -> TransactionItemCardDetail(item); Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}