package com.example.data.local

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FitnessRepository(private val dao: FitnessDao) {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun getTodayDate(): String = LocalDate.now().format(dateFormatter)

    fun getDailyActivity(date: String): Flow<DailyActivityEntity?> =
        dao.getDailyActivityFlow(date)

    fun getRecentActivities(limit: Int = 14): Flow<List<DailyActivityEntity>> =
        dao.getRecentActivitiesFlow(limit)

    fun getFoodEntriesForDate(date: String): Flow<List<FoodEntryEntity>> =
        dao.getFoodEntriesForDate(date)

    fun getAllFoodEntries(): Flow<List<FoodEntryEntity>> =
        dao.getAllFoodEntries()

    fun getWorkoutsForDate(date: String): Flow<List<WorkoutEntity>> =
        dao.getWorkoutsForDateFlow(date)

    fun getAllWorkouts(): Flow<List<WorkoutEntity>> =
        dao.getAllWorkoutsFlow()

    fun getWearableData(date: String): Flow<WearableDataEntity?> =
        dao.getWearableDataFlow(date)

    fun getUserProfile(): Flow<UserProfileEntity?> =
        dao.getUserProfileFlow()

    suspend fun addManualSteps(date: String, steps: Int) {
        val existing = dao.getDailyActivity(date)
        if (existing == null) {
            dao.insertOrUpdateActivity(
                DailyActivityEntity(
                    date = date,
                    hardwareSteps = 0,
                    manualSteps = steps,
                    waterMl = 0
                )
            )
        } else {
            dao.addManualSteps(date, steps)
        }
    }

    suspend fun addWater(date: String, amountMl: Int) {
        val existing = dao.getDailyActivity(date)
        if (existing == null) {
            dao.insertOrUpdateActivity(
                DailyActivityEntity(
                    date = date,
                    hardwareSteps = 0,
                    manualSteps = 0,
                    waterMl = amountMl
                )
            )
        } else {
            dao.addWater(date, amountMl)
        }
    }

    suspend fun resetWater(date: String) {
        dao.resetWater(date)
    }

    suspend fun logFood(entry: FoodEntryEntity): Long {
        return dao.insertFoodEntry(entry)
    }

    suspend fun deleteFood(id: Long) {
        dao.deleteFoodEntry(id)
    }

    suspend fun logWorkout(workout: WorkoutEntity): Long {
        return dao.insertWorkout(workout)
    }

    suspend fun deleteWorkout(id: Long) {
        dao.deleteWorkout(id)
    }

    suspend fun updateWearableData(data: WearableDataEntity) {
        dao.insertOrUpdateWearableData(data)
    }

    suspend fun updateProfile(profile: UserProfileEntity) {
        dao.insertOrUpdateUserProfile(profile)
    }

    suspend fun recordHardwareSensorDelta(date: String, newRawSensorValue: Float) {
        dao.recordHardwareSensorDelta(date, newRawSensorValue)
    }

    suspend fun recordDetectorStep(date: String) {
        dao.recordDetectorStep(date)
    }

    suspend fun ensureInitialData() {
        val profile = dao.getUserProfile()
        if (profile == null) {
            dao.insertOrUpdateUserProfile(UserProfileEntity())
        }

        val today = getTodayDate()
        val todayActivity = dao.getDailyActivity(today)
        if (todayActivity == null) {
            // Seed today with active start
            dao.insertOrUpdateActivity(
                DailyActivityEntity(
                    date = today,
                    hardwareSteps = 0,
                    manualSteps = 6420,
                    waterMl = 1500
                )
            )
            // Seed some realistic meals for today
            dao.insertFoodEntry(
                FoodEntryEntity(
                    date = today,
                    mealType = MealType.BREAKFAST.name,
                    name = "Avocado Toast & Poached Eggs",
                    calories = 410,
                    carbsG = 34f,
                    proteinG = 18f,
                    fatG = 22f,
                    servingSize = "2 slices",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 5
                )
            )
            dao.insertFoodEntry(
                FoodEntryEntity(
                    date = today,
                    mealType = MealType.LUNCH.name,
                    name = "Grilled Chicken Quinoa Bowl",
                    calories = 580,
                    carbsG = 48f,
                    proteinG = 42f,
                    fatG = 14f,
                    servingSize = "1 bowl",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 2
                )
            )
            dao.insertFoodEntry(
                FoodEntryEntity(
                    date = today,
                    mealType = MealType.SNACK.name,
                    name = "Greek Yogurt with Berries & Honey",
                    calories = 190,
                    carbsG = 22f,
                    proteinG = 15f,
                    fatG = 3f,
                    servingSize = "1 cup",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 30
                )
            )

            // Seed a morning workout
            dao.insertWorkout(
                WorkoutEntity(
                    date = today,
                    exerciseType = ExerciseType.RUNNING.name,
                    durationMinutes = 32,
                    intensity = WorkoutIntensity.VIGOROUS.name,
                    caloriesBurned = 360,
                    distanceKm = 4.8f,
                    avgHeartRate = 148,
                    notes = "Morning tempo run in the park",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 4
                )
            )

            // Seed initial wearable sync data (e.g. Fitbit/Garmin health telemetry)
            dao.insertOrUpdateWearableData(
                WearableDataEntity(
                    date = today,
                    deviceSource = WearableSource.FITBIT.name,
                    lastSyncTimestamp = System.currentTimeMillis() - 1000 * 60 * 15,
                    wearableSteps = 6420,
                    avgHeartRate = 72,
                    restingHeartRate = 60,
                    sleepMinutesTotal = 460, // 7h 40m
                    sleepDeepMinutes = 95,
                    sleepRemMinutes = 110,
                    sleepLightMinutes = 255,
                    isConnected = true
                )
            )

            // Seed past 6 days history so user immediately enjoys charts and trends
            val now = LocalDate.now()
            val pastDays = listOf(
                Pair(now.minusDays(1), Pair(8740, 1920)),
                Pair(now.minusDays(2), Pair(10520, 2140)),
                Pair(now.minusDays(3), Pair(6890, 1850)),
                Pair(now.minusDays(4), Pair(11340, 2280)),
                Pair(now.minusDays(5), Pair(9450, 1990)),
                Pair(now.minusDays(6), Pair(7820, 1780))
            )
            for ((dayDate, stats) in pastDays) {
                val formatted = dayDate.format(dateFormatter)
                dao.insertOrUpdateActivity(
                    DailyActivityEntity(
                        date = formatted,
                        hardwareSteps = 0,
                        manualSteps = stats.first,
                        waterMl = 2250
                    )
                )
                dao.insertFoodEntry(
                    FoodEntryEntity(
                        date = formatted,
                        mealType = MealType.DINNER.name,
                        name = "Healthy Balanced Meal",
                        calories = stats.second,
                        carbsG = 160f,
                        proteinG = 95f,
                        fatG = 55f,
                        timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 24
                    )
                )
            }
        }
    }
}
