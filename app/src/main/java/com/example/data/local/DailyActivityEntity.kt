package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_activities")
data class DailyActivityEntity(
    @PrimaryKey
    val date: String, // Format: YYYY-MM-DD
    val hardwareSteps: Int = 0,
    val manualSteps: Int = 0,
    val waterMl: Int = 0,
    val lastHardwareSensorValue: Float = -1f // Used to compute sensor deltas
) {
    val totalSteps: Int
        get() = hardwareSteps + manualSteps
}
