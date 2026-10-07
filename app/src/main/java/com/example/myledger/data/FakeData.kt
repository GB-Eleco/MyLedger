package com.example.myledger.data

import com.example.myledger.data.model.Category
import com.example.myledger.data.model.MonthlySummary
import com.example.myledger.data.model.Transaction
import com.example.myledger.data.model.TransactionType

object FakeData {
    const val CURRENT_MONTH = "2025年3月"

    val recentTransactions = listOf(
        Transaction(
            id = "1",
            title = "午餐外卖",
            amount = 38.50,
            type = TransactionType.EXPENSE,
            category = Category.FOOD,
            dateStr = "今天 12:30",
            note = "麻辣烫+冰红茶"
        ),
        Transaction(
            id = "2",
            title = "三月工资薪酬",
            amount = 18500.00,
            type = TransactionType.INCOME,
            category = Category.SALARY,
            dateStr = "今天 09:15",
            note = "基本工资+绩效奖金"
        ),
        Transaction(
            id = "3",
            title = "超市周日大采购",
            amount = 268.40,
            type = TransactionType.EXPENSE,
            category = Category.SHOPPING,
            dateStr = "昨天 19:40",
            note = "水果蔬菜与零食"
        ),
        Transaction(
            id = "4",
            title = "地铁交通卡充值",
            amount = 100.00,
            type = TransactionType.EXPENSE,
            category = Category.TRANSPORT,
            dateStr = "03月08日",
            note = "交通出行"
        ),
        Transaction(
            id = "5",
            title = "房租及物业费",
            amount = 3200.00,
            type = TransactionType.EXPENSE,
            category = Category.HOUSING,
            dateStr = "03月05日",
            note = "三月份房租"
        ),
        Transaction(
            id = "6",
            title = "电影院观影",
            amount = 96.00,
            type = TransactionType.EXPENSE,
            category = Category.ENTERTAINMENT,
            dateStr = "03月02日",
            note = "两张电影票"
        )
    )

    val monthlySummary = MonthlySummary(
        totalIncome = 18500.00,
        totalExpense = 3702.90
    )
}
