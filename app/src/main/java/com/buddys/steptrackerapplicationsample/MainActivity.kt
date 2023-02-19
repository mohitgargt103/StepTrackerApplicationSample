package com.buddys.steptrackerapplicationsample

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlin.math.sqrt


class MainActivity : AppCompatActivity(), SensorEventListener {
    lateinit var sensorManager: SensorManager
    lateinit var mSensorAcc: Sensor

    private val tvSteps by lazy {
        findViewById<TextView>(R.id.tv2)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get an instance of the SensorManager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mSensorAcc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    private var magnitudePrevious: Double = 0.0
    private var stepCount = 0

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun onSensorChanged(sensorEvent: SensorEvent) {
        if (sensorEvent != null) {
            val x_acceleration = sensorEvent.values[0]
            val y_acceleration = sensorEvent.values[1]
            val z_acceleration = sensorEvent.values[2]
            var Magnitude: Double =
                sqrt(x_acceleration * x_acceleration + y_acceleration * y_acceleration + z_acceleration * z_acceleration).toDouble();
            var MagnitudeDelta: Double = Magnitude - magnitudePrevious
            magnitudePrevious = Magnitude;

            if (MagnitudeDelta > 10) {
                stepCount++;
            }

            tvSteps.text = "Steps Tracked:  ${stepCount}"
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onResume() {
        super.onResume()
        if (mSensorAcc == null) {
            Toast.makeText(this, "No sensor detected on this device", Toast.LENGTH_SHORT).show()
        } else {
            sensorManager.registerListener(this, mSensorAcc, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    fun onReset(view: View) {
        stepCount = 0;
        tvSteps.text = "Steps Tracked:  ${stepCount}"
    }

}