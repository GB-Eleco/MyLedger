package com.example.myledger.data.backup

import android.content.ContentResolver
import android.net.Uri
import com.example.myledger.data.database.model.TransactionWithAccount
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {
    fun exportTransactions(contentResolver: ContentResolver, uri: Uri, transactions: List<TransactionWithAccount>) {
        contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { writer ->
            writer.write("\uFEFF日期,类型,名称,金额,分类,账户,备注\n")
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
            transactions.forEach { item ->
                val transaction = item.transaction
                val values = listOf(
                    format.format(Date(transaction.date)),
                    if (transaction.type == "INCOME") "收入" else "支出",
                    transaction.title,
                    String.format(Locale.US, "%.2f", transaction.amount),
                    transaction.category,
                    item.account?.name ?: "已删除账户",
                    transaction.note
                )
                writer.write(values.joinToString(",") { csvValue(it) })
                writer.newLine()
            }
        }
    }

    private fun csvValue(value: String): String = "\"${value.replace("\"", "\"\"")}\""
}
