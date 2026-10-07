package com.example.myledger.ui.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myledger.data.database.model.TransactionWithAccount
import com.example.myledger.data.database.entity.TransactionEntity
import com.example.myledger.data.model.Category
import com.example.myledger.ui.components.TransactionItem
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionsScreen(
    transactions: List<TransactionWithAccount>,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var transactionPendingDeletion by remember { mutableStateOf<TransactionEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var typeFilter by remember { mutableStateOf("ALL") }
    var dateFilter by remember { mutableStateOf("ALL") }
    val filteredTransactions = remember(transactions, searchQuery, typeFilter, dateFilter) {
        val startTime = when (dateFilter) {
            "TODAY" -> Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            "WEEK" -> Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            "MONTH" -> Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            else -> Long.MIN_VALUE
        }
        val keyword = searchQuery.trim()
        transactions.filter { item ->
            val transaction = item.transaction
            val categoryName = Category.entries
                .find { it.name == transaction.category }
                ?.categoryName
                .orEmpty()
            val matchesKeyword = keyword.isBlank() || listOf(
                transaction.title,
                transaction.note,
                transaction.category,
                categoryName,
                item.account?.name.orEmpty()
            ).any { it.contains(keyword, ignoreCase = true) }
            val matchesType = typeFilter == "ALL" || transaction.type == typeFilter
            matchesKeyword && matchesType && transaction.date >= startTime
        }
    }

    transactionPendingDeletion?.let { transaction ->
        AlertDialog(
            onDismissRequest = { transactionPendingDeletion = null },
            title = { Text("删除账单？") },
            text = { Text("“${transaction.title}”将被永久删除，此操作无法撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteTransaction(transaction)
                        transactionPendingDeletion = null
                    }
                ) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { transactionPendingDeletion = null }) { Text("取消") }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Column {
                Text(
                    text = "账单明细",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "全部交易记录",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("搜索账单") },
                    placeholder = { Text("名称、备注、分类或账户") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("ALL" to "全部", "EXPENSE" to "支出", "INCOME" to "收入").forEach { (value, label) ->
                        FilterChip(
                            selected = typeFilter == value,
                            onClick = { typeFilter = value },
                            label = { Text(label) }
                        )
                    }
                    listOf("ALL" to "全部时间", "TODAY" to "今天", "WEEK" to "本周", "MONTH" to "本月").forEach { (value, label) ->
                        FilterChip(
                            selected = dateFilter == value,
                            onClick = { dateFilter = value },
                            label = { Text(label) }
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (filteredTransactions.isEmpty()) {
                        Text(
                            text = if (transactions.isEmpty()) "暂无交易明细" else "没有符合条件的账单",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(24.dp)
                        )
                    } else {
                        filteredTransactions.forEachIndexed { index, item ->
                            TransactionItem(
                                item = item,
                                onEdit = { onEditTransaction(item.transaction) },
                                onDelete = { transactionPendingDeletion = item.transaction }
                            )
                            if (index < filteredTransactions.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    thickness = 0.8.dp
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
