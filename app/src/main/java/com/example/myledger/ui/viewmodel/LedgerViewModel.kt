package com.example.myledger.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myledger.data.database.AppDatabase
import com.example.myledger.data.database.entity.AccountEntity
import com.example.myledger.data.database.entity.TransactionEntity
import com.example.myledger.data.database.model.AccountWithBalance
import com.example.myledger.data.database.model.TransactionWithAccount
import com.example.myledger.data.repository.LedgerRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LedgerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LedgerRepository

    val enabledAccountsWithBalance: StateFlow<List<AccountWithBalance>>
    val allAccountsWithBalance: StateFlow<List<AccountWithBalance>>
    val recentTransactions: StateFlow<List<TransactionWithAccount>>
    val allTransactions: StateFlow<List<TransactionWithAccount>>
    val totalIncome: StateFlow<Double>
    val totalExpense: StateFlow<Double>
    val monthlyBalance: StateFlow<Double>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = LedgerRepository(db.accountDao(), db.transactionDao())

        enabledAccountsWithBalance = repository.enabledAccountsWithBalance.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allAccountsWithBalance = repository.allAccountsWithBalance.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        recentTransactions = repository.getRecentTransactionsWithAccount(20).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allTransactions = repository.allTransactionsWithAccount.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        totalIncome = repository.totalIncome.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

        totalExpense = repository.totalExpense.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

        monthlyBalance = combine(totalIncome, totalExpense) { income, expense ->
            income - expense
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )
    }

    fun addTransaction(
        title: String,
        amount: Double,
        type: String,
        category: String,
        accountId: Long,
        note: String = "",
        date: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            repository.addTransaction(
                title = title,
                amount = amount,
                type = type,
                category = category,
                accountId = accountId,
                note = note,
                date = date
            )
        }
    }

    fun addAccount(name: String, type: String, initialBalance: Double, note: String = "") {
        viewModelScope.launch {
            repository.addAccount(name, type, initialBalance, note)
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun updateAccount(account: AccountEntity) {
        viewModelScope.launch {
            repository.updateAccount(account)
        }
    }

    fun disableAccount(accountId: Long) {
        viewModelScope.launch {
            repository.disableAccount(accountId)
        }
    }

    fun enableAccount(accountId: Long) {
        viewModelScope.launch { repository.enableAccount(accountId) }
    }

    fun deleteAccount(accountId: Long) {
        viewModelScope.launch { repository.deleteAccount(accountId) }
    }

    fun transfer(fromAccountId: Long, toAccountId: Long, amount: Double, note: String) {
        viewModelScope.launch { repository.transfer(fromAccountId, toAccountId, amount, note) }
    }
}
