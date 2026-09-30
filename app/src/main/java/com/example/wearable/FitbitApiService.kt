package com.example.wearable

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

// 1. Steps & Activity Response
@JsonClass(generateAdapter = true)
data class FitbitActivitySummary(
    val steps: Int? = 0,
    val caloriesOut: Int? = 0,
    val fairlyActiveMinutes: Int? = 0,
    val veryActiveMinutes: Int? = 0
)

@JsonClass(generateAdapter = true)
data class FitbitActivityResponse(
    val summary: FitbitActivitySummary? = null
)

// 2. Heart Rate Response
@JsonClass(generateAdapter = true)
data class FitbitHeartRateZone(
    val name: String? = null,
    val min: Int? = null,
    val max: Int? = null,
    val minutes: Int? = null
)

@JsonClass(generateAdapter = true)
data class FitbitHeartValue(
    val restingHeartRate: Int? = null,
    val heartRateZones: List<FitbitHeartRateZone>? = null
)

@JsonClass(generateAdapter = true)
data class FitbitHeartItem(
    val dateTime: String? = null,
    val value: FitbitHeartValue? = null
)

@JsonClass(generateAdapter = true)
data class FitbitHeartResponse(
    @Json(name = "activities-heart")
    val activitiesHeart: List<FitbitHeartItem>? = null
)

// 3. Sleep Response
@JsonClass(generateAdapter = true)
data class FitbitSleepStageLevels(
    val deep: FitbitSleepStageSummary? = null,
    val light: FitbitSleepStageSummary? = null,
    val rem: FitbitSleepStageSummary? = null,
    val wake: FitbitSleepStageSummary? = null
)

@JsonClass(generateAdapter = true)
data class FitbitSleepStageSummary(
    val count: Int? = 0,
    val minutes: Int? = 0
)

@JsonClass(generateAdapter = true)
data class FitbitSleepSummary(
    val totalMinutesAsleep: Int? = 0,
    val totalTimeInBed: Int? = 0,
    val stages: FitbitSleepStageLevels? = null
)

@JsonClass(generateAdapter = true)
data class FitbitSleepResponse(
    val summary: FitbitSleepSummary? = null
)

interface FitbitApiService {

    @GET("1/user/-/activities/date/{date}.json")
    suspend fun getDailyActivity(
        @Header("Authorization") authHeader: String,
        @Path("date") date: String
    ): FitbitActivityResponse

    @GET("1/user/-/activities/heart/date/{date}/1d.json")
    suspend fun getDailyHeartRate(
        @Header("Authorization") authHeader: String,
        @Path("date") date: String
    ): FitbitHeartResponse

    @GET("1.2/user/-/sleep/date/{date}.json")
    suspend fun getDailySleep(
        @Header("Authorization") authHeader: String,
        @Path("date") date: String
    ): FitbitSleepResponse

    companion object {
        private const val BASE_URL = "https://api.fitbit.com/"

        fun create(): FitbitApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(FitbitApiService::class.java)
        }
    }
}
