// File: screens/AddScreen.kt
package com.example.catatankeuangan.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.catatankeuangan.*
import com.example.catatankeuangan.Components.formatTanggal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(navController: NavController, viewModel: MainViewModel) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("PENGELUARAN") }
    var date by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDate by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showDeleteCategoryDialog by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }

    val dateState = rememberDatePickerState(date)

    val wallets by viewModel.wallets.collectAsState()
    val expenseCats by viewModel.expenseCategories.collectAsState()
    val incomeCats by viewModel.incomeCategories.collectAsState()

    var selectedWalletId by remember { mutableStateOf<Long?>(null) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var walletTypeFilter by remember { mutableStateOf("CASH") }

    val filteredWallets = wallets.filter { it.type == walletTypeFilter }
    LaunchedEffect(walletTypeFilter, wallets) { if (filteredWallets.isNotEmpty()) selectedWalletId = filteredWallets[0].id else selectedWalletId = null }

    val currentCategories = if (type == "PENGELUARAN") expenseCats else incomeCats

    Column(Modifier.fillMaxSize().background(Color.White)) {
        Row(Modifier.fillMaxWidth().background(Color(0xFFEEEEEE))) {
            listOf("PENGELUARAN", "PEMASUKAN").forEach { t ->
                val isSel = type == t
                Box(
                    modifier = Modifier.weight(1f).clickable { type = t; selectedCategoryId = null }
                        .background(if (isSel) (if (t == "PENGELUARAN") Color(0xFFE53935) else Color(0xFF43A047)) else Color.Transparent)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (t == "PENGELUARAN") "Pengeluaran" else "Pemasukan", color = if (isSel) Color.White else Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
        }

        Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
            Text("Jumlah", fontSize = 12.sp, color = Color.Gray)
            OutlinedTextField(
                value = amount, onValueChange = { if (it.all { c -> c.isDigit() }) amount = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PrimaryColor),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                prefix = { Text("Rp ", fontSize = 24.sp, fontWeight = FontWeight.Bold) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryColor, focusedLabelColor = PrimaryColor)
            )
            Spacer(Modifier.height(16.dp))

            Text("Sumber Dana", fontSize = 14.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("CASH", "BANK", "E-WALLET").forEach { type ->
                    FilterChipCompact(selected = walletTypeFilter == type, text = type) { walletTypeFilter = type }
                }
            }
            Spacer(Modifier.height(8.dp))
            if (filteredWallets.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredWallets) { w -> FilterChipCompact(selected = selectedWalletId == w.id, text = w.name) { selectedWalletId = w.id } }
                }
            } else { Text("Tidak ada dompet tipe ini.", fontSize = 12.sp, color = Color.Red) }

            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Kategori (Tekan lama untuk hapus)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = { showAddCategoryDialog = true }) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Text("Tambah", fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(8.dp))
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 70.dp),
                modifier = Modifier.height(240.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(currentCategories) { cat ->
                    CategoryItem(
                        category = cat,
                        isSelected = selectedCategoryId == cat.id,
                        onClick = { selectedCategoryId = cat.id },
                        onLongClick = {
                            categoryToDelete = cat
                            showDeleteCategoryDialog = true
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Catatan") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryColor))
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { showDate = true }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.DateRange, null); Spacer(Modifier.width(8.dp)); Text(formatTanggal(date), color = Color.Black) }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    if (amount.isNotEmpty() && selectedCategoryId != null && selectedWalletId != null) {
                        viewModel.addTransaksi(amount.toDouble(), note, date, type, selectedCategoryId!!, selectedWalletId!!)
                        navController.popBackStack()
                    } else {
                        Toast.makeText(navController.context, "Lengkapi data", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) { Text("SIMPAN") }
            Spacer(Modifier.height(80.dp))
        }
    }

    if (showDate) {
        DatePickerDialog(onDismissRequest = { showDate = false }, confirmButton = { TextButton(onClick = { dateState.selectedDateMillis?.let { date = it }; showDate = false }) { Text("OK") } }) { DatePicker(state = dateState) }
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            type = type,
            onDismiss = { showAddCategoryDialog = false },
            onSave = { name, icon ->
                viewModel.addCategory(name, icon, type)
                showAddCategoryDialog = false
                Toast.makeText(navController.context, "Kategori ditambahkan", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showDeleteCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteCategoryDialog = false },
            title = { Text("Hapus Kategori?") },
            text = { Text("Apakah Anda yakin ingin menghapus kategori '${categoryToDelete?.name}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        categoryToDelete?.let {
                            viewModel.deleteCategory(it)
                            Toast.makeText(navController.context, "Kategori dihapus", Toast.LENGTH_SHORT).show()
                        }
                        showDeleteCategoryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Hapus") }
            },
            dismissButton = { TextButton(onClick = { showDeleteCategoryDialog = false }) { Text("Batal") } }
        )
    }
}

@Composable
fun AddCategoryDialog(type: String, onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("fastfood") }

    val icons = listOf(
        "fastfood", "commute", "shopping", "health", "education", "bill", "entertainment",
        "other_expense", "salary", "gift", "investment", "other_income", "wallet", "bank", "qr"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Kategori Baru", fontWeight = FontWeight.Bold, color = PrimaryColor) },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Kategori") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))
                Text("Pilih Ikon:", fontSize = 12.sp, color = Color.Gray)
                Spacer(Modifier.height(8.dp))
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 48.dp),
                    modifier = Modifier.height(150.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(icons) { iconName ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (selectedIcon == iconName) PrimaryColor else Color.LightGray.copy(0.2f))
                                .clickable { selectedIcon = iconName }
                        ) {
                            Icon(getIconByName(iconName), null, tint = if (selectedIcon == iconName) Color.White else Color.Gray)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotEmpty()) onSave(name, selectedIcon) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal", color = Color.Red) } }
    )
}