package com.example.myledger.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myledger.data.database.dao.AccountDao
import com.example.myledger.data.database.dao.TransactionDao
import com.example.myledger.data.database.entity.AccountEntity
import com.example.myledger.data.database.entity.TransactionEntity

@Database(
    entities = [AccountEntity::class, TransactionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "my_ledger_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }

        fun closeDatabase() {
            synchronized(this) {
                INSTANCE?.close()
                INSTANCE = null
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                db.beginTransaction()
                try {
                    val now = System.currentTimeMillis()
                    val oneDay = 86400000L

                    // Seed default accounts
                    db.execSQL(
                        "INSERT INTO accounts (id, name, type, initialBalance, note, isEnabled, createdAt) VALUES " +
                        "(1, '微信', 'wechat', 1200.0, '', 1, $now), " +
                        "(2, '支付宝', 'alipay', 860.0, '', 1, $now), " +
                        "(3, '现金', 'cash', 500.0, '', 1, $now), " +
                        "(4, '招商银行卡', 'bank', 12560.0, '', 1, $now)"
                    )

                    // Seed default transactions
                    db.execSQL(
                        "INSERT INTO transactions (title, amount, type, category, accountId, note, date) VALUES " +
                        "('午餐外卖', 38.50, 'EXPENSE', 'FOOD', 1, '麻辣烫+冰红茶', ${now - 8640000L}), " +
                        "('三月工资薪酬', 18500.00, 'INCOME', 'SALARY', 4, '基本工资+绩效奖金', ${now - 43200000L}), " +
                        "('超市周日大采购', 268.40, 'EXPENSE', 'SHOPPING', 2, '水果蔬菜与零食', ${now - oneDay}), " +
                        "('地铁交通卡充值', 100.00, 'EXPENSE', 'TRANSPORT', 1, '交通出行', ${now - oneDay * 2}), " +
                        "('房租及物业费', 3200.00, 'EXPENSE', 'HOUSING', 4, '三月份房租', ${now - oneDay * 5}), " +
                        "('电影院观影', 96.00, 'EXPENSE', 'ENTERTAINMENT', 1, '两张电影票', ${now - oneDay * 6})"
                    )

                    db.setTransactionSuccessful()
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    db.endTransaction()
                }
            }
        }
    }
}
