package com.kokorofy.music

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

data class DeviceTilt(val x: Float = 0f, val y: Float = 0f)

/**
 * 2-axis phone motion for the full player.
 *
 * Rotation-vector gives us all directions instead of a single left/right
 * axis. A low-pass filter removes the micro-jitter that is especially visible
 * on a large album cover.
 */
@Composable
fun rememberDeviceTilt(enabled: Boolean): DeviceTilt {
    val context = androidx.compose.ui.platform.LocalContext.current
    var tiltX by remember { mutableFloatStateOf(0f) }
    var tiltY by remember { mutableFloatStateOf(0f) }

    DisposableEffect(enabled) {
        if (!enabled) {
            tiltX = 0f
            tiltY = 0f
            onDispose { }
        } else {
            val manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val sensor = manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
                ?: manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

            if (sensor == null) {
                onDispose { }
            } else {
                var filteredX = 0f
                var filteredY = 0f
                val listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent) {
                        val targetX: Float
                        val targetY: Float

                        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                            val rotation = FloatArray(9)
                            SensorManager.getRotationMatrixFromVector(rotation, event.values)
                            val orientation = FloatArray(3)
                            SensorManager.getOrientation(rotation, orientation)

                            // Pitch + roll = full 2D movement.
                            targetX = (orientation[1] * 180f / Math.PI.toFloat() * 0.95f)
                                .coerceIn(-22f, 22f)
                            targetY = (orientation[2] * 180f / Math.PI.toFloat() * 0.95f)
                                .coerceIn(-22f, 22f)
                        } else {
                            targetX = (-event.values[0] * 2.2f).coerceIn(-22f, 22f)
                            targetY = (event.values[1] * 2.2f).coerceIn(-22f, 22f)
                        }

                        // Smooth but still responsive.
                        filteredX += (targetX - filteredX) * 0.16f
                        filteredY += (targetY - filteredY) * 0.16f
                        tiltX = filteredX
                        tiltY = filteredY
                    }

                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
                }
                manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
                onDispose { manager.unregisterListener(listener) }
            }
        }
    }
    return DeviceTilt(tiltX, tiltY)
}
