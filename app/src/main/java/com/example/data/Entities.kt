package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "calculations",
    indices = [Index(value = ["timestamp"])]
)
data class Calculation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val expression: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "conversions",
    indices = [Index(value = ["timestamp"])]
)
data class Conversion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String, // e.g., "Currency", "Length", "Weight", "Temperature"
    val fromValue: String,
    val fromUnit: String,
    val toValue: String,
    val toUnit: String,
    val timestamp: Long = System.currentTimeMillis()
)
