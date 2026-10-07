package com.example.myledger.data.repository

import com.example.myledger.data.database.dao.AccountDao
import com.example.myledger.data.database.dao.TransactionDao
import com.example.myledger.data.database.entity.AccountEntity
import com.example.myledger.data.database.entity.TransactionEntity
import com.example.myledger.data.database.model.AccountWithBalance
import com.example.myledger.data.database.model.TransactionWithAccount
import kotlinx.coroutines.flow.Flow

class LedgerRepository(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao
) {
    val enabledAccounts: Flow<List<AccountEntity>> = accountDao.getEnabledAccounts()
    val allAccounts: Flow<List<AccountEntity>> = accountDao.getAllAccounts()

    val enabledAccountsWithBalance: Flow<List<AccountWithBalance>> = accountDao.getEnabledAccountsWithBalance()
    val allAccountsWithBalance: Flow<List<AccountWithBalance>> = accountDao.getAllAccountsWithBalance()

    val allTransactionsWithAccount: Flow<List<TransactionWithAccount>> = transactionDao.getAllTransactionsWithAccount()
    val totalIncome: Flow<Double> = transactionDao.getTotalIncome()
    val totalExpense: Flow<Double> = transactionDao.getTotalExpense()

    fun getRecentTransactionsWithAccount(limit: Int = 20): Flow<List<TransactionWithAccount>> {
        return transactionDao.getRecentTransactionsWithAccount(limit)
    }

    fun getTransactionsByAccount(accountId: Long): Flow<List<TransactionWithAccount>> {
        return transactionDao.getTransactionsByAccountId(accountId)
    }

    suspend fun addAccount(name: String, type: String, initialBalance: Double, note: String = ""): Long {
        return accountDao.insertAccount(
            AccountEntity(
                name = name,
                type = type,
                initialBalance = initialBalance,
                note = note
            )
        )
    }

    suspend fun updateAccount(account: AccountEntity) {
        accountDao.updateAccount(account)
    }

    suspend fun disableAccount(accountId: Long) {
        accountDao.disableAccount(accountId)
    }

    suspend fun enableAccount(accountId: Long) {
        accountDao.enableAccount(accountId)
    }

    suspend fun deleteAccount(accountId: Long) {
        accountDao.deleteAccount(accountId)
    }

    suspend fun addTransaction(
        title: String,
        amount: Double,
        type: String,
        category: String,
        accountId: Long,
        note: String = "",
        date: Long = System.currentTimeMillis()
    ): Long {
        return transactionDao.insertTransaction(
            TransactionEntity(
                title = title,
                amount = amount,
                type = type,
                category = category,
                accountId = accountId,
                note = note,
                date = date
            )
        )
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun transfer(
        fromAccountId: Long,
        toAccountId: Long,
        amount: Double,
        note: String,
        date: Long = System.currentTimeMillis()
    ) {
        transactionDao.insertTransfer(
            TransactionEntity(title = "转出", amount = amount, type = "EXPENSE", category = "OTHER", accountId = fromAccountId, note = note, date = date),
            TransactionEntity(title = "转入", amount = amount, type = "INCOME", category = "OTHER", accountId = toAccountId, note = note, date = date)
        )
    }
}
