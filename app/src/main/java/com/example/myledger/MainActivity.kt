package com.example.myledger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.example.myledger.ui.MainScreen
import com.example.myledger.ui.theme.MyLedgerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var darkTheme by remember { mutableStateOf(false) }
            var reverseColors by remember { mutableStateOf(false) }
            MyLedgerTheme(darkTheme = darkTheme, reverseIncomeExpenseColors = reverseColors) {
                MainScreen(darkTheme = darkTheme, onDarkThemeChange = { darkTheme = it }, reverseColors = reverseColors, onReverseColorsChange = { reverseColors = it })
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MyLedgerTheme {
        MainScreen(darkTheme = false, onDarkThemeChange = {}, reverseColors = false, onReverseColorsChange = {})
    }
}
