package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.PrecisionCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.math.BigDecimal

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AI Calculator", appName)
    }

    @Test
    fun `precision calculator handles 25 decimal calculations accurately`() {
        val eval = PrecisionCalculator.evaluate("10 / 3")
        val formatted = PrecisionCalculator.formatResult(eval)
        assertEquals("3.3333333333333333333333333", formatted)
    }

    @Test
    fun `precision calculator square root precision`() {
        val eval = PrecisionCalculator.evaluate("√2")
        val formatted = PrecisionCalculator.formatResult(eval)
        assertTrue(formatted.startsWith("1.414213562373095048801688"))
    }

    @Test
    fun `precision calculator handles powers and order of operations`() {
        val eval = PrecisionCalculator.evaluate("2 + 3 * 4 ^ 2")
        val formatted = PrecisionCalculator.formatResult(eval)
        assertEquals("50", formatted)
    }
}
