package com.ar.dev.simplear.app

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.content.Context.SENSOR_SERVICE




class CurrentAzimuth(private var onAzimuthChangedListener: OnAzimuthChangedListener, private var context: Context) : SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var sensor: Sensor
    private var azimuthFrom = 0
    private var azimuthTo = 0

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int){}

    override fun onSensorChanged(event: SensorEvent?) {
       azimuthFrom = azimuthTo

        val orientation = FloatArray(3)
        val rMat = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(rMat, event?.values)
        azimuthTo = (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0].toDouble()) + 360).toInt() % 360
        onAzimuthChangedListener.onAzimuthChanged(azimuthFrom .toFloat(), azimuthTo.toFloat())
    }

    fun start() {
        sensorManager = context.getSystemService(SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        sensorManager.registerListener(this, sensor,
                SensorManager.SENSOR_DELAY_UI)
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    fun setOnShakeListener(listener: OnAzimuthChangedListener) {
        onAzimuthChangedListener = listener
    }
}