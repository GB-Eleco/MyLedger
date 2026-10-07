package com.example.myledger.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myledger.data.database.entity.AccountEntity
import com.example.myledger.data.database.model.AccountWithBalance
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountEntity>)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Query("SELECT * FROM accounts WHERE isEnabled = 1 ORDER BY id ASC")
    fun getEnabledAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts ORDER BY id ASC")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: Long): AccountEntity?

    @Query("UPDATE accounts SET isEnabled = 0 WHERE id = :id")
    suspend fun disableAccount(id: Long)

    @Query("UPDATE accounts SET isEnabled = 1 WHERE id = :id")
    suspend fun enableAccount(id: Long)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteAccount(id: Long)

    @Query("""
        SELECT a.*, 
            COALESCE((SELECT SUM(t.amount) FROM transactions t WHERE t.accountId = a.id AND t.type = 'INCOME'), 0.0) AS totalIncome,
            COALESCE((SELECT SUM(t.amount) FROM transactions t WHERE t.accountId = a.id AND t.type = 'EXPENSE'), 0.0) AS totalExpense
        FROM accounts a
        ORDER BY a.id ASC
    """)
    fun getAllAccountsWithBalance(): Flow<List<AccountWithBalance>>

    @Query("""
        SELECT a.*, 
            COALESCE((SELECT SUM(t.amount) FROM transactions t WHERE t.accountId = a.id AND t.type = 'INCOME'), 0.0) AS totalIncome,
            COALESCE((SELECT SUM(t.amount) FROM transactions t WHERE t.accountId = a.id AND t.type = 'EXPENSE'), 0.0) AS totalExpense
        FROM accounts a
        WHERE a.isEnabled = 1
        ORDER BY a.id ASC
    """)
    fun getEnabledAccountsWithBalance(): Flow<List<AccountWithBalance>>

    @Query("""
        SELECT a.*, 
            COALESCE((SELECT SUM(t.amount) FROM transactions t WHERE t.accountId = a.id AND t.type = 'INCOME'), 0.0) AS totalIncome,
            COALESCE((SELECT SUM(t.amount) FROM transactions t WHERE t.accountId = a.id AND t.type = 'EXPENSE'), 0.0) AS totalExpense
        FROM accounts a
        WHERE a.id = :id
    """)
    fun getAccountWithBalanceById(id: Long): Flow<AccountWithBalance?>

    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun getAccountCount(): Int
}
