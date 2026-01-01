package com.example.catatankeuangan.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.catatankeuangan.*
import com.example.catatankeuangan.Components.formatRupiah
import com.example.catatankeuangan.Components.formatTanggal

// --- DEFINISI WARNA (Dipindahkan dari Screens.kt) ---
val PrimaryColor = Color(0xFF1565C0)
val SecondaryColor = Color(0xFF00ACC1)
val GoldColor = Color(0xFFFFD700)

// --- Shared Helper UI Components ---

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryItem(category: Category, isSelected: Boolean, onClick: () -> Unit, onLongClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(56.dp).clip(CircleShape).background(if (isSelected) PrimaryColor else Color(0xFFF0F0F0))
        ) {
            Icon(getIconByName(category.iconName), null, tint = if (isSelected) Color.White else Color.Gray)
        }
        Spacer(Modifier.height(4.dp))
        Text(category.name, fontSize = 11.sp, textAlign = TextAlign.Center, color = if (isSelected) PrimaryColor else Color.Black, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun TransactionItemCardDetail(item: TransaksiDetail) {
    val isMasuk = item.type == "PEMASUKAN"
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp), border = BorderStroke(1.dp, Color(0xFFF0F0F0)), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).background(if (isMasuk) Color(0xFFE8F5E9) else Color(0xFFFFEBEE), CircleShape), Alignment.Center) {
                Icon(getIconByName(item.categoryIcon), null, tint = if (isMasuk) Color(0xFF4CAF50) else Color(0xFFE53935))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.categoryName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(if (item.note.isNotEmpty()) item.note else item.walletName, fontSize = 12.sp, color = Color.Gray)
                Text(formatTanggal(item.date), fontSize = 10.sp, color = Color.LightGray)
            }
            Text(text = (if (isMasuk) "+ " else "- ") + formatRupiah(item.amount), fontWeight = FontWeight.Bold, color = if (isMasuk) Color(0xFF2E7D32) else Color(0xFFE53935))
        }
    }
}

@Composable
fun DonutChartDetail(data: List<TransaksiDetail>) {
    val totalMasuk = data.filter { it.type == "PEMASUKAN" }.sumOf { it.amount }
    val totalKeluar = data.filter { it.type == "PENGELUARAN" }.sumOf { it.amount }
    val totalVol = totalMasuk + totalKeluar
    val inSweep = if (totalVol == 0.0) 0f else (totalMasuk / totalVol * 360f).toFloat()
    val outSweep = if (totalVol == 0.0) 0f else (totalKeluar / totalVol * 360f).toFloat()

    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().height(220.dp)) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(180.dp)) {
            val stroke = 35f
            drawArc(Color.LightGray.copy(0.2f), 0f, 360f, false, style = Stroke(stroke, cap = StrokeCap.Round))
            if (inSweep > 0) drawArc(Color(0xFF4CAF50), -90f, inSweep, false, style = Stroke(stroke, cap = StrokeCap.Round))
            if (outSweep > 0) drawArc(Color(0xFFE91E63), -90f + inSweep + 5f, outSweep - 5f, false, style = Stroke(stroke, cap = StrokeCap.Round))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Sisa Saldo", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(formatRupiah(totalMasuk - totalKeluar), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = if ((totalMasuk - totalKeluar) >= 0) Color(0xFF2E7D32) else Color.Red)
        }
    }
}

@Composable
fun ProfileMenuRow(title: String, icon: ImageVector, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = PrimaryColor, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = Color.Gray.copy(0.5f), modifier = Modifier.size(20.dp))
    }
}

@Composable
fun FilterChipCompact(selected: Boolean, text: String, onClick: () -> Unit) {
    Surface(color = if (selected) PrimaryColor else Color.LightGray.copy(alpha = 0.2f), contentColor = if (selected) Color.White else Color.Black, shape = CircleShape, modifier = Modifier.clickable { onClick() }) {
        Text(text = text, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}