// File: screens/StatsScreen.kt
package com.example.catatankeuangan.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.catatankeuangan.*
import com.example.catatankeuangan.Components.formatTanggal
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: MainViewModel) {
    val list by viewModel.allTransaksi.collectAsState(); val context = LocalContext.current
    var showDateRangePicker by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf<Long?>(Calendar.getInstance().apply { add(Calendar.MONTH, -1) }.timeInMillis) }
    var endDate by remember { mutableStateOf<Long?>(System.currentTimeMillis()) }
    val dateRangePickerState = rememberDateRangePickerState(initialSelectedStartDateMillis = startDate, initialSelectedEndDateMillis = endDate)
    var filterType by remember { mutableStateOf("SEMUA") }

    val filteredList = list.filter {
        val d = it.date
        val isDateMatch = (startDate == null || d >= startDate!!) && (endDate == null || d <= (endDate!! + 86400000))
        val isTypeMatch = if (filterType == "SEMUA") true else it.type == filterType
        isDateMatch && isTypeMatch
    }

    Scaffold { p ->
        Column(Modifier.padding(p).padding(16.dp)) {
            Text("Laporan Keuangan", fontSize=24.sp, fontWeight=FontWeight.Bold, color = PrimaryColor)
            OutlinedButton(onClick = { showDateRangePicker = true }, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.DateRange, null); Spacer(Modifier.width(8.dp))
                val startTxt = startDate?.let { formatTanggal(it) } ?: "Awal"
                val endTxt = endDate?.let { formatTanggal(it) } ?: "Akhir"
                Text("$startTxt - $endTxt", color = Color.Black)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { FilterChipCompact(selected = filterType == "SEMUA", text = "Semua") { filterType = "SEMUA" }; FilterChipCompact(selected = filterType == "PEMASUKAN", text = "Masuk") { filterType = "PEMASUKAN" }; FilterChipCompact(selected = filterType == "PENGELUARAN", text = "Keluar") { filterType = "PENGELUARAN" } }
            Spacer(Modifier.height(12.dp))
            DonutChartDetail(filteredList)
            Spacer(Modifier.height(16.dp)); Divider(); Spacer(Modifier.height(8.dp)); Text("Rincian Transaksi", fontWeight = FontWeight.Bold); Spacer(Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) { items(filteredList) { item -> TransactionItemCardDetail(item); Spacer(Modifier.height(8.dp)) }; item { Spacer(Modifier.height(80.dp)) } }
        }
    }
    if (showDateRangePicker) { DatePickerDialog(onDismissRequest = { showDateRangePicker = false }, confirmButton = { TextButton(onClick = { startDate = dateRangePickerState.selectedStartDateMillis; endDate = dateRangePickerState.selectedEndDateMillis; showDateRangePicker = false }) { Text("Terapkan") } }) { DateRangePicker(state = dateRangePickerState) } }
}