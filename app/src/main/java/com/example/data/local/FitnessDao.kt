package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface FitnessDao {

    // Daily Activities
    @Query("SELECT * FROM daily_activities WHERE date = :date LIMIT 1")
    fun getDailyActivityFlow(date: String): Flow<DailyActivityEntity?>

    @Query("SELECT * FROM daily_activities WHERE date = :date LIMIT 1")
    suspend fun getDailyActivity(date: String): DailyActivityEntity?

    @Query("SELECT * FROM daily_activities ORDER BY date DESC LIMIT :limit")
    fun getRecentActivitiesFlow(limit: Int = 30): Flow<List<DailyActivityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateActivity(activity: DailyActivityEntity)

    @Query("UPDATE daily_activities SET manualSteps = manualSteps + :steps WHERE date = :date")
    suspend fun addManualSteps(date: String, steps: Int)

    @Query("UPDATE daily_activities SET waterMl = waterMl + :amountMl WHERE date = :date")
    suspend fun addWater(date: String, amountMl: Int)

    @Query("UPDATE daily_activities SET waterMl = 0 WHERE date = :date")
    suspend fun resetWater(date: String)

    // Food entries
    @Query("SELECT * FROM food_entries WHERE date = :date ORDER BY timestamp DESC")
    fun getFoodEntriesForDate(date: String): Flow<List<FoodEntryEntity>>

    @Query("SELECT * FROM food_entries ORDER BY timestamp DESC LIMIT 100")
    fun getAllFoodEntries(): Flow<List<FoodEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodEntry(entry: FoodEntryEntity): Long

    @Query("DELETE FROM food_entries WHERE id = :id")
    suspend fun deleteFoodEntry(id: Long)

    @Query("SELECT SUM(calories) FROM food_entries WHERE date = :date")
    fun getTotalCaloriesForDateFlow(date: String): Flow<Int?>

    // Workouts
    @Query("SELECT * FROM workouts WHERE date = :date ORDER BY timestamp DESC")
    fun getWorkoutsForDateFlow(date: String): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts ORDER BY timestamp DESC LIMIT 100")
    fun getAllWorkoutsFlow(): Flow<List<WorkoutEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Query("DELETE FROM workouts WHERE id = :id")
    suspend fun deleteWorkout(id: Long)

    @Query("SELECT SUM(caloriesBurned) FROM workouts WHERE date = :date")
    fun getTotalWorkoutCaloriesForDateFlow(date: String): Flow<Int?>

    // Wearable Data
    @Query("SELECT * FROM wearable_sync_data WHERE date = :date LIMIT 1")
    fun getWearableDataFlow(date: String): Flow<WearableDataEntity?>

    @Query("SELECT * FROM wearable_sync_data WHERE date = :date LIMIT 1")
    suspend fun getWearableData(date: String): WearableDataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWearableData(data: WearableDataEntity)

    // User profile
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfileFlow(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfile(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserProfile(profile: UserProfileEntity)

    @Transaction
    suspend fun recordHardwareSensorDelta(date: String, newRawSensorValue: Float) {
        val existing = getDailyActivity(date)
        if (existing == null) {
            insertOrUpdateActivity(
                DailyActivityEntity(
                    date = date,
                    hardwareSteps = 0,
                    manualSteps = 0,
                    waterMl = 0,
                    lastHardwareSensorValue = newRawSensorValue
                )
            )
        } else {
            val lastVal = existing.lastHardwareSensorValue
            val delta = if (lastVal < 0 || newRawSensorValue < lastVal) {
                1
            } else {
                (newRawSensorValue - lastVal).toInt().coerceIn(0, 5000)
            }
            insertOrUpdateActivity(
                existing.copy(
                    hardwareSteps = existing.hardwareSteps + delta,
                    lastHardwareSensorValue = newRawSensorValue
                )
            )
        }
    }

    @Transaction
    suspend fun recordDetectorStep(date: String) {
        val existing = getDailyActivity(date)
        if (existing == null) {
            insertOrUpdateActivity(
                DailyActivityEntity(
                    date = date,
                    hardwareSteps = 1,
                    manualSteps = 0,
                    waterMl = 0
                )
            )
        } else {
            insertOrUpdateActivity(
                existing.copy(hardwareSteps = existing.hardwareSteps + 1)
            )
        }
    }
}
