package com.example.myledger.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Work
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.myledger.ui.theme.CategoryEntertainmentBgDark
import com.example.myledger.ui.theme.CategoryEntertainmentBgLight
import com.example.myledger.ui.theme.CategoryEntertainmentIcon
import com.example.myledger.ui.theme.CategoryFoodBgDark
import com.example.myledger.ui.theme.CategoryFoodBgLight
import com.example.myledger.ui.theme.CategoryFoodIcon
import com.example.myledger.ui.theme.CategoryHousingBgDark
import com.example.myledger.ui.theme.CategoryHousingBgLight
import com.example.myledger.ui.theme.CategoryHousingIcon
import com.example.myledger.ui.theme.CategoryOtherBgDark
import com.example.myledger.ui.theme.CategoryOtherBgLight
import com.example.myledger.ui.theme.CategoryOtherIcon
import com.example.myledger.ui.theme.CategorySalaryBgDark
import com.example.myledger.ui.theme.CategorySalaryBgLight
import com.example.myledger.ui.theme.CategorySalaryIcon
import com.example.myledger.ui.theme.CategoryShoppingBgDark
import com.example.myledger.ui.theme.CategoryShoppingBgLight
import com.example.myledger.ui.theme.CategoryShoppingIcon
import com.example.myledger.ui.theme.CategoryTransportBgDark
import com.example.myledger.ui.theme.CategoryTransportBgLight
import com.example.myledger.ui.theme.CategoryTransportIcon

enum class Category(
    val categoryName: String,
    val icon: ImageVector,
    private val lightBg: Color,
    private val darkBg: Color,
    val iconColor: Color
) {
    FOOD("餐饮", Icons.Default.Fastfood, CategoryFoodBgLight, CategoryFoodBgDark, CategoryFoodIcon),
    SHOPPING("购物", Icons.Default.ShoppingBag, CategoryShoppingBgLight, CategoryShoppingBgDark, CategoryShoppingIcon),
    TRANSPORT("交通", Icons.Default.DirectionsBus, CategoryTransportBgLight, CategoryTransportBgDark, CategoryTransportIcon),
    SALARY("工资", Icons.Default.Work, CategorySalaryBgLight, CategorySalaryBgDark, CategorySalaryIcon),
    HOUSING("居住", Icons.Default.Home, CategoryHousingBgLight, CategoryHousingBgDark, CategoryHousingIcon),
    ENTERTAINMENT("娱乐", Icons.Default.Movie, CategoryEntertainmentBgLight, CategoryEntertainmentBgDark, CategoryEntertainmentIcon),
    OTHER("其他", Icons.Default.MoreHoriz, CategoryOtherBgLight, CategoryOtherBgDark, CategoryOtherIcon);

    @Composable
    fun getBackgroundColor(isDark: Boolean): Color {
        return if (isDark) darkBg else lightBg
    }

    companion object {
        fun fromCategoryName(name: String): Category {
            return entries.firstOrNull { it.name == name || it.categoryName == name } ?: OTHER
        }
    }
}
