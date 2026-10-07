package com.example.myledger.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Immutable
data class FinancialColors(
    val income: Color,
    val expense: Color,
    val incomeContainer: Color,
    val expenseContainer: Color
)

val LocalFinancialColors = staticCompositionLocalOf {
    FinancialColors(
        income = IncomeGreenLight,
        expense = ExpenseRedLight,
        incomeContainer = CategorySalaryBgLight,
        expenseContainer = CategoryEntertainmentBgLight
    )
}

private val LightColorScheme = lightColorScheme(
    primary = PrimarySlateLight,
    secondary = SecondarySlateLight,
    background = LightBackground,
    surface = LightSurface,
    onBackground = LightOnSurface,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceVariant = Color(0xFFF1F5F9),
    outline = Color(0xFFE2E8F0)
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimarySlateDark,
    secondary = SecondarySlateDark,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceVariant = Color(0xFF272A30),
    outline = Color(0xFF333742)
)

@Composable
fun MyLedgerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    reverseIncomeExpenseColors: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val baseFinancialColors = if (darkTheme) {
        FinancialColors(
            income = IncomeGreenDark,
            expense = ExpenseRedDark,
            incomeContainer = CategorySalaryBgDark,
            expenseContainer = CategoryEntertainmentBgDark
        )
    } else {
        FinancialColors(
            income = IncomeGreenLight,
            expense = ExpenseRedLight,
            incomeContainer = CategorySalaryBgLight,
            expenseContainer = CategoryEntertainmentBgLight
        )
    }

    val financialColors = if (reverseIncomeExpenseColors) baseFinancialColors.copy(
        income = baseFinancialColors.expense,
        expense = baseFinancialColors.income,
        incomeContainer = baseFinancialColors.expenseContainer,
        expenseContainer = baseFinancialColors.incomeContainer
    ) else baseFinancialColors

    CompositionLocalProvider(LocalFinancialColors provides financialColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

object FinancialTheme {
    val colors: FinancialColors
        @Composable
        get() = LocalFinancialColors.current
}
