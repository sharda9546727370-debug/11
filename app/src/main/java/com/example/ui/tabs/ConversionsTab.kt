package com.example.ui.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.CalculatorViewModel
import com.example.ui.components.UnitSelectorDropdown
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun ConversionsTab(viewModel: CalculatorViewModel) {
    val category by viewModel.conversionCategory.collectAsStateWithLifecycle()
    val fromUnit by viewModel.fromUnit.collectAsStateWithLifecycle()
    val toUnit by viewModel.toUnit.collectAsStateWithLifecycle()
    val fromValue by viewModel.fromValue.collectAsStateWithLifecycle()
    val toValue by viewModel.toValue.collectAsStateWithLifecycle()
    val history by viewModel.conversionHistory.collectAsStateWithLifecycle()
    val isSyncingRates by viewModel.isSyncingRates.collectAsStateWithLifecycle()

    val categories = remember { listOf("Currency", "Length", "Weight", "Temperature", "Area") }
    val currentUnits = remember(category) { getUnitsForCategory(category) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "UNIVERSAL HIGH-PRECISION CONVERTER",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = GreenPrimary,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Converts standard units and global currencies up to 25 decimal places.",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }

        // Category tab bar
        item {
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(category).coerceAtLeast(0),
                containerColor = Color(0xFF030303),
                contentColor = GreenPrimary,
                edgePadding = 0.dp,
                divider = { HorizontalDivider(color = Color(0xFF1E1E1E), thickness = 1.dp) }
            ) {
                categories.forEach { cat ->
                    Tab(
                        selected = category == cat,
                        onClick = { viewModel.updateConversionCategory(cat) },
                        text = {
                            Text(
                                text = cat,
                                fontSize = 12.sp,
                                color = if (category == cat) GreenPrimary else TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                }
            }
        }

        // Conversion Fields Input UI
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0C0C)),
                border = BorderStroke(1.dp, Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Currency rates sync button
                    if (category == "Currency") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Exchange Rates synced via AI",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                            Button(
                                onClick = { viewModel.syncCurrencyRatesWithAI() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF112217)),
                                border = BorderStroke(1.dp, GreenPrimary),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier
                                    .height(30.dp)
                                    .testTag("sync_rates_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isSyncingRates) {
                                        CircularProgressIndicator(color = GreenPrimary, modifier = Modifier.size(12.dp))
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Sync",
                                            tint = GreenPrimary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("SYNC RATES", fontSize = 10.sp, color = GreenPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // FROM Box unit selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = fromValue,
                            onValueChange = { viewModel.updateFromValue(it) },
                            label = { Text("From Value", color = GreenPrimary) },
                            modifier = Modifier.weight(1.5f),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenPrimary,
                                unfocusedBorderColor = Color(0xFF333333),
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedContainerColor = Color.Black,
                                unfocusedContainerColor = Color.Black
                            )
                        )

                        UnitSelectorDropdown(
                            selectedUnit = fromUnit,
                            units = currentUnits,
                            modifier = Modifier.weight(1f),
                            onSelected = { viewModel.updateFromUnit(it) }
                        )
                    }

                    // Arrow indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "⇩",
                            fontSize = 24.sp,
                            color = GreenPrimary,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // TO Box unit selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .weight(1.5f)
                                .height(56.dp)
                                .background(Color.Black, RoundedCornerShape(4.dp))
                                .border(1.dp, Color(0xFF222222), RoundedCornerShape(4.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Black)
                        ) {
                            Column {
                                Text("Converted Value", fontSize = 10.sp, color = TextMuted)
                                Text(
                                    text = toValue.ifEmpty { "0.0" },
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MatrixGreen,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        UnitSelectorDropdown(
                            selectedUnit = toUnit,
                            units = currentUnits,
                            modifier = Modifier.weight(1f),
                            onSelected = { viewModel.updateToUnit(it) }
                        )
                    }
                }
            }
        }

        // Saved conversions histories
        if (history.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CONVERSION HISTORY",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary
                    )
                    IconButton(onClick = { viewModel.clearConversionHistory() }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear", tint = Color(0xFFFF5252))
                    }
                }
            }

            items(
                items = history,
                key = { it.id }
            ) { conversion ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A0A)),
                    border = BorderStroke(1.dp, Color(0xFF1C1C1C))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Category: ${conversion.category}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${conversion.fromValue} ${conversion.fromUnit} ➔ ${conversion.toValue} ${conversion.toUnit}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MatrixGreen
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getUnitsForCategory(category: String): List<String> {
    return when (category) {
        "Currency" -> listOf("USD", "EUR", "GBP", "INR", "JPY", "CAD", "AUD", "CNY", "BRL")
        "Length" -> listOf("m", "km", "cm", "mm", "in", "ft", "yd", "mi")
        "Weight" -> listOf("kg", "g", "mg", "lb", "oz", "st")
        "Temperature" -> listOf("°C", "°F", "K")
        "Area" -> listOf("m²", "km²", "ft²", "ac", "ha")
        else -> emptyList()
    }
}
