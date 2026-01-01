package com.example.catatankeuangan

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransaksiDao {
    @Query("""
        SELECT t.id, t.amount, t.note, t.date, t.type, 
               c.name as categoryName, c.iconName as categoryIcon, 
               w.name as walletName
        FROM tabel_transaksi t
        INNER JOIN tabel_category c ON t.categoryId = c.id
        INNER JOIN tabel_wallet w ON t.walletId = w.id
        ORDER BY t.date DESC
    """)
    fun getAllTransaksiDetail(): Flow<List<TransaksiDetail>>

    @Insert
    suspend fun insertTransaksi(transaksi: Transaksi)

    @Delete
    suspend fun deleteTransaksi(transaksi: Transaksi)

    // --- BARU: Query untuk menghitung total pengeluaran secara langsung ---
    @Query("SELECT SUM(amount) FROM tabel_transaksi WHERE type = 'PENGELUARAN' AND date BETWEEN :startDate AND :endDate")
    suspend fun getExpenseSumInRange(startDate: Long, endDate: Long): Double?
}

@Dao
interface WalletDao {
    @Query("SELECT * FROM tabel_wallet")
    fun getAllWallets(): Flow<List<Wallet>>

    @Insert
    suspend fun insertWallet(wallet: Wallet)

    @Query("SELECT COUNT(*) FROM tabel_wallet")
    suspend fun getCount(): Int
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM tabel_category WHERE type = :type")
    fun getCategoriesByType(type: String): Flow<List<Category>>

    @Insert
    suspend fun insertCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("SELECT COUNT(*) FROM tabel_category")
    suspend fun getCount(): Int
}

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun registerUser(user: User)
    @Query("SELECT * FROM tabel_user WHERE username = :u AND password = :p LIMIT 1")
    suspend fun loginUser(u: String, p: String): User?
    @Query("SELECT * FROM tabel_user WHERE username = :u")
    suspend fun checkUserExists(u: String): User?
    @Query("UPDATE tabel_user SET fotoUri = :uri WHERE id = :id")
    suspend fun updateFoto(id: Long, uri: String)
    @Query("UPDATE tabel_user SET password = :newPass WHERE id = :id")
    suspend fun updatePassword(id: Long, newPass: String)
}

@Database(entities = [Transaksi::class, User::class, Wallet::class, Category::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transaksiDao(): TransaksiDao
    abstract fun userDao(): UserDao
    abstract fun walletDao(): WalletDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "catatan_keuangan_db"
                )
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
        }
    }
}