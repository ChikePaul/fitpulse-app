package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    // Goals
    val dailyStepGoal: Int = 10000,
    val weeklyStepGoal: Int = 70000,
    val dailyCalorieIntakeGoal: Int = 2000,
    val weeklyCalorieIntakeGoal: Int = 14000,
    val dailyCalorieBurnGoal: Int = 500,
    val weeklyCalorieBurnGoal: Int = 3500,
    val waterGoalMl: Int = 2500,
    val notificationsEnabled: Boolean = true,

    // Body Stats
    val weightKg: Float = 70f,
    val heightCm: Float = 175f,
    val strideLengthM: Float = 0.76f,
    val isMetric: Boolean = true,

    // Wearable Integration Settings
    val activeWearableSource: String = WearableSource.FITBIT.name,
    val fitbitAccessToken: String = "",
    val fitbitClientId: String = "",
    val isWearableLinked: Boolean = false
) {
    // Backwards compatibility property
    val stepGoal: Int get() = dailyStepGoal
    val calorieGoal: Int get() = dailyCalorieIntakeGoal

    val caloriesPerStep: Float
        get() = (weightKg * 0.00057f).coerceIn(0.035f, 0.065f)

    // Calculate calories burned from workout using MET standard formula
    fun calculateWorkoutCalories(exerciseType: ExerciseType, durationMinutes: Int, intensity: WorkoutIntensity): Int {
        val met = exerciseType.baseMet * intensity.multiplier
        val durationHours = durationMinutes / 60f
        return (met * weightKg * durationHours).toInt().coerceAtLeast(10)
    }
}
