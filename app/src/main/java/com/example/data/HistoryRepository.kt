package com.example.data

import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val historyDao: HistoryDao) {
    val allCalculations: Flow<List<Calculation>> = historyDao.getAllCalculations()
    val allConversions: Flow<List<Conversion>> = historyDao.getAllConversions()

    suspend fun insertCalculation(calculation: Calculation) {
        historyDao.insertCalculation(calculation)
    }

    suspend fun clearCalculationHistory() {
        historyDao.clearCalculationHistory()
    }

    suspend fun insertConversion(conversion: Conversion) {
        historyDao.insertConversion(conversion)
    }

    suspend fun clearConversionHistory() {
        historyDao.clearConversionHistory()
    }
}
