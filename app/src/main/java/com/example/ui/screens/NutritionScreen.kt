package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.FoodEntryEntity
import com.example.data.local.MealType
import com.example.ui.FitnessUiState
import com.example.ui.components.FoodLoggingDialog
import com.example.ui.theme.CoralCalories
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MacroCarbs
import com.example.ui.theme.MacroFat
import com.example.ui.theme.MacroProtein
import java.text.NumberFormat
import java.util.Locale

@Composable
fun NutritionScreen(
    state: FitnessUiState,
    onLogFood: (name: String, mealType: MealType, calories: Int, carbsG: Float, proteinG: Float, fatG: Float, servingSize: String) -> Unit,
    onDeleteFood: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedMealForAdd by remember { mutableStateOf(MealType.BREAKFAST) }

    val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())
    val calorieGoal = state.profile.dailyCalorieIntakeGoal
    val consumed = state.nutrition.totalCalories
    val remaining = calorieGoal - consumed
    val intakeFraction = (consumed.toFloat() / calorieGoal.coerceAtLeast(1)).coerceIn(0f, 1f)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedMealForAdd = MealType.BREAKFAST
                    showAddDialog = true
                },
                containerColor = CoralCalories,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_food")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Food")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag("nutrition_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Nutrition & Calorie Tracker",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Log food intake and balance with daily active burn",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Calorie Budget Card
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Daily Calorie Budget",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (remaining >= 0) "${numberFormat.format(remaining)} kcal remaining" else "${numberFormat.format(-remaining)} kcal over budget",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (remaining >= 0) EmeraldPrimary else CoralCalories,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = CoralCalories,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${numberFormat.format(consumed)} / ${numberFormat.format(calorieGoal)}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = CoralCalories
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { intakeFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = if (consumed <= calorieGoal) CoralCalories else Color(0xFFFF1744),
                            trackColor = CoralCalories.copy(alpha = 0.2f)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stats Summary Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            CalorieStatItem(label = "Goal", value = "${numberFormat.format(calorieGoal)}")
                            CalorieStatItem(label = "Eaten", value = "${numberFormat.format(consumed)}")
                            CalorieStatItem(label = "Burned", value = "${numberFormat.format(state.totalBurnedCalories)}")
                            CalorieStatItem(
                                label = "Net",
                                value = "${numberFormat.format(consumed - state.totalBurnedCalories)}"
                            )
                        }
                    }
                }
            }

            // Macronutrient Breakdown Card
            item {
                val totalGrams = state.nutrition.totalCarbsG + state.nutrition.totalProteinG + state.nutrition.totalFatG
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Macronutrient Distribution",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MacroBarItem(
                                name = "Carbs",
                                grams = state.nutrition.totalCarbsG,
                                totalGrams = totalGrams,
                                color = MacroCarbs,
                                modifier = Modifier.weight(1f)
                            )
                            MacroBarItem(
                                name = "Protein",
                                grams = state.nutrition.totalProteinG,
                                totalGrams = totalGrams,
                                color = MacroProtein,
                                modifier = Modifier.weight(1f)
                            )
                            MacroBarItem(
                                name = "Fat",
                                grams = state.nutrition.totalFatG,
                                totalGrams = totalGrams,
                                color = MacroFat,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Categorized Meals: Breakfast, Lunch, Dinner, Snack
            MealType.entries.forEach { mealType ->
                val mealEntries = state.todayFoodEntries.filter { it.mealType == mealType.name }
                val mealCalories = mealEntries.sumOf { it.calories }

                item {
                    MealCategorySection(
                        mealType = mealType,
                        totalCalories = mealCalories,
                        entries = mealEntries,
                        onAddFoodClick = {
                            selectedMealForAdd = mealType
                            showAddDialog = true
                        },
                        onDeleteEntry = onDeleteFood
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    if (showAddDialog) {
        FoodLoggingDialog(
            initialMealType = selectedMealForAdd,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, mealType, calories, carbs, protein, fat, serving ->
                onLogFood(name, mealType, calories, carbs, protein, fat, serving)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun MealCategorySection(
    mealType: MealType,
    totalCalories: Int,
    entries: List<FoodEntryEntity>,
    onAddFoodClick: () -> Unit,
    onDeleteEntry: (Long) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(CoralCalories.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Restaurant, contentDescription = null, tint = CoralCalories, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = mealType.displayName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$totalCalories kcal",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = CoralCalories)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(onClick = onAddFoodClick, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add to ${mealType.displayName}", tint = CoralCalories)
                    }
                }
            }

            if (entries.isEmpty()) {
                Text(
                    text = "No items logged yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                entries.forEach { food ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = food.name,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${food.servingSize} • C: ${food.carbsG.toInt()}g  P: ${food.proteinG.toInt()}g  F: ${food.fatG.toInt()}g",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${food.calories} kcal",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(onClick = { onDeleteEntry(food.id) }, modifier = Modifier.size(28.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MacroBarItem(
    name: String,
    grams: Float,
    totalGrams: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    val pct = if (totalGrams > 0) ((grams / totalGrams) * 100).toInt() else 0
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, color = color))
            Text(text = "$pct%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { if (totalGrams > 0) grams / totalGrams else 0f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "${grams.toInt()}g", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun CalorieStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
