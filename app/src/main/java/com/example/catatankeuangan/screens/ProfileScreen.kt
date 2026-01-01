// File: screens/ProfileScreen.kt
package com.example.catatankeuangan.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.catatankeuangan.*
import com.example.catatankeuangan.Components.formatRupiah
import com.example.catatankeuangan.Components.shareLaporanWA
import java.util.*

@Composable
fun ProfileScreen(navController: NavController, viewModel: MainViewModel) {
    val user = viewModel.currentUser
    val list by viewModel.allTransaksi.collectAsState()
    val context = LocalContext.current

    // Dialog States
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }

    val isDev = user?.username == "RayyDev"
    val memberStatus = if (isDev) "Developer" else "Member Premium"
    val statusColor = if (isDev) GoldColor else Color.White.copy(0.8f)
    val borderStroke = if (isDev) BorderStroke(3.dp, GoldColor) else BorderStroke(0.dp, Color.Transparent)

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { try { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION); viewModel.updateProfilePhoto(it.toString()) } catch (e: Exception) { viewModel.updateProfilePhoto(it.toString()) } }
    }

    val totalTransaksi = list.size
    val saldo = list.filter { it.type == "PEMASUKAN" }.sumOf { it.amount } - list.filter { it.type == "PENGELUARAN" }.sumOf { it.amount }

    Column(Modifier.fillMaxSize().background(Color(0xFFF5F7FA)).verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth()) {
            Box(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(PrimaryColor, SecondaryColor)), RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))) {
                Column(Modifier.fillMaxWidth().padding(top = 40.dp, bottom = 30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Profil Saya", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(Modifier.height(20.dp))
                    Box(Modifier.size(104.dp).clip(CircleShape).background(Color.White).border(borderStroke, CircleShape).padding(4.dp).clip(CircleShape).clickable { launcher.launch(arrayOf("image/*")) }) {
                        if (user?.fotoUri != null) Image(rememberAsyncImagePainter(user.fotoUri), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        else Icon(Icons.Default.Person, null, Modifier.padding(20.dp).fillMaxSize(), tint = Color.Gray)
                        Box(Modifier.align(Alignment.BottomEnd).background(PrimaryColor, CircleShape).padding(6.dp)) { Icon(Icons.Default.Edit, null, Modifier.size(14.dp), tint = Color.White) }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(user?.username ?: "Guest", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(memberStatus, fontSize = 14.sp, color = statusColor, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp)).padding(vertical = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(totalTransaksi.toString(), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White); Text("Total Transaksi", fontSize = 12.sp, color = Color.White.copy(0.8f)) }
                        Box(modifier = Modifier.height(30.dp).width(1.dp).background(Color.White.copy(0.3f)))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(formatRupiah(saldo), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White); Text("Sisa Saldo", fontSize = 12.sp, color = Color.White.copy(0.8f)) }
                    }
                }
            }
        }

        // --- AKTIVITAS MENU ---
        Column(Modifier.padding(horizontal = 24.dp, vertical = 10.dp)) {
            Text("Aktivitas", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryColor)
            Spacer(Modifier.height(10.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
                Column {
                    ProfileMenuRow("Dompet Saya", Icons.Outlined.AccountBalanceWallet) { navController.navigate(Screen.Wallet.route) }
                    Divider(color = Color.LightGray.copy(0.2f))
                    ProfileMenuRow("Atur Budget Alert", Icons.Outlined.NotificationsActive) { showBudgetDialog = true }
                    Divider(color = Color.LightGray.copy(0.2f))
                    ProfileMenuRow("Riwayat Transaksi", Icons.Outlined.History) { navController.navigate(Screen.History.route) }
                    Divider(color = Color.LightGray.copy(0.2f))
                    ProfileMenuRow("Laporan Analisis", Icons.Outlined.Analytics) { navController.navigate(Screen.Stats.route) }
                    Divider(color = Color.LightGray.copy(0.2f))
                    ProfileMenuRow("Bagikan Laporan", Icons.Outlined.Share) { showShareDialog = true }
                }
            }
        }

        Column(Modifier.padding(horizontal = 24.dp)) {
            Text("Pengaturan", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryColor)
            Spacer(Modifier.height(10.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
                Column {
                    ProfileMenuRow("Ubah Password", Icons.Outlined.Lock) { showPasswordDialog = true }
                    Divider(color = Color.LightGray.copy(0.2f))
                    ProfileMenuRow("Tentang & Bantuan", Icons.Outlined.HelpOutline) { showHelpDialog = true }
                }
            }
        }

        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = { showLogoutDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE), contentColor = Color.Red), modifier = Modifier.fillMaxWidth().height(50.dp), shape=RoundedCornerShape(12.dp)) { Icon(Icons.Default.Logout, null); Spacer(Modifier.width(8.dp)); Text("Keluar") }
            Spacer(Modifier.height(16.dp)); Text("Versi 1.7.0 (Pro)", fontSize=12.sp, color=Color.Gray); Spacer(Modifier.height(80.dp))
        }
    }

    // --- DIALOGS ---
    if (showBudgetDialog) {
        val currentLimit by viewModel.budgetLimit.collectAsState()
        val currentPeriod by viewModel.budgetPeriod.collectAsState()
        var inputLimit by remember { mutableStateOf(if(currentLimit > 0) currentLimit.toInt().toString() else "") }
        var selectedPeriod by remember { mutableStateOf(currentPeriod) }

        AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            title = { Text("Atur Budget Alert", fontWeight = FontWeight.Bold, color = PrimaryColor) },
            text = {
                Column {
                    Text("Notifikasi akan muncul & bersuara jika pengeluaran melebihi batas ini.", fontSize = 12.sp, color = Color.Gray)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = inputLimit,
                        onValueChange = { if (it.all { c -> c.isDigit() }) inputLimit = it },
                        label = { Text("Batas (Rp)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Periode Reset:", fontSize = 12.sp, color = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("HARIAN", "MINGGUAN", "BULANAN").forEach { period ->
                            FilterChipCompact(selected = selectedPeriod == period, text = period) { selectedPeriod = period }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val limit = inputLimit.toDoubleOrNull() ?: 0.0
                        viewModel.saveBudgetSettings(limit, selectedPeriod)
                        Toast.makeText(context, "Pengingat disimpan!", Toast.LENGTH_SHORT).show()
                        showBudgetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) { Text("Simpan") }
            },
            dismissButton = { TextButton(onClick = { showBudgetDialog = false }) { Text("Batal") } }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(onDismissRequest = { showLogoutDialog = false }, title = { Text("Konfirmasi") }, text = { Text("Yakin ingin keluar?") }, confirmButton = { Button(onClick = { showLogoutDialog = false; viewModel.logout(); navController.navigate(Screen.Login.route) { popUpTo(Screen.Home.route) { inclusive = true } } }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Ya, Keluar") } }, dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Batal") } })
    }

    if (showPasswordDialog) {
        var newPass by remember { mutableStateOf("") }
        AlertDialog(onDismissRequest = { showPasswordDialog = false }, title = { Text("Ganti Password", color = PrimaryColor) }, text = { OutlinedTextField(newPass, { newPass = it }, label = { Text("Password Baru") }) }, confirmButton = { Button(onClick = { viewModel.updatePassword(newPass); showPasswordDialog = false; Toast.makeText(context, "Sukses", Toast.LENGTH_SHORT).show() }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)) { Text("Simpan") } }, dismissButton = { TextButton({ showPasswordDialog = false }) { Text("Batal") } })
    }
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Info, null, tint = PrimaryColor); Spacer(Modifier.width(8.dp)); Text("Tentang Aplikasi", color = PrimaryColor, fontWeight = FontWeight.Bold) } },
            text = { Column { Text("Catatan Keuangan Pro", fontWeight = FontWeight.Bold, fontSize = 16.sp); Text("Versi 1.6.0", fontSize = 12.sp, color = Color.Gray); Spacer(Modifier.height(16.dp)); Text("Aplikasi ini dirancang untuk membantu Anda mengelola arus kas harian, memantau saldo di berbagai dompet, dan menganalisis keuangan dengan mudah.", style = TextStyle(lineHeight = 20.sp)); Spacer(Modifier.height(16.dp)); Divider(color = Color.LightGray.copy(0.5f)); Spacer(Modifier.height(16.dp)); Text("Pengembang:", fontWeight = FontWeight.SemiBold); Text("RayyDev", color = PrimaryColor); Spacer(Modifier.height(8.dp)); Text("Kontak & Bantuan:", fontWeight = FontWeight.SemiBold); Text("raynandaramadhan1@gmail.com", color = Color.DarkGray); Spacer(Modifier.height(16.dp)); Text("© 2024 RayyDev. All rights reserved.", fontSize = 10.sp, color = Color.LightGray) } },
            confirmButton = { Button(onClick = { showHelpDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)) { Text("Tutup") } }
        )
    }
    if (showShareDialog) {
        ShareOptionsDialog(
            list = list,
            onDismiss = { showShareDialog = false },
            onShare = { filteredList, start, end ->
                shareLaporanWA(context, filteredList, start, end)
                showShareDialog = false
            }
        )
    }
}

@Composable
fun ShareOptionsDialog(
    list: List<TransaksiDetail>,
    onDismiss: () -> Unit,
    onShare: (List<TransaksiDetail>, Long?, Long?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pilih Periode Laporan", fontWeight = FontWeight.Bold, color = PrimaryColor) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { val today = Calendar.getInstance(); val start = today.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0) }.timeInMillis; val end = today.apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59) }.timeInMillis; val filtered = list.filter { it.date in start..end }; onShare(filtered, start, end) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)) { Text("Hari Ini") }
                Button(onClick = { val cal = Calendar.getInstance(); cal.set(Calendar.DAY_OF_MONTH, 1); val start = cal.timeInMillis; cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH)); val end = cal.timeInMillis; val filtered = list.filter { it.date in start..end }; onShare(filtered, start, end) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)) { Text("Bulan Ini") }
                Button(onClick = { val cal = Calendar.getInstance(); cal.set(Calendar.DAY_OF_YEAR, 1); val start = cal.timeInMillis; cal.set(Calendar.MONTH, 11); cal.set(Calendar.DAY_OF_MONTH, 31); val end = cal.timeInMillis; val filtered = list.filter { it.date in start..end }; onShare(filtered, start, end) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)) { Text("Tahun Ini") }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Batal", color = Color.Red) } }
    )
}