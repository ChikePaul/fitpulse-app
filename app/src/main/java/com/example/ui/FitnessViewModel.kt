package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.FitPulseApp
import com.example.data.local.DailyActivityEntity
import com.example.data.local.ExerciseType
import com.example.data.local.FoodEntryEntity
import com.example.data.local.MealType
import com.example.data.local.UserProfileEntity
import com.example.data.local.WearableDataEntity
import com.example.data.local.WearableSource
import com.example.data.local.WorkoutEntity
import com.example.data.local.WorkoutIntensity
import com.example.healthconnect.HealthConnectManager
import com.example.healthconnect.HealthConnectStatus
import com.example.notification.GoalNotificationHelper
import com.example.sensor.SensorStatus
import com.example.sensor.StepSensorManager
import com.example.wearable.SyncResult
import com.example.wearable.WearableSyncManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class DailyNutritionSummary(
    val totalCalories: Int = 0,
    val breakfastCalories: Int = 0,
    val lunchCalories: Int = 0,
    val dinnerCalories: Int = 0,
    val snackCalories: Int = 0,
    val totalCarbsG: Float = 0f,
    val totalProteinG: Float = 0f,
    val totalFatG: Float = 0f
)

data class WeeklyGoalProgress(
    val weeklyStepsTaken: Int = 0,
    val weeklyStepGoal: Int = 70000,
    val weeklyStepFraction: Float = 0f,
    val weeklyBurnedCalories: Int = 0,
    val weeklyCalorieBurnGoal: Int = 3500,
    val weeklyBurnFraction: Float = 0f,
    val weeklyIntakeCalories: Int = 0,
    val weeklyCalorieIntakeGoal: Int = 14000
)

data class FitnessUiState(
    val dateString: String = "",
    val displayDate: String = "",
    val todayActivity: DailyActivityEntity = DailyActivityEntity(date = ""),
    val todayFoodEntries: List<FoodEntryEntity> = emptyList(),
    val todayWorkouts: List<WorkoutEntity> = emptyList(),
    val wearableData: WearableDataEntity? = null,
    val recentActivities: List<DailyActivityEntity> = emptyList(),
    val profile: UserProfileEntity = UserProfileEntity(),
    val sensorStatus: SensorStatus = SensorStatus(),
    val healthConnectStatus: HealthConnectStatus = HealthConnectStatus(),
    val nutrition: DailyNutritionSummary = DailyNutritionSummary(),
    val workoutCaloriesBurned: Int = 0,
    val stepCaloriesBurned: Int = 0,
    val totalBurnedCalories: Int = 0,
    val distanceKm: Float = 0f,
    val activeMinutes: Int = 0,
    val streakDays: Int = 3,
    val weeklyGoals: WeeklyGoalProgress = WeeklyGoalProgress(),
    val isSyncingWearable: Boolean = false,
    val lastSyncMessage: String? = null
)

class FitnessViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as FitPulseApp).repository
    private val todayDate = repository.getTodayDate()
    private val notificationHelper = GoalNotificationHelper(application)
    private val wearableManager = WearableSyncManager(application)
    val healthConnectManager = wearableManager.healthConnectManager

    private val sensorManager = StepSensorManager(
        context = application,
        onStepCountDelta = { rawValue ->
            viewModelScope.launch {
                repository.recordHardwareSensorDelta(todayDate, rawValue)
                checkMilestones()
            }
        },
        onSingleStepDetected = {
            viewModelScope.launch {
                repository.recordDetectorStep(todayDate)
                checkMilestones()
            }
        }
    )

    private val _sensorStatus = sensorManager.status
    private val _isSyncing = MutableStateFlow(false)
    private val _syncMessage = MutableStateFlow<String?>(null)
    private val _healthConnectStatus = MutableStateFlow(HealthConnectStatus())

    private val _celebrationEvent = MutableSharedFlow<String>()
    val celebrationEvent: SharedFlow<String> = _celebrationEvent.asSharedFlow()

    private var hasNotifiedStepGoalToday = false
    private var hasNotifiedCalorieGoalToday = false

    private val todayActivityFlow = repository.getDailyActivity(todayDate)
    private val foodEntriesFlow = repository.getFoodEntriesForDate(todayDate)
    private val workoutsFlow = repository.getWorkoutsForDate(todayDate)
    private val wearableFlow = repository.getWearableData(todayDate)
    private val recentActivitiesFlow = repository.getRecentActivities(14)
    private val userProfileFlow = repository.getUserProfile()

    val uiState: StateFlow<FitnessUiState> = combine(
        combine(todayActivityFlow, foodEntriesFlow, workoutsFlow, wearableFlow) { act, foods, wos, wear ->
            Quadruple(act, foods, wos, wear)
        },
        combine(recentActivitiesFlow, userProfileFlow, _sensorStatus, _isSyncing) { recents, prof, sensor, syncing ->
            Quadruple(recents, prof, sensor, syncing)
        },
        combine(_syncMessage, _healthConnectStatus) { syncMsg, hcStat ->
            Pair(syncMsg, hcStat)
        }
    ) { (activity, foods, workouts, wearable), (recents, profileOrNull, sensorStat, syncing), (syncMsg, hcStat) ->
        val currentActivity = activity ?: DailyActivityEntity(date = todayDate)
        val currentProfile = profileOrNull ?: UserProfileEntity()

        val wearableSteps = wearable?.wearableSteps ?: 0
        val baseSteps = currentActivity.totalSteps
        val effectiveSteps = maxOf(baseSteps, wearableSteps)

        val distance = (effectiveSteps * currentProfile.strideLengthM) / 1000f
        val stepBurn = (effectiveSteps * currentProfile.caloriesPerStep).toInt()
        val workoutBurn = workouts.sumOf { it.caloriesBurned }
        val totalBurn = stepBurn + workoutBurn
        val activeMins = (effectiveSteps / 110).coerceAtLeast(0) + workouts.sumOf { it.durationMinutes }

        // Nutrition calculations
        var bCalories = 0
        var lCalories = 0
        var dCalories = 0
        var sCalories = 0
        var carbs = 0f
        var protein = 0f
        var fat = 0f
        var totCal = 0

        foods.forEach { food ->
            totCal += food.calories
            carbs += food.carbsG
            protein += food.proteinG
            fat += food.fatG
            when (food.mealType) {
                MealType.BREAKFAST.name -> bCalories += food.calories
                MealType.LUNCH.name -> lCalories += food.calories
                MealType.DINNER.name -> dCalories += food.calories
                MealType.SNACK.name -> sCalories += food.calories
            }
        }

        val parsedDate = try {
            LocalDate.parse(todayDate)
        } catch (_: Exception) {
            LocalDate.now()
        }
        val displayDate = parsedDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault()))

        // Streak
        val streak = recents.count { it.totalSteps >= currentProfile.dailyStepGoal } + 1

        // Weekly aggregates
        val past7Days = recents.take(6)
        val weeklySteps = past7Days.sumOf { it.totalSteps } + effectiveSteps
        val weeklyStepFrac = (weeklySteps.toFloat() / currentProfile.weeklyStepGoal.coerceAtLeast(1)).coerceIn(0f, 1f)

        val weeklyBurn = past7Days.sumOf { (it.totalSteps * currentProfile.caloriesPerStep).toInt() } + totalBurn
        val weeklyBurnFrac = (weeklyBurn.toFloat() / currentProfile.weeklyCalorieBurnGoal.coerceAtLeast(1)).coerceIn(0f, 1f)

        FitnessUiState(
            dateString = todayDate,
            displayDate = displayDate,
            todayActivity = currentActivity,
            todayFoodEntries = foods,
            todayWorkouts = workouts,
            wearableData = wearable,
            recentActivities = recents,
            profile = currentProfile,
            sensorStatus = sensorStat,
            healthConnectStatus = hcStat,
            nutrition = DailyNutritionSummary(
                totalCalories = totCal,
                breakfastCalories = bCalories,
                lunchCalories = lCalories,
                dinnerCalories = dCalories,
                snackCalories = sCalories,
                totalCarbsG = carbs,
                totalProteinG = protein,
                totalFatG = fat
            ),
            workoutCaloriesBurned = workoutBurn,
            stepCaloriesBurned = stepBurn,
            totalBurnedCalories = totalBurn,
            distanceKm = distance,
            activeMinutes = activeMins,
            streakDays = streak.coerceAtLeast(1),
            weeklyGoals = WeeklyGoalProgress(
                weeklyStepsTaken = weeklySteps,
                weeklyStepGoal = currentProfile.weeklyStepGoal,
                weeklyStepFraction = weeklyStepFrac,
                weeklyBurnedCalories = weeklyBurn,
                weeklyCalorieBurnGoal = currentProfile.weeklyCalorieBurnGoal,
                weeklyBurnFraction = weeklyBurnFrac,
                weeklyIntakeCalories = totCal * 7,
                weeklyCalorieIntakeGoal = currentProfile.weeklyCalorieIntakeGoal
            ),
            isSyncingWearable = syncing,
            lastSyncMessage = syncMsg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FitnessUiState(
            dateString = todayDate,
            displayDate = "Today"
        )
    )

    init {
        if (sensorManager.hasActivityRecognitionPermission()) {
            sensorManager.startListening()
        }
        refreshHealthConnectStatus()
    }

    fun refreshHealthConnectStatus() {
        viewModelScope.launch {
            _healthConnectStatus.value = healthConnectManager.checkStatus()
        }
    }

    private fun checkMilestones() {
        val state = uiState.value
        val steps = state.todayActivity.totalSteps
        val stepGoal = state.profile.dailyStepGoal

        if (steps >= stepGoal && !hasNotifiedStepGoalToday) {
            hasNotifiedStepGoalToday = true
            notificationHelper.notifyStepGoalReached(steps, stepGoal)
            viewModelScope.launch {
                _celebrationEvent.emit("🎉 Congratulations! You reached your daily goal of $stepGoal steps!")
            }
        }

        val burned = state.totalBurnedCalories
        val burnGoal = state.profile.dailyCalorieBurnGoal
        if (burned >= burnGoal && !hasNotifiedCalorieGoalToday) {
            hasNotifiedCalorieGoalToday = true
            notificationHelper.notifyCalorieBurnGoalReached(burned, burnGoal)
            viewModelScope.launch {
                _celebrationEvent.emit("🔥 Amazing! You hit your calorie burn goal of $burnGoal kcal!")
            }
        }
    }

    fun onPermissionResult(granted: Boolean) {
        sensorManager.updateSensorAvailability()
        if (granted) {
            sensorManager.startListening()
        }
    }

    fun onHealthConnectPermissionsResult() {
        refreshHealthConnectStatus()
        syncWearable(WearableSource.HEALTH_CONNECT)
    }

    fun addManualSteps(steps: Int) {
        viewModelScope.launch {
            repository.addManualSteps(todayDate, steps)
            checkMilestones()
        }
    }

    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            repository.addWater(todayDate, amountMl)
        }
    }

    fun resetWater() {
        viewModelScope.launch {
            repository.resetWater(todayDate)
        }
    }

    fun logFood(
        name: String,
        mealType: MealType,
        calories: Int,
        carbsG: Float = 0f,
        proteinG: Float = 0f,
        fatG: Float = 0f,
        servingSize: String = "1 serving"
    ) {
        viewModelScope.launch {
            repository.logFood(
                FoodEntryEntity(
                    date = todayDate,
                    mealType = mealType.name,
                    name = name.trim(),
                    calories = calories,
                    carbsG = carbsG,
                    proteinG = proteinG,
                    fatG = fatG,
                    servingSize = servingSize.ifBlank { "1 serving" },
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteFood(id: Long) {
        viewModelScope.launch {
            repository.deleteFood(id)
        }
    }

    fun logWorkout(
        exerciseType: ExerciseType,
        durationMinutes: Int,
        intensity: WorkoutIntensity,
        distanceKm: Float = 0f,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val profile = uiState.value.profile
            val burned = profile.calculateWorkoutCalories(exerciseType, durationMinutes, intensity)
            val workout = WorkoutEntity(
                date = todayDate,
                exerciseType = exerciseType.name,
                durationMinutes = durationMinutes,
                intensity = intensity.name,
                caloriesBurned = burned,
                distanceKm = distanceKm,
                avgHeartRate = 120 + (intensity.multiplier * 20).toInt(),
                notes = notes,
                timestamp = System.currentTimeMillis()
            )
            repository.logWorkout(workout)
            checkMilestones()

            // Also write to Health Connect if available
            val now = Instant.now()
            val startTime = now.minusSeconds((durationMinutes * 60).toLong())
            healthConnectManager.writeCaloriesBurnedToHealthConnect(burned.toDouble(), startTime, now)
        }
    }

    fun deleteWorkout(id: Long) {
        viewModelScope.launch {
            repository.deleteWorkout(id)
        }
    }

    fun syncWearable(source: WearableSource, token: String = "") {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncMessage.value = "Syncing with ${source.displayName}..."
            when (val result = wearableManager.syncWearable(todayDate, source, token)) {
                is SyncResult.Success -> {
                    repository.updateWearableData(result.data)
                    _syncMessage.value = "Synced successfully with ${source.displayName}!"
                    checkMilestones()
                }
                is SyncResult.Error -> {
                    _syncMessage.value = "Sync failed: ${result.message}"
                }
            }
            _isSyncing.value = false
            refreshHealthConnectStatus()
        }
    }

    fun exportToHealthConnect() {
        viewModelScope.launch {
            _isSyncing.value = true
            val now = Instant.now()
            val startOfDay = now.minusSeconds(3600 * 8)
            val state = uiState.value

            val stepsOk = healthConnectManager.writeStepsToHealthConnect(
                steps = state.todayActivity.totalSteps.toLong(),
                startTime = startOfDay,
                endTime = now
            )
            val caloriesOk = healthConnectManager.writeCaloriesBurnedToHealthConnect(
                caloriesKcal = state.totalBurnedCalories.toDouble(),
                startTime = startOfDay,
                endTime = now
            )
            val waterOk = healthConnectManager.writeHydrationToHealthConnect(
                milliliters = state.todayActivity.waterMl,
                time = now
            )

            if (stepsOk || caloriesOk || waterOk) {
                _syncMessage.value = "Data exported to Android Health Connect!"
            } else {
                _syncMessage.value = "Health Connect export requires permissions or installed provider."
            }
            _isSyncing.value = false
        }
    }

    fun updateGoalsAndProfile(
        dailyStepGoal: Int,
        weeklyStepGoal: Int,
        dailyCalorieIntakeGoal: Int,
        weeklyCalorieIntakeGoal: Int,
        dailyCalorieBurnGoal: Int,
        weeklyCalorieBurnGoal: Int,
        waterGoalMl: Int,
        weightKg: Float,
        heightCm: Float,
        notificationsEnabled: Boolean,
        activeWearableSource: String,
        fitbitAccessToken: String,
        fitbitClientId: String
    ) {
        viewModelScope.launch {
            val current = uiState.value.profile
            repository.updateProfile(
                current.copy(
                    dailyStepGoal = dailyStepGoal.coerceAtLeast(1000),
                    weeklyStepGoal = weeklyStepGoal.coerceAtLeast(7000),
                    dailyCalorieIntakeGoal = dailyCalorieIntakeGoal.coerceAtLeast(500),
                    weeklyCalorieIntakeGoal = weeklyCalorieIntakeGoal.coerceAtLeast(3500),
                    dailyCalorieBurnGoal = dailyCalorieBurnGoal.coerceAtLeast(100),
                    weeklyCalorieBurnGoal = weeklyCalorieBurnGoal.coerceAtLeast(700),
                    waterGoalMl = waterGoalMl.coerceAtLeast(500),
                    weightKg = weightKg.coerceAtLeast(30f),
                    heightCm = heightCm.coerceAtLeast(100f),
                    notificationsEnabled = notificationsEnabled,
                    activeWearableSource = activeWearableSource,
                    fitbitAccessToken = fitbitAccessToken,
                    fitbitClientId = fitbitClientId,
                    isWearableLinked = fitbitAccessToken.isNotBlank() || activeWearableSource != WearableSource.FITBIT.name
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.stopListening()
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
