package com.example.myledger.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.myledger.MainActivity
import com.example.myledger.R
import com.example.myledger.data.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class LedgerWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val now = Calendar.getInstance()
                val transactions = AppDatabase.getDatabase(context).transactionDao()
                    .getAllTransactionsWithAccount().first()
                val expense = transactions.filter {
                    val date = Calendar.getInstance().apply { timeInMillis = it.transaction.date }
                    it.transaction.type == "EXPENSE" && date.get(Calendar.YEAR) == now.get(Calendar.YEAR) && date.get(Calendar.MONTH) == now.get(Calendar.MONTH)
                }.sumOf { it.transaction.amount }
                val budget = context.getSharedPreferences("settings", Context.MODE_PRIVATE).getFloat("monthly_budget", 0f).toDouble()
                ids.forEach { updateWidget(context, manager, it, expense, budget) }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun updateWidget(context: Context, manager: AppWidgetManager, id: Int, expense: Double, budget: Double) {
        val views = RemoteViews(context.packageName, R.layout.widget_ledger_summary)
        views.setTextViewText(R.id.widget_amount, "本月支出 ¥ ${String.format(Locale.CHINA, "%.2f", expense)}")
        val budgetText = if (budget > 0) {
            val remaining = budget - expense
            if (remaining < 0) "已超支 ¥ ${String.format(Locale.CHINA, "%.2f", -remaining)}" else "预算剩余 ¥ ${String.format(Locale.CHINA, "%.2f", remaining)}"
        } else "未设置月预算 · 点击打开记账"
        views.setTextViewText(R.id.widget_budget, budgetText)
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
        manager.updateAppWidget(id, views)
    }
}
