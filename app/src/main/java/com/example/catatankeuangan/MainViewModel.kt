package com.example.catatankeuangan

import android.app.Application
import android.content.Context
import androidx.compose.runtime.*
import androidx.lifecycle.*
import androidx.work.*
import com.example.catatankeuangan.Components.NotificationWorker
import com.example.catatankeuangan.Components.formatRupiah
import com.example.catatankeuangan.Components.showNotification
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val transaksiDao = db.transaksiDao()
    private val userDao = db.userDao()
    private val walletDao = db.walletDao()
    private val categoryDao = db.categoryDao()
    private val prefs = application.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    val allTransaksi: StateFlow<List<TransaksiDetail>> = transaksiDao.getAllTransaksiDetail()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wallets: StateFlow<List<Wallet>> = walletDao.getAllWallets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenseCategories = categoryDao.getCategoriesByType("PENGELUARAN")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val incomeCategories = categoryDao.getCategoriesByType("PEMASUKAN")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _budgetLimit = MutableStateFlow(0.0)
    val budgetLimit: StateFlow<Double> = _budgetLimit

    private val _budgetPeriod = MutableStateFlow("HARIAN") // HARIAN, MINGGUAN, BULANAN
    val budgetPeriod: StateFlow<String> = _budgetPeriod

    var currentUser by mutableStateOf<User?>(null)
        private set

    init {
        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(8, TimeUnit.HOURS).build()
        WorkManager.getInstance(application).enqueueUniquePeriodicWork("DailyReminder", ExistingPeriodicWorkPolicy.KEEP, workRequest)

        seedDatabase()
        loadBudgetSettings()
    }

    private fun loadBudgetSettings() {
        val limit = prefs.getFloat("budget_limit", 0f).toDouble()
        val period = prefs.getString("budget_period", "HARIAN") ?: "HARIAN"
        _budgetLimit.value = limit
        _budgetPeriod.value = period
    }

    fun saveBudgetSettings(limit: Double, period: String) {
        prefs.edit()
            .putFloat("budget_limit", limit.toFloat())
            .putString("budget_period", period)
            .apply()
        _budgetLimit.value = limit
        _budgetPeriod.value = period
    }

    private fun seedDatabase() {
        viewModelScope.launch {
            if (walletDao.getCount() == 0) {
                walletDao.insertWallet(Wallet(name = "Tunai", type = "CASH"))
                val banks = listOf("BCA", "Mandiri", "BRI", "BNI", "Jago", "Jenius", "SeaBank")
                banks.forEach { walletDao.insertWallet(Wallet(name = it, type = "BANK")) }
                val ewallets = listOf("GoPay", "OVO", "DANA", "ShopeePay", "LinkAja")
                ewallets.forEach { walletDao.insertWallet(Wallet(name = it, type = "E-WALLET")) }
            }
            if (categoryDao.getCount() == 0) {
                val exp = listOf(
                    Category(name="Makan", iconName="fastfood", type="PENGELUARAN"),
                    Category(name="Transport", iconName="commute", type="PENGELUARAN"),
                    Category(name="Belanja", iconName="shopping", type="PENGELUARAN"),
                    Category(name="Tagihan", iconName="bill", type="PENGELUARAN")
                )
                exp.forEach { categoryDao.insertCategory(it) }
                val inc = listOf(Category(name="Gaji", iconName="salary", type="PEMASUKAN"))
                inc.forEach { categoryDao.insertCategory(it) }
            }
        }
    }

    fun addTransaksi(amount: Double, note: String, date: Long, type: String, categoryId: Long, walletId: Long) {
        viewModelScope.launch {
            transaksiDao.insertTransaksi(Transaksi(amount=amount, note=note, date=date, type=type, categoryId=categoryId, walletId=walletId))
            if (type == "PENGELUARAN") {
                checkBudgetExceeded()
            }
        }
    }

    private suspend fun checkBudgetExceeded() {
        val limit = _budgetLimit.value
        val period = _budgetPeriod.value
        if (limit <= 0) return

        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        val startTime: Long = when (period) {
            "HARIAN" -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0)
                calendar.timeInMillis
            }
            "MINGGUAN" -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek); calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0)
                calendar.timeInMillis
            }
            "BULANAN" -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1); calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0)
                calendar.timeInMillis
            }
            else -> 0L
        }

        // --- UPDATE: Menggunakan Query Database langsung agar data Real-Time ---
        val totalExpense = transaksiDao.getExpenseSumInRange(startTime, endTime) ?: 0.0

        val context = getApplication<Application>().applicationContext

        // Logic Peringatan (Menggunakan >= agar lebih ketat)
        if (period == "BULANAN" && totalExpense >= (limit * 0.9) && totalExpense < limit) {
            val sisa = limit - totalExpense
            showNotification(context, "📉 Status Anggaran Bulanan", "Anda telah menggunakan 90% anggaran. Sisa: ${formatRupiah(sisa)}")
        } else if (totalExpense >= limit) {
            val title = if (period == "HARIAN") "⚠️ Peringatan Pengeluaran Harian" else if (period == "MINGGUAN") "📅 Evaluasi Mingguan" else "📉 Budget Terlampaui"
            val msg = if (period == "HARIAN") "Hati-hati! Pengeluaran hari ini (${formatRupiah(totalExpense)}) telah mencapai/melebihi batas ${formatRupiah(limit)}."
            else "Pengeluaran periode ini sudah di atas target (${formatRupiah(limit)})."

            showNotification(context, title, msg)
        }
    }

    fun addCategory(name: String, iconName: String, type: String) {
        viewModelScope.launch { categoryDao.insertCategory(Category(name = name, iconName = iconName, type = type)) }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch { categoryDao.deleteCategory(category) }
    }

    fun deleteTransaksiObj(item: TransaksiDetail) {
        viewModelScope.launch {
            val t = Transaksi(id=item.id, amount=item.amount, note=item.note, date=item.date, type=item.type, categoryId=0, walletId=0)
            transaksiDao.deleteTransaksi(t)
        }
    }

    fun checkLoginStatus(onResult: (Boolean) -> Unit) {
        val savedUsername = prefs.getString("username", null)
        if (savedUsername != null) {
            viewModelScope.launch {
                val user = userDao.checkUserExists(savedUsername)
                if (user != null) { currentUser = user; onResult(true) } else { onResult(false) }
            }
        } else { onResult(false) }
    }

    fun login(u: String, p: String, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            val user = userDao.loginUser(u, p)
            if (user != null) { currentUser = user; prefs.edit().putString("username", u).apply(); onSuccess() } else { onError() }
        }
    }

    fun register(u: String, p: String, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            if (userDao.checkUserExists(u) == null) { userDao.registerUser(User(username = u, password = p)); onSuccess() } else { onError() }
        }
    }

    fun updateProfilePhoto(uri: String) {
        currentUser?.let { user -> viewModelScope.launch { userDao.updateFoto(user.id, uri); currentUser = user.copy(fotoUri = uri) } }
    }

    fun updatePassword(newPass: String) {
        currentUser?.let { user -> viewModelScope.launch { userDao.updatePassword(user.id, newPass); currentUser = user.copy(password = newPass) } }
    }

    fun logout() { currentUser = null; prefs.edit().clear().apply() }
}

class MainViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}