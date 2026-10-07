package com.example.myledger.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    onExportCsv: () -> Unit,
    onExportDatabase: () -> Unit,
    onImportDatabase: () -> Unit,
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    reverseColors: Boolean,
    onReverseColorsChange: (Boolean) -> Unit,
    monthlyBudget: Double,
    onMonthlyBudgetChange: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Text("设置", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("外观", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                SettingSwitch("深色主题", darkTheme, onDarkThemeChange)
                SettingSwitch("收支颜色互换（收入红色 / 支出绿色）", reverseColors, onReverseColorsChange)
            }
        }
        val budgetText = remember(monthlyBudget) { mutableStateOf(if (monthlyBudget > 0) monthlyBudget.toString() else "") }
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("月度预算", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                OutlinedTextField(budgetText.value, { budgetText.value = it }, label = { Text("预算金额（留空表示不设限）") })
                Button(onClick = { onMonthlyBudgetChange(budgetText.value.toDoubleOrNull() ?: 0.0) }, modifier = Modifier.fillMaxWidth()) { Text("保存预算") }
            }
        }
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("数据与备份", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("CSV 可直接用 Excel 打开；数据库备份可完整保留账户与账单。恢复会覆盖当前本地数据。", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onClick = onExportCsv, modifier = Modifier.fillMaxWidth()) { Text("导出账单 CSV（Excel）") }
                Button(onClick = onExportDatabase, modifier = Modifier.fillMaxWidth()) { Text("备份本地数据库") }
                Button(onClick = onImportDatabase, modifier = Modifier.fillMaxWidth()) { Text("恢复本地数据库") }
            }
        }
    }
}

@Composable
private fun SettingSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    androidx.compose.foundation.layout.Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
