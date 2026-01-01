package com.example.catatankeuangan

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

@Entity(tableName = "tabel_user")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val password: String,
    val fotoUri: String? = null
)

@Entity(tableName = "tabel_wallet")
data class Wallet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String // CASH, BANK, E-WALLET
)

@Entity(tableName = "tabel_category")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconName: String,
    val type: String
)

@Entity(tableName = "tabel_transaksi")
data class Transaksi(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val note: String,
    val date: Long,
    val type: String,
    val categoryId: Long,
    val walletId: Long
)

data class TransaksiDetail(
    val id: Long,
    val amount: Double,
    val note: String,
    val date: Long,
    val type: String,
    val categoryName: String,
    val categoryIcon: String,
    val walletName: String
)

fun getIconByName(name: String): ImageVector {
    return when(name) {
        "fastfood" -> Icons.Default.Fastfood
        "commute" -> Icons.Default.Commute
        "shopping" -> Icons.Default.ShoppingBag
        "health" -> Icons.Default.MedicalServices
        "education" -> Icons.Default.School
        "bill" -> Icons.Default.ReceiptLong
        "entertainment" -> Icons.Default.Movie
        "other_expense" -> Icons.Default.MoneyOff
        "salary" -> Icons.Default.Work
        "gift" -> Icons.Default.CardGiftcard
        "investment" -> Icons.Default.TrendingUp
        "other_income" -> Icons.Default.AttachMoney
        else -> Icons.Default.Help
    }
}

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Add : Screen("add")
    object Profile : Screen("profile")
    object History : Screen("history")
    object Stats : Screen("stats")
    object Wallet : Screen("wallet")
}

data class BottomNavItem(val name: String, val route: String, val icon: ImageVector)
val bottomNavItems = listOf(
    BottomNavItem("Beranda", Screen.Home.route, Icons.Default.Home),
    BottomNavItem("Tambah", Screen.Add.route, Icons.Default.AddCircle),
    BottomNavItem("Profil", Screen.Profile.route, Icons.Default.Person)
)