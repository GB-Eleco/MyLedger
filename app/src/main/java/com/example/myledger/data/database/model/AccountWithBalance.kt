package com.example.myledger.data.database.model

import androidx.room.Embedded
import com.example.myledger.data.database.entity.AccountEntity
import java.util.Locale

data class AccountWithBalance(
    @Embedded val account: AccountEntity,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0
) {
    val currentBalance: Double
        get() = account.initialBalance + totalIncome - totalExpense

    val formattedCurrentBalance: String
        get() {
            val prefix = if (currentBalance >= 0) "¥ " else "-¥ "
            return "$prefix${String.format(Locale.CHINA, "%,.2f", Math.abs(currentBalance))}"
        }
}
