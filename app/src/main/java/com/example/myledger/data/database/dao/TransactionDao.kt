package com.example.myledger.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.myledger.data.database.entity.TransactionEntity
import com.example.myledger.data.database.model.TransactionWithAccount
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Transaction
    suspend fun insertTransfer(outgoing: TransactionEntity, incoming: TransactionEntity) {
        insertTransaction(outgoing)
        insertTransaction(incoming)
    }

    @Transaction
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactionsWithAccount(): Flow<List<TransactionWithAccount>>

    @Transaction
    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT :limit")
    fun getRecentTransactionsWithAccount(limit: Int): Flow<List<TransactionWithAccount>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY date DESC")
    fun getTransactionsByAccountId(accountId: Long): Flow<List<TransactionWithAccount>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE accountId = :accountId AND type = 'INCOME'")
    fun getTotalIncomeByAccount(accountId: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE accountId = :accountId AND type = 'EXPENSE'")
    fun getTotalExpenseByAccount(accountId: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE type = 'INCOME'")
    fun getTotalIncome(): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE type = 'EXPENSE'")
    fun getTotalExpense(): Flow<Double>
}
