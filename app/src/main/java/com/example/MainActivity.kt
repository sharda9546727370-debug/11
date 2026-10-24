package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.HistoryRepository
import com.example.ui.CalculatorAppScreen
import com.example.ui.CalculatorViewModel
import com.example.ui.CalculatorViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Room Database & Repository
        val database = AppDatabase.getDatabase(this)
        val repository = HistoryRepository(database.historyDao())

        // Build ViewModel
        val factory = CalculatorViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory)[CalculatorViewModel::class.java]

        setContent {
            MyApplicationTheme {
                CalculatorAppScreen(viewModel)
            }
        }
    }
}
