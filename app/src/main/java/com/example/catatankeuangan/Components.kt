package com.example.catatankeuangan.Components

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.catatankeuangan.MainActivity
import com.example.catatankeuangan.TransaksiDetail
import com.example.catatankeuangan.bottomNavItems
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

fun formatRupiah(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(amount).replace("Rp", "Rp ").substringBeforeLast(",00")
}

fun formatTanggal(millis: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    return formatter.format(Date(millis))
}

fun showNotification(context: Context, title: String, message: String) {
    val channelId = "catatan_keuangan_alert_v3"
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(channelId, "Budget Alert & Reminder", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Notifikasi penting untuk budget dan pengingat"
            enableVibration(true)
            enableLights(true)
            setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build())
        }
        notificationManager.createNotificationChannel(channel)
    }

    // --- UPDATE: Intent agar notifikasi bisa diklik dan membuka aplikasi ---
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

    val pendingIntent: PendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE else 0
    )

    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setDefaults(Notification.DEFAULT_ALL)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent) // Pasang intent di sini

    notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
}

class NotificationWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    override fun doWork(): Result {
        showNotification(applicationContext, "Cek Keuanganmu", "Jangan lupa catat transaksi hari ini!")
        return Result.success()
    }
}

fun shareLaporanWA(context: Context, data: List<TransaksiDetail>, startDate: Long?, endDate: Long?) {
    val sb = StringBuilder()
    val rangeInfo = if (startDate != null && endDate != null) "${formatTanggal(startDate)} - ${formatTanggal(endDate)}" else "Semua Waktu"
    val totalMasuk = data.filter { it.type == "PEMASUKAN" }.sumOf { it.amount }
    val totalKeluar = data.filter { it.type == "PENGELUARAN" }.sumOf { it.amount }
    val saldo = totalMasuk - totalKeluar

    sb.append("📊 *LAPORAN KEUANGAN*\n🗓 Periode: $rangeInfo\n\n")
    sb.append("💰 *RINGKASAN*\n➕ Masuk : ${formatRupiah(totalMasuk)}\n➖ Keluar : ${formatRupiah(totalKeluar)}\n🟰 Saldo  : ${formatRupiah(saldo)}\n--------------------------------\n\n")
    sb.append("📝 *DETAIL TRANSAKSI*\n")
    data.groupBy { formatTanggal(it.date) }.forEach { (tanggal, transaksis) ->
        sb.append("📅 *$tanggal*\n")
        transaksis.forEach { t -> sb.append("${if(t.type == "PEMASUKAN") "🟢" else "🔴"} ${t.categoryName} (${t.walletName})\n   ${formatRupiah(t.amount)}\n") }
        sb.append("\n")
    }
    sb.append("_Dikirim dari Aplikasi Catatan Keuangan_")
    val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, sb.toString()) }
    intent.setPackage("com.whatsapp")
    try { context.startActivity(intent) } catch (e: Exception) { intent.setPackage(null); context.startActivity(Intent.createChooser(intent, "Bagikan Laporan")) }
}

@Composable
fun SummaryCardTransparent(title: String, amount: Double, icon: ImageVector, iconColor: Color, mod: Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)), shape = RoundedCornerShape(16.dp), modifier = mod) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(36.dp).background(Color.White, CircleShape), Alignment.Center) { Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp)) }
            Spacer(Modifier.width(12.dp))
            Column { Text(title, fontSize = 11.sp, color = Color.White.copy(0.9f)); Text(formatRupiah(amount), fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp) }
        }
    }
}

@Composable
fun BottomNavigationBar(currentRoute: String?, onNavigate: (String) -> Unit) {
    NavigationBar(containerColor = Color.White, tonalElevation = 10.dp) {
        bottomNavItems.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                icon = { Icon(item.icon, null, tint = if(isSelected) Color(0xFF1565C0) else Color.Gray) },
                label = { Text(item.name, fontSize = 10.sp, color = if(isSelected) Color(0xFF1565C0) else Color.Gray) },
                selected = isSelected,
                onClick = { onNavigate(item.route) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteContainer(onDeleteRequest: () -> Unit, content: @Composable () -> Unit) {
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { if (it == SwipeToDismissBoxValue.EndToStart) { onDeleteRequest(); false } else false }
    )
    SwipeToDismissBox(
        state = state,
        backgroundContent = {
            Box(Modifier.fillMaxSize().background(Color.Red, RoundedCornerShape(16.dp)).padding(horizontal = 20.dp), contentAlignment = Alignment.CenterEnd) { Icon(Icons.Default.Delete, null, tint = Color.White) }
        },
        content = { content() }
    )
}