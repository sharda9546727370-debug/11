package com.example.ui.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FormulasTab(viewModel: CalculatorViewModel) {
    val selectedFormula by viewModel.selectedFormula.collectAsStateWithLifecycle()
    val formulaVars by viewModel.formulaVars.collectAsStateWithLifecycle()
    val formulaResult by viewModel.formulaResult.collectAsStateWithLifecycle()
    val aiExplanation by viewModel.aiExplanation.collectAsStateWithLifecycle()
    val isExplaining by viewModel.isExplaining.collectAsStateWithLifecycle()

    val formulas = viewModel.formulas
    val constants = viewModel.constants

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "MATHEMATICAL FORMULAS & CONSTANTS",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = GreenPrimary,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Understand, solve, or explain STEM equations & physical constants.",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }

        // Selected Formula solver area
        if (selectedFormula != null) {
            val formula = selectedFormula!!
            item(key = "formula_solver_${formula.name}") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF090F0C)),
                    border = BorderStroke(1.dp, GreenPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formula.name,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = GreenPrimary,
                                fontSize = 16.sp
                            )
                            IconButton(onClick = { viewModel.selectFormula(null) }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Close Solver",
                                    tint = TextWhite
                                )
                            }
                        }

                        Text(
                            text = "Formula: ${formula.text}",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            color = MatrixGreen,
                            fontSize = 18.sp,
                            modifier = Modifier
                                .background(Color(0xFF030504), RoundedCornerShape(4.dp))
                                .padding(8.dp)
                                .fillMaxWidth()
                        )

                        Text(
                            text = formula.description,
                            fontSize = 12.sp,
                            color = TextWhite
                        )

                        HorizontalDivider(color = DarkGreen, thickness = 1.dp)

                        Text(
                            text = "ENTER VARIABLES",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = GreenPrimary
                        )

                        formula.variables.forEach { variableName ->
                            val currentVal = formulaVars[variableName] ?: ""
                            OutlinedTextField(
                                value = currentVal,
                                onValueChange = { viewModel.updateFormulaVariable(variableName, it) },
                                label = { Text(variableName, color = TextMuted) },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GreenPrimary,
                                    unfocusedBorderColor = Color(0xFF333333),
                                    focusedLabelColor = GreenPrimary,
                                    unfocusedLabelColor = TextMuted,
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite,
                                    focusedContainerColor = Color.Black,
                                    unfocusedContainerColor = Color.Black
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.calculateFormula() },
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("COMPUTE HIGH-PRECISION", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }

                            Button(
                                onClick = { viewModel.explainFormulaWithAI() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                border = BorderStroke(1.dp, MatrixGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("explain_formula_button")
                            ) {
                                Text("ASK AI TO EXPLAIN", color = MatrixGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        if (formulaResult.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF030504)),
                                border = BorderStroke(1.dp, Color(0xFF1E1E1E))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "OUTPUT",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                    Text(
                                        text = formulaResult,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MatrixGreen,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        if (isExplaining || aiExplanation.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF000F05)),
                                border = BorderStroke(1.dp, MatrixGreen)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "AI Breakdown",
                                            tint = GreenPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "GEMINI AI REPORT",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GreenPrimary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    if (isExplaining) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            CircularProgressIndicator(color = GreenPrimary, modifier = Modifier.size(24.dp))
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text("Sifting knowledge limits...", fontSize = 12.sp, color = TextWhite)
                                        }
                                    } else {
                                        Text(
                                            text = aiExplanation,
                                            fontSize = 13.sp,
                                            color = TextWhite,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "BASIC FORMULA PRESETS",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = GreenPrimary
            )
        }

        items(
            items = formulas,
            key = { it.name }
        ) { formula ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectFormula(formula) },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
                border = BorderStroke(1.dp, Color(0xFF222222)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "[${formula.category}]",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = MatrixGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = formula.name,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formula.text,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimary,
                            fontSize = 13.sp
                        )
                    }
                    Text(
                        text = "SOLVE",
                        fontFamily = FontFamily.Monospace,
                        color = GreenPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            Text(
                text = "HIGH-PRECISION MATH CONSTANTS",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = GreenPrimary,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                constants.forEach { constant ->
                    Card(
                        modifier = Modifier
                            .widthIn(min = 140.dp)
                            .clickable {
                                viewModel.onCalculatorButtonPressed(constant.first.substringAfter("(").substringBefore(")"))
                                viewModel.selectTab("Calculator")
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0C0C)),
                        border = BorderStroke(1.dp, Color(0xFF1F1F1F))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = constant.first,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = GreenPrimary,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = constant.second,
                                fontFamily = FontFamily.Monospace,
                                color = TextWhite,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
