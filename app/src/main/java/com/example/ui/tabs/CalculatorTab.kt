package com.example.ui.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.CalculatorViewModel
import com.example.ui.components.CalculatorButton
import com.example.ui.components.HistoryRecordCard
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun PreciseCalculatorTab(viewModel: CalculatorViewModel) {
    val input by viewModel.input.collectAsStateWithLifecycle()
    val result by viewModel.result.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val history by viewModel.calculationHistory.collectAsStateWithLifecycle()

    // Pre-allocated keypad layout (allocated only once per composition lifecycle)
    val buttonRows = remember {
        listOf(
            listOf("C", "√", "^", "⌫"),
            listOf("7", "8", "9", "/"),
            listOf("4", "5", "6", "*"),
            listOf("1", "2", "3", "-"),
            listOf("0", ".", "π", "+"),
            listOf("e", "00", "%", "=")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // App Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AI PRECISION CALCULATOR",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = GreenPrimary,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Arbitrary 25-decimal precision",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            if (history.isNotEmpty()) {
                IconButton(onClick = { viewModel.clearHistory() }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear History",
                        tint = Color(0xFFFF5252)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Display Card (cybernetic ambient readout screen)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.3f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF050B07)),
            border = BorderStroke(1.dp, DarkGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                // Input Display
                Text(
                    text = input.ifEmpty { "0" },
                    fontFamily = FontFamily.Monospace,
                    fontSize = 20.sp,
                    color = TextWhite,
                    textAlign = TextAlign.End,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("calc_display_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Answer Display
                if (error != null) {
                    Text(
                        text = error ?: "",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        color = Color(0xFFFF5252),
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (result.isNotEmpty()) {
                    val resultSize = if (result.length > 20) 18.sp else 28.sp
                    Text(
                        text = result,
                        fontFamily = FontFamily.Monospace,
                        fontSize = resultSize,
                        fontWeight = FontWeight.ExtraBold,
                        color = MatrixGreen,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("calc_display_result")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Grid of Keys + Calculation History
        val screenWeight = if (history.isEmpty()) 4.5f else 5.5f
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(screenWeight),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Calculator Keys Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                buttonRows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { btn ->
                            CalculatorButton(
                                label = btn,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                onClick = { viewModel.onCalculatorButtonPressed(btn) }
                            )
                        }
                    }
                }
            }

            // Quick Sidebar History (On tablet or right drawer if history exists)
            if (history.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .widthIn(max = 180.dp)
                        .fillMaxHeight(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF080808)),
                    border = BorderStroke(1.dp, Color(0xFF1E1E1E))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "HISTORY",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = GreenPrimary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = history,
                                key = { it.id }
                            ) { record ->
                                HistoryRecordCard(record) {
                                    viewModel.loadCalculationIntoInput(record)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
