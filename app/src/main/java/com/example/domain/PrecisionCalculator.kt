package com.example.domain

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

object PrecisionCalculator {
    val MC = MathContext(45, RoundingMode.HALF_UP)
    val PI_PRIME = BigDecimal("3.141592653589793238462643383279502884197169399") // 45 decimal places
    val E_PRIME = BigDecimal("2.718281828459045235360287471352662497757247093")  // 45 decimal places
    private val TWO = BigDecimal("2")

    /**
     * High-precision square root using Newton-Raphson approximation.
     * Computes square root with up to 45 decimal digits accuracy.
     */
    fun sqrt(value: BigDecimal): BigDecimal {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw ArithmeticException("Square root of a negative value")
        }
        if (value.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO
        }
        // Try built-in BigDecimal.sqrt on API 33+ or fallback to fast Newton-Raphson
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                value.sqrt(MC)
            } else {
                newtonRaphsonSqrt(value)
            }
        } catch (e: Throwable) {
            newtonRaphsonSqrt(value)
        }
    }

    private fun newtonRaphsonSqrt(value: BigDecimal): BigDecimal {
        val initialDouble = Math.sqrt(value.toDouble())
        var x = if (initialDouble.isFinite() && initialDouble > 0) {
            BigDecimal(initialDouble, MC)
        } else {
            BigDecimal.ONE
        }
        // 12 iterations of Newton-Raphson doubles precision on each step (quadratic convergence)
        for (i in 0 until 12) {
            x = x.add(value.divide(x, MC), MC).divide(TWO, MC)
        }
        return x
    }

    fun evaluate(expression: String): BigDecimal {
        val cleanExpr = normalizeExpression(expression)
        if (cleanExpr.isEmpty()) {
            return BigDecimal.ZERO
        }
        return Parser(cleanExpr).parse()
    }

    private fun normalizeExpression(expr: String): String {
        val sb = StringBuilder(expr.length + 16)
        var i = 0
        while (i < expr.length) {
            when (val c = expr[i]) {
                ' ' -> {}
                'π' -> sb.append(PI_PRIME.toString())
                'e' -> {
                    // Check if part of scientific notation like 1e-5 or constant e
                    val prev = if (sb.isNotEmpty()) sb.last() else ' '
                    if (prev.isDigit()) {
                        sb.append('e')
                    } else {
                        sb.append(E_PRIME.toString())
                    }
                }
                '×' -> sb.append('*')
                '÷' -> sb.append('/')
                '−' -> sb.append('-')
                else -> sb.append(c)
            }
            i++
        }
        return sb.toString()
    }

    private class Parser(val input: String) {
        var pos = -1
        var ch = ' '

        fun nextChar() {
            ch = if (++pos < input.length) input[pos] else '\u0000'
        }

        fun eat(charToEat: Char): Boolean {
            while (ch == ' ') nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): BigDecimal {
            nextChar()
            val x = parseExpression()
            if (pos < input.length) {
                throw IllegalArgumentException("Unexpected character '${input[pos]}' at position $pos")
            }
            return x
        }

        fun parseExpression(): BigDecimal {
            var x = parseTerm()
            while (true) {
                if (eat('+')) x = x.add(parseTerm(), MC)
                else if (eat('-')) x = x.subtract(parseTerm(), MC)
                else return x
            }
        }

        fun parseTerm(): BigDecimal {
            var x = parseFactor()
            while (true) {
                if (eat('*')) {
                    x = x.multiply(parseFactor(), MC)
                } else if (eat('/')) {
                    val divisor = parseFactor()
                    if (divisor.compareTo(BigDecimal.ZERO) == 0) {
                        throw ArithmeticException("Division by zero")
                    }
                    x = x.divide(divisor, MC)
                } else if (eat('%')) {
                    val divisor = parseFactor()
                    if (divisor.compareTo(BigDecimal.ZERO) == 0) {
                        throw ArithmeticException("Modulo by zero")
                    }
                    x = x.remainder(divisor, MC)
                } else {
                    return x
                }
            }
        }

        fun parseFactor(): BigDecimal {
            if (eat('+')) return parseFactor()
            if (eat('-')) return parseFactor().negate()

            var x: BigDecimal
            val startPos = this.pos
            if (eat('(')) {
                x = parseExpression()
                if (!eat(')')) {
                    throw IllegalArgumentException("Missing closing parenthesis")
                }
            } else if (eat('√')) {
                val base = parseFactor()
                x = sqrt(base)
            } else if ((ch in '0'..'9') || ch == '.') {
                while ((ch in '0'..'9') || ch == '.' || ch == 'E' || ch == 'e') {
                    if (ch == 'E' || ch == 'e') {
                        nextChar()
                        if (ch == '+' || ch == '-') nextChar()
                    } else {
                        nextChar()
                    }
                }
                try {
                    x = BigDecimal(input.substring(startPos, this.pos))
                } catch (e: Exception) {
                    throw IllegalArgumentException("Invalid number syntax starting at position $startPos")
                }
            } else {
                throw IllegalArgumentException("Unexpected symbol '$ch' at position $pos")
            }

            if (eat('^')) {
                val exponent = parseFactor()
                x = try {
                    val strippedExp = exponent.stripTrailingZeros()
                    if (strippedExp.scale() <= 0 && strippedExp.abs() <= BigDecimal("9999")) {
                        // Integer exponent keeps 100% BigDecimal precision
                        val intVal = strippedExp.toInt()
                        if (intVal >= 0) {
                            x.pow(intVal, MC)
                        } else {
                            BigDecimal.ONE.divide(x.pow(-intVal, MC), MC)
                        }
                    } else {
                        val doubleBase = x.toDouble()
                        val doubleExp = exponent.toDouble()
                        val resultVal = Math.pow(doubleBase, doubleExp)
                        if (resultVal.isNaN() || resultVal.isInfinite()) {
                            throw ArithmeticException("Exponent overflow error")
                        }
                        BigDecimal(resultVal, MC)
                    }
                } catch (e: Exception) {
                    throw ArithmeticException("Power calculation failed: ${e.message}")
                }
            }

            return x
        }
    }

    /**
     * Formats result up to 25 decimal places, removing unnecessary trailing zeros.
     */
    fun formatResult(value: BigDecimal): String {
        return try {
            val scaled = if (value.scale() > 25) {
                value.setScale(25, RoundingMode.HALF_UP)
            } else {
                value
            }
            val stripped = scaled.stripTrailingZeros()
            val absVal = stripped.abs()
            if (absVal > BigDecimal("1E25") || (absVal < BigDecimal("1E-6") && stripped.compareTo(BigDecimal.ZERO) != 0)) {
                stripped.toEngineeringString()
            } else {
                stripped.toPlainString()
            }
        } catch (e: Exception) {
            value.toPlainString()
        }
    }
}
