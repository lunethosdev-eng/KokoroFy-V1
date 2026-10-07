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
                val listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent) {
                        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                            val rotation = FloatArray(9)
                            SensorManager.getRotationMatrixFromVector(rotation, event.values)
                            val orientation = FloatArray(3)
                            SensorManager.getOrientation(rotation, orientation)
                            tiltX = (orientation[1] * 180f / Math.PI.toFloat()).coerceIn(-18f, 18f)
                            tiltY = (orientation[2] * 180f / Math.PI.toFloat()).coerceIn(-18f, 18f)
                        } else {
                            tiltX = (-event.values[0] * 2.2f).coerceIn(-18f, 18f)
                            tiltY = (event.values[1] * 2.2f).coerceIn(-18f, 18f)
                        }
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
