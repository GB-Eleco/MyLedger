package com.example.myledger.ui.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myledger.data.database.entity.AccountEntity
import com.example.myledger.data.database.model.TransactionWithAccount
import com.example.myledger.data.model.Category
import com.example.myledger.ui.theme.LocalFinancialColors
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max

@Composable
fun StatisticsScreen(
    transactions: List<TransactionWithAccount>,
    accounts: List<AccountEntity>,
    modifier: Modifier = Modifier
) {
    var month by remember { mutableStateOf(YearMonth.now()) }
    val zone = remember { ZoneId.systemDefault() }
    val monthItems = remember(transactions, month) {
        transactions.filter { YearMonth.from(Instant.ofEpochMilli(it.transaction.date).atZone(zone)) == month }
    }
    val income = monthItems.filter { it.transaction.type == "INCOME" }.sumOf { it.transaction.amount }
    val expense = monthItems.filter { it.transaction.type == "EXPENSE" }.sumOf { it.transaction.amount }
    val dailyStats = remember(monthItems, month) {
        (1..month.lengthOfMonth()).map { day ->
            val items = monthItems.filter { Instant.ofEpochMilli(it.transaction.date).atZone(zone).dayOfMonth == day }
            DailyStat(day, items.filter { it.transaction.type == "INCOME" }.sumOf { it.transaction.amount }, items.filter { it.transaction.type == "EXPENSE" }.sumOf { it.transaction.amount })
        }
    }
    val categoryStats = remember(monthItems, expense) {
        Category.entries.map { category ->
            val amount = monthItems.filter { it.transaction.type == "EXPENSE" && Category.fromCategoryName(it.transaction.category) == category }.sumOf { it.transaction.amount }
            CategoryStat(category, amount, if (expense == 0.0) 0f else (amount / expense).toFloat())
        }.filter { it.amount > 0 }.sortedByDescending { it.amount }
    }
    val accountStats = remember(transactions, accounts, month) {
        val monthStart = month.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val monthEnd = month.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        accounts.map { account ->
            val items = transactions.filter { it.transaction.accountId == account.id }
            fun signed(item: TransactionWithAccount) = if (item.transaction.type == "INCOME") item.transaction.amount else -item.transaction.amount
            AccountBalanceStat(account, account.initialBalance + items.filter { it.transaction.date < monthStart }.sumOf(::signed), items.filter { it.transaction.date in monthStart until monthEnd }.sumOf(::signed))
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(Modifier.height(12.dp))
            Text("收支分析", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("统计数据", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 26.sp))
        }
        item { MonthSelector(month, { month = month.minusMonths(1) }, { month = month.plusMonths(1) }) }
        item { SummaryCard(income, expense) }
        item {
            SectionTitle("按日收支趋势")
            StatCard { if (monthItems.isEmpty()) EmptyState("该月暂无收支数据") else DailyTrendChart(dailyStats) }
        }
        item {
            SectionTitle("支出分类占比")
            StatCard {
                if (categoryStats.isEmpty()) EmptyState("该月暂无支出数据") else Row(
                    Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CategoryPieChart(categoryStats, Modifier.size(156.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        categoryStats.take(5).forEach { stat ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(10.dp).background(stat.category.iconColor, CircleShape))
                                Spacer(Modifier.width(6.dp))
                                Text(stat.category.categoryName, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                Text("${String.format(Locale.CHINA, "%.0f", stat.percentage * 100)}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
        item {
            SectionTitle("账户余额变化")
            StatCard {
                if (accountStats.isEmpty()) EmptyState("暂无账户数据") else Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    accountStats.forEach { AccountBalanceRow(it) }
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable private fun MonthSelector(month: YearMonth, previous: () -> Unit, next: () -> Unit) {
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            IconButton(onClick = previous) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "上个月") }
            Text(month.format(DateTimeFormatter.ofPattern("yyyy年MM月", Locale.CHINA)), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            IconButton(onClick = next) { Icon(Icons.AutoMirrored.Filled.ArrowForward, "下个月") }
        }
    }
}

@Composable private fun SummaryCard(income: Double, expense: Double) {
    val colors = LocalFinancialColors.current
    val balance = income - expense
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Text("本月结余", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(money(balance), style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = if (balance >= 0) colors.income else colors.expense)
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryValue("收入", income, colors.income); SummaryValue("支出", expense, colors.expense)
            }
        }
    }
}

@Composable private fun SummaryValue(label: String, value: Double, color: Color) {
    Column { Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(money(value), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color) }
}
@Composable private fun SectionTitle(text: String) { Text(text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp)) }
@Composable private fun StatCard(content: @Composable () -> Unit) { Card(Modifier.fillMaxWidth(), RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Column(Modifier.fillMaxWidth().padding(16.dp)) { content() } } }
@Composable private fun EmptyState(text: String) { Box(Modifier.fillMaxWidth().height(130.dp), contentAlignment = Alignment.Center) { Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant) } }

@Composable private fun DailyTrendChart(stats: List<DailyStat>) {
    val colors = LocalFinancialColors.current
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = .16f)
    val maxValue = max(1.0, stats.maxOf { max(it.income, it.expense) })
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) { Legend("收入", colors.income); Legend("支出", colors.expense) }
        Spacer(Modifier.height(8.dp))
        Canvas(Modifier.fillMaxWidth().height(180.dp)) {
            val p = 8.dp.toPx(); val chartHeight = size.height - 18.dp.toPx(); val step = (size.width - p * 2) / stats.size; val bar = max(2.dp.toPx(), step * .28f)
            repeat(3) { i -> val y = chartHeight * i / 2; drawLine(gridColor, androidx.compose.ui.geometry.Offset(p, y), androidx.compose.ui.geometry.Offset(size.width - p, y), 1.dp.toPx()) }
            stats.forEachIndexed { i, stat ->
                val x = p + step * i + step / 2; val ih = (stat.income / maxValue * chartHeight).toFloat(); val eh = (stat.expense / maxValue * chartHeight).toFloat()
                drawRect(colors.income, androidx.compose.ui.geometry.Offset(x - bar, chartHeight - ih), androidx.compose.ui.geometry.Size(bar - 1.dp.toPx(), ih))
                drawRect(colors.expense, androidx.compose.ui.geometry.Offset(x + 1.dp.toPx(), chartHeight - eh), androidx.compose.ui.geometry.Size(bar - 1.dp.toPx(), eh))
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("1日", style = MaterialTheme.typography.labelSmall); Text("${stats.size}日", style = MaterialTheme.typography.labelSmall) }
    }
}

@Composable private fun Legend(label: String, color: Color) { Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(8.dp).background(color, CircleShape)); Spacer(Modifier.width(4.dp)); Text(label, style = MaterialTheme.typography.labelSmall) } }

@Composable private fun CategoryPieChart(stats: List<CategoryStat>, modifier: Modifier) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    Canvas(modifier) {
        var start = -90f
        stats.forEach { stat -> val sweep = stat.percentage * 360f; drawArc(stat.category.iconColor, start, sweep, true); start += sweep }
        drawCircle(surfaceColor, radius = size.minDimension * .27f)
    }
}

@Composable private fun AccountBalanceRow(stat: AccountBalanceStat) {
    val colors = LocalFinancialColors.current
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stat.account.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text((if (stat.change >= 0) "+" else "") + money(stat.change), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = if (stat.change >= 0) colors.income else colors.expense)
        }
        Spacer(Modifier.height(4.dp))
        Text("期初 ${money(stat.opening)}  →  月末 ${money(stat.closing)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun money(value: Double) = "¥ ${String.format(Locale.CHINA, "%,.2f", value)}"
private data class DailyStat(val day: Int, val income: Double, val expense: Double)
private data class CategoryStat(val category: Category, val amount: Double, val percentage: Float)
private data class AccountBalanceStat(val account: AccountEntity, val opening: Double, val change: Double) { val closing get() = opening + change }
