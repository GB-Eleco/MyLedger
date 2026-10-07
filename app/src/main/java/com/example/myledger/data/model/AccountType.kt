package com.example.myledger.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class AccountType(
    val code: String,
    val typeName: String,
    val icon: ImageVector,
    val color: Color
) {
    WECHAT("wechat", "微信", Icons.Default.QrCode, Color(0xFF07C160)),
    ALIPAY("alipay", "支付宝", Icons.Default.Payment, Color(0xFF1677FF)),
    CASH("cash", "现金", Icons.Default.Money, Color(0xFF10B981)),
    BANK("bank", "银行卡", Icons.Default.AccountBalance, Color(0xFF6366F1)),
    OTHER("other", "其他账户", Icons.Default.CreditCard, Color(0xFF8B5CF6));

    companion object {
        fun fromCode(code: String): AccountType {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: OTHER
        }
    }
}
