package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class WearableSource(val displayName: String) {
    HEALTH_CONNECT("Health Connect"),
    FITBIT("Fitbit"),
    GARMIN("Garmin"),
    APPLE_WATCH("Apple Watch"),
    WEAR_OS("Wear OS")
}

@Entity(tableName = "wearable_sync_data")
data class WearableDataEntity(
    @PrimaryKey
    val date: String, // Format: YYYY-MM-DD
    val deviceSource: String = WearableSource.FITBIT.name,
    val lastSyncTimestamp: Long = 0L,
    val wearableSteps: Int = 0,
    val avgHeartRate: Int = 0,
    val restingHeartRate: Int = 0,
    val sleepMinutesTotal: Int = 0,
    val sleepDeepMinutes: Int = 0,
    val sleepRemMinutes: Int = 0,
    val sleepLightMinutes: Int = 0,
    val isConnected: Boolean = false
) {
    val sleepHoursFormatted: String
        get() {
            val hours = sleepMinutesTotal / 60
            val mins = sleepMinutesTotal % 60
            return "${hours}h ${mins}m"
        }
}
