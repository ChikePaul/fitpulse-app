package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.local.MealType
import com.example.ui.theme.CoralCalories
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MacroCarbs
import com.example.ui.theme.MacroFat
import com.example.ui.theme.MacroProtein

data class PresetFood(
    val name: String,
    val mealType: MealType,
    val calories: Int,
    val carbsG: Float,
    val proteinG: Float,
    val fatG: Float,
    val servingSize: String
)

val PRESET_FOODS = listOf(
    PresetFood("Avocado Toast", MealType.BREAKFAST, 260, 22f, 7f, 16f, "1 slice"),
    PresetFood("Oatmeal & Honey", MealType.BREAKFAST, 220, 38f, 6f, 4f, "1 bowl"),
    PresetFood("2 Hard Boiled Eggs", MealType.BREAKFAST, 140, 1f, 12f, 10f, "2 eggs"),
    PresetFood("Grilled Chicken Salad", MealType.LUNCH, 380, 12f, 42f, 14f, "1 bowl"),
    PresetFood("Turkey & Avocado Wrap", MealType.LUNCH, 420, 38f, 28f, 16f, "1 wrap"),
    PresetFood("Salmon with Brown Rice", MealType.DINNER, 520, 44f, 38f, 18f, "1 fillet & rice"),
    PresetFood("Steak & Steamed Veggies", MealType.DINNER, 560, 14f, 48f, 26f, "200g portion"),
    PresetFood("Greek Yogurt with Berries", MealType.SNACK, 160, 18f, 15f, 3f, "1 cup"),
    PresetFood("Protein Shake", MealType.SNACK, 180, 6f, 30f, 3f, "1 scoop in water"),
    PresetFood("Banana", MealType.SNACK, 105, 27f, 1f, 0f, "1 medium"),
    PresetFood("Apple with Peanut Butter", MealType.SNACK, 210, 28f, 5f, 11f, "1 apple + 1 tbsp")
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FoodLoggingDialog(
    initialMealType: MealType = MealType.BREAKFAST,
    onDismiss: () -> Unit,
    onConfirm: (name: String, mealType: MealType, calories: Int, carbsG: Float, proteinG: Float, fatG: Float, servingSize: String) -> Unit
) {
    var selectedMeal by remember { mutableStateOf(initialMealType) }
    var foodName by remember { mutableStateOf("") }
    var caloriesText by remember { mutableStateOf("") }
    var servingSize by remember { mutableStateOf("1 serving") }
    var carbsText by remember { mutableStateOf("") }
    var proteinText by remember { mutableStateOf("") }
    var fatText by remember { mutableStateOf("") }
    var showMacros by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("food_logging_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = CoralCalories
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Log Food & Calories",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_food_dialog_button")) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Meal Type selector
                Text(
                    text = "Select Meal",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MealType.entries.forEach { meal ->
                        FilterChip(
                            selected = selectedMeal == meal,
                            onClick = { selectedMeal = meal },
                            label = { Text(meal.displayName, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CoralCalories,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("meal_chip_${meal.name.lowercase()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick presets
                Text(
                    text = "Quick Presets",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PRESET_FOODS.filter { it.mealType == selectedMeal || it.mealType == MealType.SNACK }.take(6).forEach { preset ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable {
                                    foodName = preset.name
                                    caloriesText = preset.calories.toString()
                                    carbsText = preset.carbsG.toInt().toString()
                                    proteinText = preset.proteinG.toInt().toString()
                                    fatText = preset.fatG.toInt().toString()
                                    servingSize = preset.servingSize
                                    showMacros = true
                                }
                                .testTag("preset_${preset.name.lowercase().replace(" ", "_")}")
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                Text(
                                    text = preset.name,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${preset.calories} kcal",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CoralCalories
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Food Name Input
                OutlinedTextField(
                    value = foodName,
                    onValueChange = { foodName = it },
                    label = { Text("Food Name") },
                    placeholder = { Text("e.g. Grilled Chicken Salad") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("food_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CoralCalories,
                        focusedLabelColor = CoralCalories
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Calories and Serving Size row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = caloriesText,
                        onValueChange = { if (it.all { char -> char.isDigit() }) caloriesText = it },
                        label = { Text("Calories (kcal)") },
                        placeholder = { Text("350") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("food_calories_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoralCalories,
                            focusedLabelColor = CoralCalories
                        )
                    )

                    OutlinedTextField(
                        value = servingSize,
                        onValueChange = { servingSize = it },
                        label = { Text("Portion") },
                        placeholder = { Text("1 plate") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("food_portion_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Toggle Macros
                Text(
                    text = if (showMacros) "▲ Hide Macronutrients" else "▼ Add Macros (Carbs, Protein, Fat)",
                    style = MaterialTheme.typography.labelMedium.copy(color = EmeraldPrimary, fontWeight = FontWeight.SemiBold),
                    modifier = Modifier
                        .clickable { showMacros = !showMacros }
                        .padding(vertical = 4.dp)
                )

                if (showMacros) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = carbsText,
                            onValueChange = { carbsText = it },
                            label = { Text("Carbs (g)", color = MacroCarbs) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("food_carbs_input"),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = proteinText,
                            onValueChange = { proteinText = it },
                            label = { Text("Protein (g)", color = MacroProtein) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("food_protein_input"),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = fatText,
                            onValueChange = { fatText = it },
                            label = { Text("Fat (g)", color = MacroFat) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("food_fat_input"),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Button(
                    onClick = {
                        val cal = caloriesText.toIntOrNull() ?: 0
                        if (foodName.isNotBlank() && cal > 0) {
                            onConfirm(
                                foodName,
                                selectedMeal,
                                cal,
                                carbsText.toFloatOrNull() ?: 0f,
                                proteinText.toFloatOrNull() ?: 0f,
                                fatText.toFloatOrNull() ?: 0f,
                                servingSize
                            )
                        }
                    },
                    enabled = foodName.isNotBlank() && (caloriesText.toIntOrNull() ?: 0) > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_food_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CoralCalories,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "Save Meal Entry",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
