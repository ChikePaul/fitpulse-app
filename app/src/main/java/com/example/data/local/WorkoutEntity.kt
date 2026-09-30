package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ExerciseType(
    val displayName: String,
    val baseMet: Float, // Metabolic Equivalent of Task
    val defaultIconName: String
) {
    RUNNING("Running", 9.8f, "run"),
    CYCLING("Cycling", 7.5f, "bike"),
    WEIGHTLIFTING("Weightlifting", 5.0f, "fitness_center"),
    WALKING("Walking", 3.8f, "walk"),
    HIIT("HIIT & Circuit", 8.5f, "timer"),
    SWIMMING("Swimming", 7.0f, "pool"),
    YOGA("Yoga & Mobility", 3.0f, "self_improvement"),
    ROWING("Rowing", 7.0f, "rowing")
}

enum class WorkoutIntensity(val multiplier: Float, val label: String) {
    LIGHT(0.85f, "Light"),
    MODERATE(1.0f, "Moderate"),
    VIGOROUS(1.25f, "Vigorous"),
    EXTREME(1.5f, "Maximum Effort")
}

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // Format: YYYY-MM-DD
    val exerciseType: String, // From ExerciseType.name
    val durationMinutes: Int,
    val intensity: String, // From WorkoutIntensity.name
    val caloriesBurned: Int,
    val distanceKm: Float = 0f,
    val avgHeartRate: Int = 0,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
