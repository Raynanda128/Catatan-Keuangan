// File: screens/HistoryScreen.kt
package com.example.catatankeuangan.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.catatankeuangan.*
import com.example.catatankeuangan.Components.SwipeToDeleteContainer
import com.example.catatankeuangan.Components.formatTanggal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: MainViewModel) {
    val list by viewModel.allTransaksi.collectAsState()
    var filterDate by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Dialog Delete States
    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<TransaksiDetail?>(null) }

    val dateState = rememberDatePickerState()
    val filteredList = if (filterDate == null) list else list.filter { formatTanggal(it.date) == formatTanggal(filterDate!!) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("Riwayat", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PrimaryColor); IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.CalendarMonth, null, tint = PrimaryColor) } }
        if (filterDate != null) AssistChip(onClick = { filterDate = null }, label = { Text("Filter: ${formatTanggal(filterDate!!)}") }, trailingIcon = { Icon(Icons.Default.Close, null) })
        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filteredList, key = { it.id }) { item ->
                SwipeToDeleteContainer(
                    onDeleteRequest = {
                        itemToDelete = item
                        showDeleteDialog = true
                    }
                ) {
                    TransactionItemCardDetail(item)
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showDeleteDialog && itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false; itemToDelete = null },
            title = { Text("Hapus Transaksi?") },
            text = { Text("Apakah Anda yakin ingin menghapus transaksi ini? Data tidak dapat dikembalikan.") },
            confirmButton = {
                Button(
                    onClick = {
                        itemToDelete?.let { viewModel.deleteTransaksiObj(it) }
                        showDeleteDialog = false
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Hapus") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false; itemToDelete = null }) { Text("Batal") }
            }
        )
    }

    if (showDatePicker) { DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = { TextButton(onClick = { filterDate = dateState.selectedDateMillis; showDatePicker = false }) { Text("OK") } }) { DatePicker(state = dateState) } }
}