package com.example

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.FitnessViewModel
import com.example.ui.components.FitPulseBottomBar
import com.example.ui.components.FitPulseTab
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.NutritionScreen
import com.example.ui.screens.ProfileGoalsScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.theme.FitPulseTheme
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitPulseTheme {
                FitPulseMainScreen()
            }
        }
    }
}

@Composable
fun FitPulseMainScreen(
    viewModel: FitnessViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var currentTab by remember { mutableStateOf(FitPulseTab.TODAY) }
    var celebrationBannerText by remember { mutableStateOf<String?>(null) }

    // Multi-permission launcher for ACTIVITY_RECOGNITION and POST_NOTIFICATIONS
    val permissionsToRequest = buildList {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            add(Manifest.permission.ACTIVITY_RECOGNITION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val activityRecognitionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            results[Manifest.permission.ACTIVITY_RECOGNITION] ?: false
        } else true
        viewModel.onPermissionResult(activityRecognitionGranted)
    }

    // Health Connect Permission Launcher
    val healthConnectPermissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { _ ->
        viewModel.onHealthConnectPermissionsResult()
    }

    LaunchedEffect(Unit) {
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    LaunchedEffect(Unit) {
        viewModel.celebrationEvent.collectLatest { msg ->
            celebrationBannerText = msg
        }
    }

    // Handle back button on secondary tabs
    if (currentTab != FitPulseTab.TODAY) {
        BackHandler {
            currentTab = FitPulseTab.TODAY
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            FitPulseBottomBar(
                selectedTab = currentTab,
                onTabSelected = { currentTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                FitPulseTab.TODAY -> {
                    TodayScreen(
                        state = uiState,
                        celebrationMessage = celebrationBannerText,
                        onRequestPermission = {
                            if (permissionsToRequest.isNotEmpty()) {
                                permissionLauncher.launch(permissionsToRequest.toTypedArray())
                            }
                        },
                        onAddManualSteps = { steps -> viewModel.addManualSteps(steps) },
                        onAddWater = { ml -> viewModel.addWater(ml) },
                        onResetWater = { viewModel.resetWater() },
                        onLogFood = { name, meal, cal, carbs, prot, fat, serving ->
                            viewModel.logFood(name, meal, cal, carbs, prot, fat, serving)
                        },
                        onLogWorkout = { ex, dur, intens, dist, notes ->
                            viewModel.logWorkout(ex, dur, intens, dist, notes)
                        },
                        onDeleteWorkout = { id -> viewModel.deleteWorkout(id) },
                        onSyncWearable = { source, token -> viewModel.syncWearable(source, token) },
                        onSaveWearableConfig = { src, tok, client ->
                            viewModel.updateGoalsAndProfile(
                                dailyStepGoal = uiState.profile.dailyStepGoal,
                                weeklyStepGoal = uiState.profile.weeklyStepGoal,
                                dailyCalorieIntakeGoal = uiState.profile.dailyCalorieIntakeGoal,
                                weeklyCalorieIntakeGoal = uiState.profile.weeklyCalorieIntakeGoal,
                                dailyCalorieBurnGoal = uiState.profile.dailyCalorieBurnGoal,
                                weeklyCalorieBurnGoal = uiState.profile.weeklyCalorieBurnGoal,
                                waterGoalMl = uiState.profile.waterGoalMl,
                                weightKg = uiState.profile.weightKg,
                                heightCm = uiState.profile.heightCm,
                                notificationsEnabled = uiState.profile.notificationsEnabled,
                                activeWearableSource = src,
                                fitbitAccessToken = tok,
                                fitbitClientId = client
                            )
                        },
                        onRequestHealthConnectPermissions = {
                            if (viewModel.healthConnectManager.isSupported()) {
                                healthConnectPermissionLauncher.launch(viewModel.healthConnectManager.permissions)
                            } else {
                                try {
                                    context.startActivity(viewModel.healthConnectManager.getInstallHealthConnectIntent())
                                } catch (_: Exception) {
                                    viewModel.onHealthConnectPermissionsResult()
                                }
                            }
                        },
                        onExportToHealthConnect = {
                            viewModel.exportToHealthConnect()
                        },
                        onOpenHealthConnectSettings = {
                            try {
                                context.startActivity(viewModel.healthConnectManager.getHealthConnectSettingsIntent())
                            } catch (_: Exception) {
                                try {
                                    context.startActivity(Intent(Settings.ACTION_SETTINGS))
                                } catch (_: Exception) {}
                            }
                        }
                    )
                }

                FitPulseTab.NUTRITION -> {
                    NutritionScreen(
                        state = uiState,
                        onLogFood = { name, meal, cal, carbs, prot, fat, serving ->
                            viewModel.logFood(name, meal, cal, carbs, prot, fat, serving)
                        },
                        onDeleteFood = { id -> viewModel.deleteFood(id) }
                    )
                }

                FitPulseTab.HISTORY -> {
                    HistoryScreen(
                        state = uiState
                    )
                }

                FitPulseTab.GOALS -> {
                    ProfileGoalsScreen(
                        state = uiState,
                        onSaveGoals = { dailyStep, weeklyStep, dailyCal, weeklyCal, dailyBurn, weeklyBurn, water, weight, height, notify, src, tok, client ->
                            viewModel.updateGoalsAndProfile(
                                dailyStepGoal = dailyStep,
                                weeklyStepGoal = weeklyStep,
                                dailyCalorieIntakeGoal = dailyCal,
                                weeklyCalorieIntakeGoal = weeklyCal,
                                dailyCalorieBurnGoal = dailyBurn,
                                weeklyCalorieBurnGoal = weeklyBurn,
                                waterGoalMl = water,
                                weightKg = weight,
                                heightCm = height,
                                notificationsEnabled = notify,
                                activeWearableSource = src,
                                fitbitAccessToken = tok,
                                fitbitClientId = client
                            )
                        }
                    )
                }
            }
        }
    }
}
