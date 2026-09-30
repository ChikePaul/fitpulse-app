package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ExerciseType
import com.example.data.local.MealType
import com.example.data.local.WearableSource
import com.example.data.local.WorkoutEntity
import com.example.data.local.WorkoutIntensity
import com.example.ui.FitnessUiState
import com.example.ui.components.DualActivityRings
import com.example.ui.components.FoodLoggingDialog
import com.example.ui.components.GoalMilestoneCelebrationBanner
import com.example.ui.components.StepAdjustmentDialog
import com.example.ui.components.WearableSyncCard
import com.example.ui.components.WorkoutLoggingDialog
import com.example.ui.components.getExerciseIcon
import com.example.ui.theme.CoralCalories
import com.example.ui.theme.CyanWater
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.PurpleWorkout
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TodayScreen(
    state: FitnessUiState,
    celebrationMessage: String?,
    onRequestPermission: () -> Unit,
    onAddManualSteps: (Int) -> Unit,
    onAddWater: (Int) -> Unit,
    onResetWater: () -> Unit,
    onLogFood: (name: String, mealType: MealType, calories: Int, carbsG: Float, proteinG: Float, fatG: Float, servingSize: String) -> Unit,
    onLogWorkout: (exercise: ExerciseType, duration: Int, intensity: WorkoutIntensity, distanceKm: Float, notes: String) -> Unit,
    onDeleteWorkout: (Long) -> Unit,
    onSyncWearable: (WearableSource, String) -> Unit,
    onSaveWearableConfig: (source: String, token: String, clientId: String) -> Unit,
    onRequestHealthConnectPermissions: () -> Unit,
    onExportToHealthConnect: () -> Unit,
    onOpenHealthConnectSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showStepDialog by remember { mutableStateOf(false) }
    var showFoodDialog by remember { mutableStateOf(false) }
    var showWorkoutDialog by remember { mutableStateOf(false) }

    val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())

    val totalSteps = maxOf(state.todayActivity.totalSteps, state.wearableData?.wearableSteps ?: 0)
    val stepFraction = totalSteps.toFloat() / state.profile.dailyStepGoal.coerceAtLeast(1)
    val calorieFraction = state.nutrition.totalCalories.toFloat() / state.profile.dailyCalorieIntakeGoal.coerceAtLeast(1)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("today_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Top Date and Streak Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = state.displayDate,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "FitPulse Dashboard",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Streak Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CoralCalories.copy(alpha = 0.15f),
                    modifier = Modifier.testTag("streak_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = CoralCalories,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${state.streakDays} Day Streak",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CoralCalories
                            )
                        )
                    }
                }
            }
        }

        // Milestone Celebration Banner if triggered
        if (!celebrationMessage.isNullOrBlank()) {
            item {
                GoalMilestoneCelebrationBanner(celebrationMessage = celebrationMessage)
            }
        }

        // Hardware Step Sensor Banner if permission needed
        if (!state.sensorStatus.isPermissionGranted) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRequestPermission() }
                        .testTag("grant_permission_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Enable Device Step Sensor",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Count your daily steps automatically using your phone's motion sensor.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = onRequestPermission,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color(0xFF003915)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Enable", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // Concentric Rings & Core Stats Card
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DualActivityRings(
                        stepProgress = stepFraction,
                        calorieProgress = calorieFraction,
                        steps = totalSteps,
                        stepGoal = state.profile.dailyStepGoal,
                        caloriesIntake = state.nutrition.totalCalories,
                        caloriesBurned = state.totalBurnedCalories,
                        size = 220.dp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Secondary metrics row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        MetricSmallItem(
                            icon = Icons.Default.DirectionsRun,
                            iconTint = EmeraldPrimary,
                            label = "Distance",
                            value = "${String.format("%.2f", state.distanceKm)} km"
                        )
                        MetricSmallItem(
                            icon = Icons.Default.LocalFireDepartment,
                            iconTint = CoralCalories,
                            label = "Total Burned",
                            value = "${numberFormat.format(state.totalBurnedCalories)} kcal"
                        )
                        MetricSmallItem(
                            icon = Icons.Default.FitnessCenter,
                            iconTint = PurpleWorkout,
                            label = "Workouts",
                            value = "${state.todayWorkouts.size} logged"
                        )
                    }
                }
            }
        }

        // Quick Actions Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Default.DirectionsWalk,
                    label = "+ Walk",
                    accentColor = EmeraldPrimary,
                    onClick = { showStepDialog = true },
                    modifier = Modifier.weight(1f),
                    testTag = "quick_action_walk"
                )
                QuickActionButton(
                    icon = Icons.Default.Restaurant,
                    label = "+ Food",
                    accentColor = CoralCalories,
                    onClick = { showFoodDialog = true },
                    modifier = Modifier.weight(1f),
                    testTag = "quick_action_food"
                )
                QuickActionButton(
                    icon = Icons.Default.FitnessCenter,
                    label = "+ Workout",
                    accentColor = PurpleWorkout,
                    onClick = { showWorkoutDialog = true },
                    modifier = Modifier.weight(1f),
                    testTag = "quick_action_workout"
                )
                QuickActionButton(
                    icon = Icons.Default.WaterDrop,
                    label = "+ Water",
                    accentColor = CyanWater,
                    onClick = { onAddWater(250) },
                    modifier = Modifier.weight(1f),
                    testTag = "quick_action_water"
                )
            }
        }

        // Wearable Integration Card (Health Connect, Fitbit, Garmin, Apple Watch, Wear OS)
        item {
            WearableSyncCard(
                wearableData = state.wearableData,
                profile = state.profile,
                healthConnectStatus = state.healthConnectStatus,
                isSyncing = state.isSyncingWearable,
                syncMessage = state.lastSyncMessage,
                onSyncNow = onSyncWearable,
                onSaveCredentials = onSaveWearableConfig,
                onRequestHealthConnectPermissions = onRequestHealthConnectPermissions,
                onExportToHealthConnect = onExportToHealthConnect,
                onOpenHealthConnectSettings = onOpenHealthConnectSettings
            )
        }

        // Workouts logged today
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Workouts",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${state.workoutCaloriesBurned} kcal burned",
                    style = MaterialTheme.typography.labelMedium.copy(color = PurpleWorkout, fontWeight = FontWeight.Bold)
                )
            }
        }

        if (state.todayWorkouts.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No workouts logged today yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = { showWorkoutDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PurpleWorkout),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Log Exercise", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        } else {
            items(state.todayWorkouts) { workout ->
                val exerciseType = try {
                    ExerciseType.valueOf(workout.exerciseType)
                } catch (_: Exception) {
                    ExerciseType.RUNNING
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(PurpleWorkout.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getExerciseIcon(exerciseType),
                                    contentDescription = null,
                                    tint = PurpleWorkout,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = exerciseType.displayName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${workout.durationMinutes} min • ${workout.intensity.lowercase().replaceFirstChar { it.uppercase() }}" +
                                            if (workout.distanceKm > 0) " • ${workout.distanceKm} km" else "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "+${workout.caloriesBurned} kcal",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CoralCalories
                                )
                            )
                            IconButton(onClick = { onDeleteWorkout(workout.id) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Hydration Card
        item {
            val waterFrac = (state.todayActivity.waterMl.toFloat() / state.profile.waterGoalMl.coerceAtLeast(1)).coerceIn(0f, 1f)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.LocalDrink, contentDescription = null, tint = CyanWater)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Daily Hydration",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "${state.todayActivity.waterMl} / ${state.profile.waterGoalMl} ml",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = CyanWater
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { waterFrac },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = CyanWater,
                        trackColor = CyanWater.copy(alpha = 0.2f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onAddWater(250) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanWater.copy(alpha = 0.2f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+250 ml", color = CyanWater, style = MaterialTheme.typography.labelSmall)
                        }
                        Button(
                            onClick = { onAddWater(500) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanWater.copy(alpha = 0.2f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+500 ml", color = CyanWater, style = MaterialTheme.typography.labelSmall)
                        }
                        Button(
                            onClick = onResetWater,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reset", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Dialogs
    if (showStepDialog) {
        StepAdjustmentDialog(
            caloriesPerStep = state.profile.caloriesPerStep,
            strideLengthM = state.profile.strideLengthM,
            onDismiss = { showStepDialog = false },
            onAddSteps = onAddManualSteps
        )
    }

    if (showFoodDialog) {
        FoodLoggingDialog(
            onDismiss = { showFoodDialog = false },
            onConfirm = { name, mealType, calories, carbs, protein, fat, serving ->
                onLogFood(name, mealType, calories, carbs, protein, fat, serving)
                showFoodDialog = false
            }
        )
    }

    if (showWorkoutDialog) {
        WorkoutLoggingDialog(
            userProfile = state.profile,
            onDismiss = { showWorkoutDialog = false },
            onConfirm = { exercise, duration, intensity, distance, notes ->
                onLogWorkout(exercise, duration, intensity, distance, notes)
                showWorkoutDialog = false
            }
        )
    }
}

@Composable
fun MetricSmallItem(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
