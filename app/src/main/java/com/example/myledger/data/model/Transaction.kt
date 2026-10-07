package com.example.myledger.data.model

import java.util.Locale

enum class TransactionType {
    EXPENSE,
    INCOME
}

data class Transaction(
    val id: String,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: Category,
    val dateStr: String,
    val note: String = ""
) {
    val formattedAmount: String
        get() {
            val prefix = if (type == TransactionType.INCOME) "+¥ " else "-¥ "
            return "$prefix${String.format(Locale.CHINA, "%.2f", amount)}"
        }
}

data class MonthlySummary(
    val totalIncome: Double,
    val totalExpense: Double
) {
    val balance: Double
        get() = totalIncome - totalExpense

    val formattedBalance: String
        get() {
            val prefix = if (balance >= 0) "¥ " else "-¥ "
            return "$prefix${String.format(Locale.CHINA, "%,.2f", Math.abs(balance))}"
        }

    val formattedIncome: String
        get() = "¥ ${String.format(Locale.CHINA, "%,.2f", totalIncome)}"

    val formattedExpense: String
        get() = "¥ ${String.format(Locale.CHINA, "%,.2f", totalExpense)}"
}
