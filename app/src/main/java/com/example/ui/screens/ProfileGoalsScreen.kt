package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.FitnessUiState
import com.example.ui.components.DailyGoalItemCard
import com.example.ui.theme.CoralCalories
import com.example.ui.theme.CyanWater
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.PurpleWorkout
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ProfileGoalsScreen(
    state: FitnessUiState,
    onSaveGoals: (
        dailyStep: Int,
        weeklyStep: Int,
        dailyCalorieIntake: Int,
        weeklyCalorieIntake: Int,
        dailyCalorieBurn: Int,
        weeklyCalorieBurn: Int,
        waterGoalMl: Int,
        weightKg: Float,
        heightCm: Float,
        notifications: Boolean,
        activeSource: String,
        fitbitToken: String,
        fitbitClientId: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val profile = state.profile
    val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())

    var dailyStepGoal by remember(profile.dailyStepGoal) { mutableStateOf(profile.dailyStepGoal) }
    var weeklyStepGoal by remember(profile.weeklyStepGoal) { mutableStateOf(profile.weeklyStepGoal) }

    var dailyCalorieIntakeGoal by remember(profile.dailyCalorieIntakeGoal) { mutableStateOf(profile.dailyCalorieIntakeGoal) }
    var weeklyCalorieIntakeGoal by remember(profile.weeklyCalorieIntakeGoal) { mutableStateOf(profile.weeklyCalorieIntakeGoal) }

    var dailyCalorieBurnGoal by remember(profile.dailyCalorieBurnGoal) { mutableStateOf(profile.dailyCalorieBurnGoal) }
    var weeklyCalorieBurnGoal by remember(profile.weeklyCalorieBurnGoal) { mutableStateOf(profile.weeklyCalorieBurnGoal) }

    var waterGoalMl by remember(profile.waterGoalMl) { mutableStateOf(profile.waterGoalMl) }
    var weightText by remember(profile.weightKg) { mutableStateOf(profile.weightKg.toInt().toString()) }
    var heightText by remember(profile.heightCm) { mutableStateOf(profile.heightCm.toInt().toString()) }

    var notificationsEnabled by remember(profile.notificationsEnabled) { mutableStateOf(profile.notificationsEnabled) }
    var showSavedMessage by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("goals_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Custom Goals & Targets",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Define your targets and track daily & weekly completion",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Live Goal Progress Cards
        item {
            Text(
                text = "Current Goal Status",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            val totalSteps = maxOf(state.todayActivity.totalSteps, state.wearableData?.wearableSteps ?: 0)
            DailyGoalItemCard(
                title = "Daily Step Goal",
                currentValue = totalSteps,
                goalValue = profile.dailyStepGoal,
                unit = "steps",
                accentColor = EmeraldPrimary,
                icon = Icons.Default.DirectionsWalk
            )
        }

        item {
            DailyGoalItemCard(
                title = "Daily Calorie Burn Target",
                currentValue = state.totalBurnedCalories,
                goalValue = profile.dailyCalorieBurnGoal,
                unit = "kcal",
                accentColor = CoralCalories,
                icon = Icons.Default.LocalFireDepartment
            )
        }

        item {
            DailyGoalItemCard(
                title = "Weekly Step Target",
                currentValue = state.weeklyGoals.weeklyStepsTaken,
                goalValue = profile.weeklyStepGoal,
                unit = "steps",
                accentColor = EmeraldPrimary,
                icon = Icons.Default.EmojiEvents
            )
        }

        item {
            DailyGoalItemCard(
                title = "Weekly Calorie Burn Target",
                currentValue = state.weeklyGoals.weeklyBurnedCalories,
                goalValue = profile.weeklyCalorieBurnGoal,
                unit = "kcal",
                accentColor = PurpleWorkout,
                icon = Icons.Default.FitnessCenter
            )
        }

        // Customize Daily Step Goal
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Daily Step Goal: ${numberFormat.format(dailyStepGoal)} steps",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(6000, 8000, 10000, 12000, 15000).forEach { steps ->
                            FilterChip(
                                selected = dailyStepGoal == steps,
                                onClick = {
                                    dailyStepGoal = steps
                                    weeklyStepGoal = steps * 7
                                },
                                label = { Text("${steps / 1000}k", style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldPrimary,
                                    selectedLabelColor = Color(0xFF003915)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Customize Calorie Targets
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Daily Calorie Intake Budget: ${numberFormat.format(dailyCalorieIntakeGoal)} kcal",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(1600, 1800, 2000, 2200, 2500).forEach { cals ->
                            FilterChip(
                                selected = dailyCalorieIntakeGoal == cals,
                                onClick = {
                                    dailyCalorieIntakeGoal = cals
                                    weeklyCalorieIntakeGoal = cals * 7
                                },
                                label = { Text("$cals", style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CoralCalories,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Daily Active Burn Target: ${numberFormat.format(dailyCalorieBurnGoal)} kcal",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(300, 450, 500, 650, 800).forEach { burn ->
                            FilterChip(
                                selected = dailyCalorieBurnGoal == burn,
                                onClick = {
                                    dailyCalorieBurnGoal = burn
                                    weeklyCalorieBurnGoal = burn * 7
                                },
                                label = { Text("$burn", style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CoralCalories,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Body Metrics
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Body Parameters (for accurate MET & burn)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = weightText,
                            onValueChange = { weightText = it },
                            label = { Text("Weight (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = heightText,
                            onValueChange = { heightText = it },
                            label = { Text("Height (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }

        // Notification Settings
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = EmeraldPrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Milestone Notifications",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Receive alert when daily step or calorie goal is achieved",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
                    )
                }
            }
        }

        // Save Button
        item {
            Button(
                onClick = {
                    onSaveGoals(
                        dailyStepGoal,
                        weeklyStepGoal,
                        dailyCalorieIntakeGoal,
                        weeklyCalorieIntakeGoal,
                        dailyCalorieBurnGoal,
                        weeklyCalorieBurnGoal,
                        waterGoalMl,
                        weightText.toFloatOrNull() ?: 70f,
                        heightText.toFloatOrNull() ?: 175f,
                        notificationsEnabled,
                        profile.activeWearableSource,
                        profile.fitbitAccessToken,
                        profile.fitbitClientId
                    )
                    showSavedMessage = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_goals_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = Color(0xFF003915)
                )
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (showSavedMessage) "Goals Updated Successfully!" else "Save Goal Settings",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
