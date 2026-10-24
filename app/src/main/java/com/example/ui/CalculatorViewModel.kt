package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Calculation
import com.example.data.Conversion
import com.example.data.GeminiClient
import com.example.data.HistoryRepository
import com.example.domain.PrecisionCalculator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.Locale

private val MC = MathContext(45, RoundingMode.HALF_UP)
private val CONVERSION_MC = MathContext(35, RoundingMode.HALF_UP)

// Precomputed static conversion rate tables (relative to standard SI unit: meters, kilograms, square meters)
private val LENGTH_RATES = mapOf(
    "m" to BigDecimal("1.0"),
    "km" to BigDecimal("0.001"),
    "cm" to BigDecimal("100.0"),
    "mm" to BigDecimal("1000.0"),
    "in" to BigDecimal("39.37007874015748031496063"),
    "ft" to BigDecimal("3.280839895013123359580052"),
    "yd" to BigDecimal("1.093613298337707786526247"),
    "mi" to BigDecimal("0.000621371192237333969617")
)

private val WEIGHT_RATES = mapOf(
    "kg" to BigDecimal("1.0"),
    "g" to BigDecimal("1000.0"),
    "mg" to BigDecimal("1000000.0"),
    "lb" to BigDecimal("2.204622621848775807229739"),
    "oz" to BigDecimal("35.2739619495804129156759"),
    "st" to BigDecimal("0.157473044417769700516410")
)

private val AREA_RATES = mapOf(
    "m²" to BigDecimal("1.0"),
    "km²" to BigDecimal("0.000001"),
    "ft²" to BigDecimal("10.76391041670972230833292"),
    "ac" to BigDecimal("0.000247105381467165341258"),
    "ha" to BigDecimal("0.0001")
)

data class MathFormula(
    val name: String,
    val text: String,
    val description: String,
    val category: String,
    val variables: List<String>,
    val calculate: (vars: Map<String, BigDecimal>) -> BigDecimal
)

class CalculatorViewModel(private val repository: HistoryRepository) : ViewModel() {

    // --- Calculator State ---
    private val _input = MutableStateFlow("")
    val input: StateFlow<String> = _input.asStateFlow()

    private val _result = MutableStateFlow("")
    val result: StateFlow<String> = _result.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val calculationHistory: StateFlow<List<Calculation>> = repository.allCalculations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Tab Navigation ---
    private val _currentTab = MutableStateFlow("Calculator")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    fun selectTab(tab: String) {
        _currentTab.value = tab
    }

    // --- Formula Library State ---
    private val _selectedFormula = MutableStateFlow<MathFormula?>(null)
    val selectedFormula: StateFlow<MathFormula?> = _selectedFormula.asStateFlow()

    private val _formulaVars = MutableStateFlow<Map<String, String>>(emptyMap())
    val formulaVars: StateFlow<Map<String, String>> = _formulaVars.asStateFlow()

    private val _formulaResult = MutableStateFlow("")
    val formulaResult: StateFlow<String> = _formulaResult.asStateFlow()

    private val _aiExplanation = MutableStateFlow("")
    val aiExplanation: StateFlow<String> = _aiExplanation.asStateFlow()

    private val _isExplaining = MutableStateFlow(false)
    val isExplaining: StateFlow<Boolean> = _isExplaining.asStateFlow()

    // --- Conversion State ---
    private val _conversionCategory = MutableStateFlow("Currency")
    val conversionCategory: StateFlow<String> = _conversionCategory.asStateFlow()

    private val _fromUnit = MutableStateFlow("USD")
    val fromUnit: StateFlow<String> = _fromUnit.asStateFlow()

    private val _toUnit = MutableStateFlow("EUR")
    val toUnit: StateFlow<String> = _toUnit.asStateFlow()

    private val _fromValue = MutableStateFlow("")
    val fromValue: StateFlow<String> = _fromValue.asStateFlow()

    private val _toValue = MutableStateFlow("")
    val toValue: StateFlow<String> = _toValue.asStateFlow()

    private var conversionDebounceJob: Job? = null

    val conversionHistory: StateFlow<List<Conversion>> = repository.allConversions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Live AI currency exchange rates relative to USD in BigDecimal format
    private val _currencyRates = MutableStateFlow(
        mapOf(
            "USD" to BigDecimal("1.0"),
            "EUR" to BigDecimal("0.9215000000000000000000000"),
            "GBP" to BigDecimal("0.7845000000000000000000000"),
            "INR" to BigDecimal("83.4210000000000000000000000"),
            "JPY" to BigDecimal("156.8820000000000000000000000"),
            "CAD" to BigDecimal("1.3655000000000000000000000"),
            "AUD" to BigDecimal("1.5126000000000000000000000"),
            "CNY" to BigDecimal("7.2450000000000000000000000"),
            "BRL" to BigDecimal("5.1642000000000000000000000")
        )
    )
    val currencyRates: StateFlow<Map<String, BigDecimal>> = _currencyRates.asStateFlow()

    private val _isSyncingRates = MutableStateFlow(false)
    val isSyncingRates: StateFlow<Boolean> = _isSyncingRates.asStateFlow()

    // --- AI Assistant Chat State ---
    private val _chatHistory = MutableStateFlow<List<Pair<String, Boolean>>>(
        listOf(
            Pair("Welcome! I am your AI Math Assistant configured with ultra-precise decimal calculations. Ask me math equations, currency advice, or step-by-step formula analysis!", false)
        )
    )
    val chatHistory: StateFlow<List<Pair<String, Boolean>>> = _chatHistory.asStateFlow()

    private val _chatInput = MutableStateFlow("")
    val chatInput: StateFlow<String> = _chatInput.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    // --- Formulas List (Pre-initialized) ---
    val formulas = listOf(
        MathFormula(
            "Area of a Circle",
            "A = π * r²",
            "Calculates the total region enclosed by a circle of radius r with 45-decimal precision π.",
            "Geometry",
            listOf("r")
        ) { vars ->
            val r = vars["r"] ?: BigDecimal.ZERO
            PrecisionCalculator.PI_PRIME.multiply(r.multiply(r, MC), MC)
        },
        MathFormula(
            "Pythagorean Theorem",
            "c = √(a² + b²)",
            "Calculates the hypotenuse length of a right-angled triangle given legs a and b.",
            "Geometry",
            listOf("a", "b")
        ) { vars ->
            val a = vars["a"] ?: BigDecimal.ZERO
            val b = vars["b"] ?: BigDecimal.ZERO
            val sumOfSquares = a.multiply(a, MC).add(b.multiply(b, MC), MC)
            PrecisionCalculator.sqrt(sumOfSquares)
        },
        MathFormula(
            "Einstein Mass-Energy",
            "E = m * c²",
            "Calculates mass-energy equivalence where c is the constant speed of light (299,792,458 m/s).",
            "Physics",
            listOf("m (mass in kg)")
        ) { vars ->
            val m = vars["m (mass in kg)"] ?: BigDecimal.ZERO
            val c = BigDecimal("299792458")
            m.multiply(c.multiply(c, MC), MC)
        },
        MathFormula(
            "Newtonian Force",
            "F = m * a",
            "Calculates Force in Newtons given mass m in kg and acceleration a in m/s².",
            "Physics",
            listOf("m (mass)", "a (accel)")
        ) { vars ->
            val m = vars["m (mass)"] ?: BigDecimal.ZERO
            val a = vars["a (accel)"] ?: BigDecimal.ZERO
            m.multiply(a, MC)
        },
        MathFormula(
            "Quadratic Discriminant",
            "Δ = b² - 4ac",
            "Determines the nature and count of roots for quadratic equation ax² + bx + c = 0.",
            "Algebra",
            listOf("a", "b", "c")
        ) { vars ->
            val a = vars["a"] ?: BigDecimal.ZERO
            val b = vars["b"] ?: BigDecimal.ZERO
            val c = vars["c"] ?: BigDecimal.ZERO
            b.multiply(b, MC).subtract(BigDecimal("4").multiply(a, MC).multiply(c, MC), MC)
        },
        MathFormula(
            "Compound Interest",
            "A = P * (1 + r/n)^(nt)",
            "Calculates compound interest accumulated over time t with principal P, rate r, compounded n times.",
            "Finance",
            listOf("P (principal)", "r (rate, e.g. 0.05)", "n (compounds/yr)", "t (years)")
        ) { vars ->
            val p = vars["P (principal)"] ?: BigDecimal.ZERO
            val r = vars["r (rate, e.g. 0.05)"] ?: BigDecimal.ZERO
            val n = vars["n (compounds/yr)"] ?: BigDecimal.ONE
            val t = vars["t (years)"] ?: BigDecimal.ONE

            val base = BigDecimal.ONE.add(r.divide(n, MC), MC)
            val nt = n.multiply(t, MC).toInt()
            val factor = try {
                base.pow(nt, MC)
            } catch (e: Exception) {
                BigDecimal(Math.pow(base.toDouble(), nt.toDouble()), MC)
            }
            p.multiply(factor, MC)
        }
    )

    val constants = listOf(
        Pair("Pi (π)", "3.1415926535897932384626433"),
        Pair("Euler's Number (e)", "2.7182818284590452353602874"),
        Pair("Golden Ratio (φ)", "1.6180339887498948482045868"),
        Pair("Square Root of 2 (√2)", "1.4142135623730950488016887"),
        Pair("Planck Constant (h)", "6.62607015e-34"),
        Pair("Speed of Light (c)", "299792458")
    )

    // --- Calculator Actions ---

    fun onCalculatorButtonPressed(action: String) {
        _error.value = null
        when (action) {
            "C" -> {
                _input.value = ""
                _result.value = ""
            }
            "⌫" -> {
                val current = _input.value
                if (current.isNotEmpty()) {
                    _input.value = current.substring(0, current.length - 1)
                }
            }
            "=" -> {
                val statement = _input.value
                if (statement.isNotEmpty()) {
                    viewModelScope.launch {
                        try {
                            val value = PrecisionCalculator.evaluate(statement)
                            val displayValue = PrecisionCalculator.formatResult(value)
                            _result.value = displayValue

                            repository.insertCalculation(
                                Calculation(
                                    expression = statement,
                                    result = displayValue
                                )
                            )
                        } catch (e: Exception) {
                            _error.value = e.localizedMessage ?: "Math error"
                        }
                    }
                }
            }
            "x²" -> {
                _input.value = _input.value + "^2"
            }
            "√" -> {
                _input.value = _input.value + "√"
            }
            else -> {
                _input.value = _input.value + action
            }
        }
    }

    fun loadCalculationIntoInput(calculation: Calculation) {
        _input.value = calculation.expression
        _result.value = calculation.result
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearCalculationHistory()
        }
    }

    // --- Formula Library Actions ---

    fun selectFormula(formula: MathFormula?) {
        _selectedFormula.value = formula
        _formulaResult.value = ""
        _aiExplanation.value = ""
        if (formula != null) {
            _formulaVars.value = formula.variables.associateWith { "" }
        } else {
            _formulaVars.value = emptyMap()
        }
    }

    fun updateFormulaVariable(name: String, value: String) {
        val updated = _formulaVars.value.toMutableMap()
        updated[name] = value
        _formulaVars.value = updated
    }

    fun calculateFormula() {
        val formula = _selectedFormula.value ?: return
        try {
            val parsedVars = _formulaVars.value.mapValues { entry ->
                if (entry.value.isBlank()) BigDecimal.ZERO else BigDecimal(entry.value)
            }
            val bigDecimalResult = formula.calculate(parsedVars)
            _formulaResult.value = PrecisionCalculator.formatResult(bigDecimalResult)
        } catch (e: Exception) {
            _formulaResult.value = "Error: Input format invalid"
        }
    }

    fun explainFormulaWithAI() {
        val formula = _selectedFormula.value ?: return
        _isExplaining.value = true
        _aiExplanation.value = ""

        viewModelScope.launch {
            val systemPrompt = "You are a professional mathematician. Explain math formulas clearly, beautifully, and simply. Show real-world examples."
            val prompt = """
                Explain the mathematical formula: '${formula.name}' with structure: '${formula.text}'.
                ${formula.description}
                
                Please provide:
                1. Concept in simple layman terms.
                2. Explicit practical example with calculations.
                3. Historical context or use cases.
            """.trimIndent()

            val response = GeminiClient.getModelResponse(prompt, systemPrompt)
            _aiExplanation.value = response
            _isExplaining.value = false
        }
    }

    // --- Conversion Actions ---

    fun updateConversionCategory(category: String) {
        _conversionCategory.value = category
        _fromValue.value = ""
        _toValue.value = ""

        when (category) {
            "Currency" -> {
                _fromUnit.value = "USD"
                _toUnit.value = "EUR"
            }
            "Length" -> {
                _fromUnit.value = "m"
                _toUnit.value = "km"
            }
            "Weight" -> {
                _fromUnit.value = "kg"
                _toUnit.value = "g"
            }
            "Temperature" -> {
                _fromUnit.value = "°C"
                _toUnit.value = "°F"
            }
            "Area" -> {
                _fromUnit.value = "m²"
                _toUnit.value = "km²"
            }
        }
    }

    fun updateFromUnit(unit: String) {
        _fromUnit.value = unit
        performConversion(shouldPersist = false)
    }

    fun updateToUnit(unit: String) {
        _toUnit.value = unit
        performConversion(shouldPersist = false)
    }

    fun updateFromValue(value: String) {
        _fromValue.value = value
        // Fast instant preview calculation, debounced database persistence
        performConversion(shouldPersist = true)
    }

    private fun performConversion(shouldPersist: Boolean) {
        val valueStr = _fromValue.value
        if (valueStr.isBlank()) {
            _toValue.value = ""
            return
        }

        val parsedVal = try {
            BigDecimal(valueStr)
        } catch (e: Exception) {
            _toValue.value = "Invalid input"
            return
        }

        val category = _conversionCategory.value
        val from = _fromUnit.value
        val to = _toUnit.value

        try {
            val outputBigDecimal = when (category) {
                "Currency" -> convertCurrencyPrecision(parsedVal, from, to)
                "Length" -> convertLengthPrecision(parsedVal, from, to)
                "Weight" -> convertWeightPrecision(parsedVal, from, to)
                "Temperature" -> convertTemperaturePrecision(parsedVal, from, to)
                "Area" -> convertAreaPrecision(parsedVal, from, to)
                else -> BigDecimal.ZERO
            }

            val resultStr = PrecisionCalculator.formatResult(outputBigDecimal)
            _toValue.value = resultStr

            // Debounced database write avoids disk thrashing on every keystroke
            if (shouldPersist) {
                conversionDebounceJob?.cancel()
                conversionDebounceJob = viewModelScope.launch {
                    delay(800)
                    repository.insertConversion(
                        Conversion(
                            category = category,
                            fromValue = valueStr,
                            fromUnit = from,
                            toValue = resultStr,
                            toUnit = to
                        )
                    )
                }
            }
        } catch (e: Exception) {
            _toValue.value = "Error during conversion"
        }
    }

    private fun convertLengthPrecision(value: BigDecimal, from: String, to: String): BigDecimal {
        val rateFrom = LENGTH_RATES[from] ?: BigDecimal.ONE
        val rateTo = LENGTH_RATES[to] ?: BigDecimal.ONE
        val metersVal = value.divide(rateFrom, CONVERSION_MC)
        return metersVal.multiply(rateTo, CONVERSION_MC)
    }

    private fun convertWeightPrecision(value: BigDecimal, from: String, to: String): BigDecimal {
        val rateFrom = WEIGHT_RATES[from] ?: BigDecimal.ONE
        val rateTo = WEIGHT_RATES[to] ?: BigDecimal.ONE
        val kgVal = value.divide(rateFrom, CONVERSION_MC)
        return kgVal.multiply(rateTo, CONVERSION_MC)
    }

    private fun convertAreaPrecision(value: BigDecimal, from: String, to: String): BigDecimal {
        val rateFrom = AREA_RATES[from] ?: BigDecimal.ONE
        val rateTo = AREA_RATES[to] ?: BigDecimal.ONE
        val baseVal = value.divide(rateFrom, CONVERSION_MC)
        return baseVal.multiply(rateTo, CONVERSION_MC)
    }

    private fun convertTemperaturePrecision(value: BigDecimal, from: String, to: String): BigDecimal {
        if (from == to) return value

        val celsius = when (from) {
            "°C" -> value
            "°F" -> value.subtract(BigDecimal("32"), CONVERSION_MC)
                .multiply(BigDecimal("5"), CONVERSION_MC)
                .divide(BigDecimal("9"), CONVERSION_MC)
            "K" -> value.subtract(BigDecimal("273.15"), CONVERSION_MC)
            else -> value
        }

        return when (to) {
            "°C" -> celsius
            "°F" -> celsius.multiply(BigDecimal("9"), CONVERSION_MC)
                .divide(BigDecimal("5"), CONVERSION_MC)
                .add(BigDecimal("32"), CONVERSION_MC)
            "K" -> celsius.add(BigDecimal("273.15"), CONVERSION_MC)
            else -> celsius
        }
    }

    private fun convertCurrencyPrecision(value: BigDecimal, from: String, to: String): BigDecimal {
        val rates = _currencyRates.value
        val rateFrom = rates[from] ?: BigDecimal.ONE
        val rateTo = rates[to] ?: BigDecimal.ONE
        val usdVal = value.divide(rateFrom, CONVERSION_MC)
        return usdVal.multiply(rateTo, CONVERSION_MC)
    }

    fun syncCurrencyRatesWithAI() {
        _isSyncingRates.value = true
        viewModelScope.launch {
            val systemPrompt = "You are a professional currency analyst. Provide high-precision exchange rate estimates relative to USD."
            val prompt = """
                Provide current high precision estimates of global currency exchange rates relative to USD (1 USD = target rate) for these currencies:
                EUR, GBP, INR, JPY, CAD, AUD, CNY, BRL.
                
                Please return strictly a plain text breakdown in the following exact format, with one currency on each line:
                EUR: <rate>
                GBP: <rate>
                INR: <rate>
                JPY: <rate>
                CAD: <rate>
                AUD: <rate>
                CNY: <rate>
                BRL: <rate>
                
                Do not include any headers, footers, JSON wrappers, or explanations. Just return the lines for those currencies.
            """.trimIndent()

            val response = GeminiClient.getModelResponse(prompt, systemPrompt)
            val updatedRates = _currencyRates.value.toMutableMap()
            var successes = 0

            response.lines().forEach { line ->
                val parts = line.split(":")
                if (parts.size == 2) {
                    val code = parts[0].trim().uppercase(Locale.getDefault())
                    val rateStr = parts[1].trim().replace(",", "")
                    if (updatedRates.containsKey(code)) {
                        try {
                            updatedRates[code] = BigDecimal(rateStr)
                            successes++
                        } catch (e: Exception) {
                            // Ignored formatting failures
                        }
                    }
                }
            }

            if (successes > 0) {
                _currencyRates.value = updatedRates
                performConversion(shouldPersist = false)
            }
            _isSyncingRates.value = false
        }
    }

    fun clearConversionHistory() {
        viewModelScope.launch {
            repository.clearConversionHistory()
        }
    }

    // --- AI Chat Actions ---

    fun updateChatInput(value: String) {
        _chatInput.value = value
    }

    fun sendChatMessage() {
        val msg = _chatInput.value.trim()
        if (msg.isBlank()) return

        val currentList = _chatHistory.value.toMutableList()
        currentList.add(Pair(msg, true))
        _chatHistory.value = currentList
        _chatInput.value = ""
        _isChatLoading.value = true

        viewModelScope.launch {
            val systemPrompt = """
                You are high precision "AI Calculator". You can parse and explain mathematical concepts,
                evaluate expressions, explain conversion relationships, or solve formulas.
                Your calculation precision limit is 25 decimal places. Keep your explanations beautiful, mathematical,
                extremely clear, simple to understand, and highly readable. Show step-by-step working when helpful.
            """.trimIndent()

            val aiResponse = GeminiClient.getModelResponse(msg, systemPrompt)
            val updatedList = _chatHistory.value.toMutableList()
            updatedList.add(Pair(aiResponse, false))
            _chatHistory.value = updatedList
            _isChatLoading.value = false
        }
    }
}

class CalculatorViewModelFactory(private val repository: HistoryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalculatorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalculatorViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
