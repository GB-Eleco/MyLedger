package com.example.myledger.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.myledger.data.database.entity.TransactionEntity
import com.example.myledger.data.backup.CsvExporter
import com.example.myledger.data.backup.DatabaseBackupManager
import com.example.myledger.data.database.model.TransactionWithAccount
import com.example.myledger.data.model.MonthlySummary
import com.example.myledger.ui.account.AccountScreen
import com.example.myledger.ui.components.AddTransactionSheet
import com.example.myledger.ui.home.HomeScreen
import com.example.myledger.ui.statistics.StatisticsScreen
import com.example.myledger.ui.settings.SettingsScreen
import com.example.myledger.ui.transactions.TransactionsScreen
import com.example.myledger.ui.viewmodel.LedgerViewModel

enum class BottomTab(
    val label: String,
    val icon: ImageVector
) {
    HOME("首页", Icons.Default.Home),
    TRANSACTIONS("账单", Icons.Default.ReceiptLong),
    STATISTICS("统计", Icons.Default.BarChart),
    ACCOUNT("账户", Icons.Default.AccountBalanceWallet),
    SETTINGS("设置", Icons.Default.Settings)
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: LedgerViewModel = viewModel(),
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    reverseColors: Boolean,
    onReverseColorsChange: (Boolean) -> Unit
) {
    var selectedTabItem by remember { mutableIntStateOf(0) }
    var showAddTransactionSheet by remember { mutableStateOf(false) }
    var transactionBeingEdited by remember { mutableStateOf<TransactionEntity?>(null) }
    var exportTransactions by remember { mutableStateOf<List<TransactionWithAccount>>(emptyList()) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE) }
    var monthlyBudget by remember { mutableStateOf(preferences.getFloat("monthly_budget", 0f).toDouble()) }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri?.let { CsvExporter.exportTransactions(context.contentResolver, it, exportTransactions) }
    }
    val databaseExportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        uri?.let { DatabaseBackupManager.export(context, it) }
    }
    val databaseImportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            DatabaseBackupManager.restore(context, it)
            (context as? android.app.Activity)?.recreate()
        }
    }

    // ViewModel StateFlow Observations
    val enabledAccountsWithBalance by viewModel.enabledAccountsWithBalance.collectAsState()
    val allAccountsWithBalance by viewModel.allAccountsWithBalance.collectAsState()
    val recentTransactions by viewModel.recentTransactions.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val currentMonthExpense = remember(allTransactions) {
        val now = java.util.Calendar.getInstance()
        allTransactions.filter {
            val date = java.util.Calendar.getInstance().apply { timeInMillis = it.transaction.date }
            it.transaction.type == "EXPENSE" && date.get(java.util.Calendar.YEAR) == now.get(java.util.Calendar.YEAR) && date.get(java.util.Calendar.MONTH) == now.get(java.util.Calendar.MONTH)
        }.sumOf { it.transaction.amount }
    }

    val monthlySummary = remember(totalIncome, totalExpense) {
        MonthlySummary(
            totalIncome = totalIncome,
            totalExpense = totalExpense
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                BottomTab.entries.forEachIndexed { index, tab ->
                    val isSelected = selectedTabItem == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTabItem = index },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    transactionBeingEdited = null
                    showAddTransactionSheet = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加账单"
                )
            }
        }
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)
        when (selectedTabItem) {
            0 -> HomeScreen(
                summary = monthlySummary,
                recentTransactions = recentTransactions,
                monthlyBudget = monthlyBudget,
                monthlyExpense = currentMonthExpense,
                modifier = contentModifier,
                onNavigateToTransactions = { selectedTabItem = 1 }
            )
            1 -> TransactionsScreen(
                transactions = allTransactions,
                onEditTransaction = { transaction ->
                    transactionBeingEdited = transaction
                    showAddTransactionSheet = true
                },
                onDeleteTransaction = { transaction ->
                    viewModel.deleteTransaction(transaction)
                    scope.launch {
                        if (snackbarHostState.showSnackbar("账单已删除", "撤销") == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                            viewModel.addTransaction(transaction.title, transaction.amount, transaction.type, transaction.category, transaction.accountId, transaction.note, transaction.date)
                        }
                    }
                },
                modifier = contentModifier
            )
            2 -> StatisticsScreen(
                transactions = allTransactions,
                accounts = allAccountsWithBalance.map { it.account },
                modifier = contentModifier
            )
            3 -> AccountScreen(
                accountsWithBalance = allAccountsWithBalance,
                allTransactions = allTransactions,
                onAddAccount = { name, type, initialBalance, note ->
                    viewModel.addAccount(name, type, initialBalance, note)
                },
                onUpdateAccount = { account ->
                    viewModel.updateAccount(account)
                },
                onDisableAccount = { accountId ->
                    viewModel.disableAccount(accountId)
                },
                onEnableAccount = viewModel::enableAccount,
                onDeleteAccount = viewModel::deleteAccount,
                onTransfer = viewModel::transfer,
                modifier = contentModifier
            )
            4 -> SettingsScreen(
                onExportCsv = {
                    exportTransactions = allTransactions
                    exportLauncher.launch("MyLedger-${System.currentTimeMillis()}.csv")
                },
                onExportDatabase = { databaseExportLauncher.launch("MyLedger-backup.db") },
                onImportDatabase = { databaseImportLauncher.launch(arrayOf("application/octet-stream", "application/x-sqlite3")) },
                darkTheme = darkTheme,
                onDarkThemeChange = onDarkThemeChange,
                reverseColors = reverseColors,
                onReverseColorsChange = onReverseColorsChange,
                monthlyBudget = monthlyBudget,
                onMonthlyBudgetChange = { value -> monthlyBudget = value; preferences.edit().putFloat("monthly_budget", value.toFloat()).apply() },
                modifier = contentModifier
            )
        }

        // Add Transaction Sheet Modal
        if (showAddTransactionSheet) {
            AddTransactionSheet(
                accounts = if (transactionBeingEdited == null) {
                    enabledAccountsWithBalance.map { it.account }
                } else {
                    allAccountsWithBalance.map { it.account }
                },
                transaction = transactionBeingEdited,
                onDismiss = {
                    showAddTransactionSheet = false
                    transactionBeingEdited = null
                },
                onSaveTransaction = { title, amount, type, category, accountId, note, date ->
                    transactionBeingEdited?.let { original ->
                        viewModel.updateTransaction(
                            original.copy(
                                title = title,
                                amount = amount,
                                type = type,
                                category = category,
                                accountId = accountId,
                                note = note,
                                date = date
                            )
                        )
                    } ?: viewModel.addTransaction(title, amount, type, category, accountId, note, date)
                }
            )
        }
    }
}
