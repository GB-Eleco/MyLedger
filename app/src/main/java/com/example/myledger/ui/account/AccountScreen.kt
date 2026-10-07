package com.example.myledger.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myledger.data.database.entity.AccountEntity
import com.example.myledger.data.database.model.AccountWithBalance
import com.example.myledger.data.database.model.TransactionWithAccount
import com.example.myledger.data.model.AccountType
import com.example.myledger.ui.components.TransactionItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    accountsWithBalance: List<AccountWithBalance>,
    allTransactions: List<TransactionWithAccount>,
    onAddAccount: (name: String, type: String, initialBalance: Double, note: String) -> Unit,
    onUpdateAccount: (AccountEntity) -> Unit,
    onDisableAccount: (Long) -> Unit,
    onEnableAccount: (Long) -> Unit,
    onDeleteAccount: (Long) -> Unit,
    onTransfer: (Long, Long, Double, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<AccountEntity?>(null) }
    var selectedAccountForTransactions by remember { mutableStateOf<AccountWithBalance?>(null) }
    var accountPendingDelete by remember { mutableStateOf<AccountWithBalance?>(null) }
    var showTransferDialog by remember { mutableStateOf(false) }
    val totalBalance = accountsWithBalance.sumOf { it.currentBalance }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "资金管理",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "我的账户",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("新增账户")
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("总金额", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "¥ ${String.format(java.util.Locale.CHINA, "%,.2f", totalBalance)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("包含已停用账户的历史余额", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            Button(onClick = { showTransferDialog = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                Icon(Icons.Default.SwapHoriz, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("账户转账")
            }
        }


        items(accountsWithBalance, key = { it.account.id }) { item ->
            AccountCardItem(
                accountWithBalance = item,
                onEditClick = { editingAccount = item.account },
                onDisableClick = { onDisableAccount(item.account.id) },
                onEnableClick = { onEnableAccount(item.account.id) },
                onDeleteClick = { accountPendingDelete = item },
                onViewTransactionsClick = { selectedAccountForTransactions = item }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Add Account Dialog
    if (showAddDialog) {
        AccountEditDialog(
            title = "新增账户",
            initialName = "",
            initialType = AccountType.WECHAT.code,
            initialBalanceStr = "0",
            initialNote = "",
            onDismiss = { showAddDialog = false },
            onConfirm = { name, type, balance, note ->
                onAddAccount(name, type, balance, note)
                showAddDialog = false
            }
        )
    }

    // Edit Account Dialog
    editingAccount?.let { acc ->
        AccountEditDialog(
            title = "编辑账户",
            initialName = acc.name,
            initialType = acc.type,
            initialBalanceStr = acc.initialBalance.toString(),
            initialNote = acc.note,
            onDismiss = { editingAccount = null },
            onConfirm = { name, type, balance, note ->
                onUpdateAccount(
                    acc.copy(
                        name = name,
                        type = type,
                        initialBalance = balance,
                        note = note
                    )
                )
                editingAccount = null
            }
        )
    }

    // Account Transactions Sheet
    selectedAccountForTransactions?.let { accWithBalance ->
        val accountTransactions = allTransactions.filter { it.transaction.accountId == accWithBalance.account.id }
        AccountTransactionsSheet(
            accountWithBalance = accWithBalance,
            transactions = accountTransactions,
            onDismiss = { selectedAccountForTransactions = null }
        )
    }

    accountPendingDelete?.let { item ->
        val hasTransactions = allTransactions.any { it.transaction.accountId == item.account.id }
        AlertDialog(
            onDismissRequest = { accountPendingDelete = null },
            title = { Text(if (hasTransactions) "无法删除账户" else "删除账户？") },
            text = { Text(if (hasTransactions) "该账户已有账单记录。停用账户可保留历史账单，并可随时恢复。" else "“${item.account.name}”将被永久删除。") },
            confirmButton = { TextButton(onClick = { if (!hasTransactions) onDeleteAccount(item.account.id); accountPendingDelete = null }) { Text(if (hasTransactions) "知道了" else "删除") } },
            dismissButton = if (hasTransactions) null else ({ TextButton(onClick = { accountPendingDelete = null }) { Text("取消") } })
        )
    }

    if (showTransferDialog) {
        TransferDialog(
            accounts = accountsWithBalance.filter { it.account.isEnabled }.map { it.account },
            onDismiss = { showTransferDialog = false },
            onConfirm = { from, to, amount, note ->
                onTransfer(from, to, amount, note)
                showTransferDialog = false
            }
        )
    }
}

@Composable
private fun AccountCardItem(
    accountWithBalance: AccountWithBalance,
    onEditClick: () -> Unit,
    onDisableClick: () -> Unit,
    onEnableClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onViewTransactionsClick: () -> Unit
) {
    val account = accountWithBalance.account
    val accountType = AccountType.fromCode(account.type)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewTransactionsClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Account Icon Box
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            color = accountType.color.copy(alpha = 0.15f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = accountType.icon,
                        contentDescription = account.name,
                        tint = accountType.color,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = account.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "初始余额 ¥${String.format(java.util.Locale.CHINA, "%.2f", account.initialBalance)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Actions Menu
                Row {
                    IconButton(onClick = onViewTransactionsClick) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = "查看账单",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "编辑账户",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = if (account.isEnabled) onDisableClick else onEnableClick) {
                        Icon(
                            imageVector = if (account.isEnabled) Icons.Default.PowerOff else Icons.Default.Restore,
                            contentDescription = if (account.isEnabled) "停用账户" else "恢复账户",
                            tint = if (account.isEnabled) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, contentDescription = "删除账户", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            Spacer(modifier = Modifier.height(14.dp))

            // Balance Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "当前余额",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = accountWithBalance.formattedCurrentBalance,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun TransferDialog(
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onConfirm: (Long, Long, Double, String) -> Unit
) {
    var from by remember(accounts) { mutableStateOf(accounts.firstOrNull()) }
    var to by remember(accounts) { mutableStateOf(accounts.drop(1).firstOrNull()) }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectingFrom by remember { mutableStateOf(false) }
    var selectingTo by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("账户转账") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AccountPicker("转出账户", from, accounts, selectingFrom, { selectingFrom = it }, { from = it })
                AccountPicker("转入账户", to, accounts.filter { it.id != from?.id }, selectingTo, { selectingTo = it }, { to = it })
                OutlinedTextField(amount, { amount = it }, label = { Text("转账金额 (¥)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                OutlinedTextField(note, { note = it }, label = { Text("备注 (可选)") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(from!!.id, to!!.id, amount.toDouble(), note.ifBlank { "账户转账" }) }, enabled = from != null && to != null && from?.id != to?.id && (amount.toDoubleOrNull() ?: 0.0) > 0) { Text("确认转账") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

@Composable
private fun AccountPicker(
    label: String,
    selected: AccountEntity?,
    accounts: List<AccountEntity>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelected: (AccountEntity) -> Unit
) {
    Box {
        Button(
            onClick = { onExpandedChange(true) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("$label：${selected?.name ?: "请选择"}", modifier = Modifier.weight(1f))
            Icon(Icons.Default.ArrowDropDown, contentDescription = "选择$label")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
            accounts.forEach { account -> DropdownMenuItem(text = { Text(account.name) }, onClick = { onSelected(account); onExpandedChange(false) }) }
        }
    }
}

@Composable
private fun AccountEditDialog(
    title: String,
    initialName: String,
    initialType: String,
    initialBalanceStr: String,
    initialNote: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: String, initialBalance: Double, note: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var selectedType by remember { mutableStateOf(initialType) }
    var balanceText by remember { mutableStateOf(initialBalanceStr) }
    var note by remember { mutableStateOf(initialNote) }
    var isTypeMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("账户名称 (如: 招商银行卡)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Type selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = AccountType.fromCode(selectedType).typeName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("账户类型") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isTypeMenuExpanded = true },
                        trailingIcon = {
                            IconButton(onClick = { isTypeMenuExpanded = true }) {
                                Icon(imageVector = AccountType.fromCode(selectedType).icon, contentDescription = null)
                            }
                        }
                    )

                    DropdownMenu(
                        expanded = isTypeMenuExpanded,
                        onDismissRequest = { isTypeMenuExpanded = false }
                    ) {
                        AccountType.entries.forEach { accType ->
                            DropdownMenuItem(
                                text = { Text(accType.typeName) },
                                leadingIcon = { Icon(imageVector = accType.icon, contentDescription = null, tint = accType.color) },
                                onClick = {
                                    selectedType = accType.code
                                    isTypeMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { balanceText = it },
                    label = { Text("初始余额 (¥)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注 (可选)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val balance = balanceText.toDoubleOrNull() ?: 0.0
                    onConfirm(name.ifBlank { AccountType.fromCode(selectedType).typeName }, selectedType, balance, note)
                },
                enabled = name.isNotBlank() || selectedType.isNotBlank()
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountTransactionsSheet(
    accountWithBalance: AccountWithBalance,
    transactions: List<TransactionWithAccount>,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "${accountWithBalance.account.name} · 关联账单",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "共 ${transactions.size} 笔明细 | 当前余额 ${accountWithBalance.formattedCurrentBalance}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无该账户关联交易",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(transactions) { item ->
                        TransactionItem(item = item)
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
