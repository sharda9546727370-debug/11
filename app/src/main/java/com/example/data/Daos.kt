package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM calculations ORDER BY timestamp DESC LIMIT 100")
    fun getAllCalculations(): Flow<List<Calculation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalculation(calculation: Calculation)

    @Query("DELETE FROM calculations")
    suspend fun clearCalculationHistory()

    @Query("SELECT * FROM conversions ORDER BY timestamp DESC LIMIT 100")
    fun getAllConversions(): Flow<List<Conversion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversion(conversion: Conversion)

    @Query("DELETE FROM conversions")
    suspend fun clearConversionHistory()
}
