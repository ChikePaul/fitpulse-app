package com.example.sensor

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SensorStatus(
    val hasHardwareSensor: Boolean = false,
    val sensorType: String = "None",
    val isPermissionGranted: Boolean = false,
    val isListening: Boolean = false,
    val rawSensorStepCount: Float = 0f
)

class StepSensorManager(
    private val context: Context,
    private val onStepCountDelta: (rawStepCount: Float) -> Unit,
    private val onSingleStepDetected: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val stepCounterSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val stepDetectorSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    private val _status = MutableStateFlow(SensorStatus())
    val status: StateFlow<SensorStatus> = _status.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        updateSensorAvailability()
    }

    fun hasActivityRecognitionPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun updateSensorAvailability() {
        val hasCounter = stepCounterSensor != null
        val hasDetector = stepDetectorSensor != null
        val type = when {
            hasCounter -> "Step Counter (Hardware)"
            hasDetector -> "Step Detector (Hardware)"
            else -> "Accelerometer / Virtual"
        }
        val permitted = hasActivityRecognitionPermission()
        _status.value = _status.value.copy(
            hasHardwareSensor = hasCounter || hasDetector,
            sensorType = type,
            isPermissionGranted = permitted
        )
    }

    fun startListening() {
        if (!hasActivityRecognitionPermission()) {
            updateSensorAvailability()
            return
        }

        var registered = false
        stepCounterSensor?.let { sensor ->
            val ok = sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI) == true
            if (ok) registered = true
        }

        if (!registered) {
            stepDetectorSensor?.let { sensor ->
                val ok = sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI) == true
                if (ok) registered = true
            }
        }

        _status.value = _status.value.copy(
            isListening = registered,
            isPermissionGranted = true
        )
    }

    fun stopListening() {
        try {
            sensorManager?.unregisterListener(this)
        } catch (_: Exception) {}
        _status.value = _status.value.copy(isListening = false)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                val rawCount = event.values.firstOrNull() ?: return
                _status.value = _status.value.copy(rawSensorStepCount = rawCount)
                scope.launch {
                    onStepCountDelta(rawCount)
                }
            }
            Sensor.TYPE_STEP_DETECTOR -> {
                if (event.values.firstOrNull() == 1.0f) {
                    scope.launch {
                        onSingleStepDetected()
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
