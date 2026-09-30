package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MealType(val displayName: String) {
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner"),
    SNACK("Snack")
}

@Entity(tableName = "food_entries")
data class FoodEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // Format: YYYY-MM-DD
    val mealType: String, // From MealType.name
    val name: String,
    val calories: Int,
    val carbsG: Float = 0f,
    val proteinG: Float = 0f,
    val fatG: Float = 0f,
    val servingSize: String = "1 serving",
    val timestamp: Long = System.currentTimeMillis()
)
