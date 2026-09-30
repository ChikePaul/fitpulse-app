package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.ExerciseType
import com.example.data.local.UserProfileEntity
import com.example.data.local.WorkoutIntensity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("FitPulse", appName)
    }

    @Test
    fun `calculate workout calories based on MET and weight`() {
        val profile = UserProfileEntity(weightKg = 70f)
        // Running at vigorous intensity for 30 minutes
        val calories = profile.calculateWorkoutCalories(
            exerciseType = ExerciseType.RUNNING,
            durationMinutes = 30,
            intensity = WorkoutIntensity.VIGOROUS
        )
        assertTrue("Calories should be positive and realistic", calories in 300..500)
    }

    @Test
    fun `daily step goals and weekly step goal default values`() {
        val profile = UserProfileEntity()
        assertEquals(10000, profile.dailyStepGoal)
        assertEquals(70000, profile.weeklyStepGoal)
    }
}
