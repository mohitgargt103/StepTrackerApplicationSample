package com.buddys.steptrackerapplicationsample

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.pow
import kotlin.math.sqrt

class DebugActivity : AppCompatActivity(), SensorEventListener {

    companion object {
        private const val SMOOTHING_WINDOW_SIZE = 20
        var mStepCounterAndroid = 0f
        var mInitialStepCount = 0f
    }

    var mSensorManager: SensorManager? = null
    var mSensorCount: Sensor? = null
    var mSensorAcc: Sensor? = null
    private val mRawAccelValues = FloatArray(3)

    // smoothing accelerometer signal variables
    private val mAccelValueHistory = Array(3) {
        FloatArray(
            SMOOTHING_WINDOW_SIZE
        )
    }
    private val mRunningAccelTotal = FloatArray(3)
    private val mCurAccelAvg = FloatArray(3)
    private var mCurReadIndex = 0


    private var mGraph1LastXValue = 0.0
    private var mGraph2LastXValue = 0.0

    private var lastMag = 0.0
    private var avgMag = 0.0
    private var netMag = 0.0

    lateinit var androidStep: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)
        androidStep = findViewById<TextView>(R.id.tv2)
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mSensorManager?.let {
            mSensorCount = it.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            mSensorAcc = it.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            it.registerListener(this, mSensorCount, SensorManager.SENSOR_DELAY_UI)
            it.registerListener(this, mSensorAcc, SensorManager.SENSOR_DELAY_UI)
        }

        if (mSensorCount == null) {
            // This will give a toast message to the user if there is no sensor in the device
            Toast.makeText(this, "No sensor detected on this device", Toast.LENGTH_SHORT).show()
        } else {
            // Rate suitable for the user interface
            mSensorManager?.registerListener(this, mSensorCount, SensorManager.SENSOR_DELAY_UI)
        }

        findViewById<Button>(R.id.btnreset).setOnClickListener {
            mStepCounterAndroid = 0f
            mInitialStepCount = 0f
            androidStep.setText("Steps Count:\n\n ${(mStepCounterAndroid - mInitialStepCount).toInt()}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onSensorChanged(e: SensorEvent?) {
        e?.apply {
            when (sensor.type) {
                Sensor.TYPE_STEP_COUNTER -> {
                    if (mInitialStepCount.toDouble() == 0.0) {
                        mInitialStepCount = e.values[0]
                    }
                    mStepCounterAndroid = e.values[0]
                }
                Sensor.TYPE_ACCELEROMETER -> {
                    mRawAccelValues[0] = e.values[0]
                    mRawAccelValues[1] = e.values[1]
                    mRawAccelValues[2] = e.values[2]

                    lastMag = sqrt(
                        mRawAccelValues[0].toDouble().pow(2.0) + mRawAccelValues[1].toDouble()
                            .pow(2.0) + mRawAccelValues[2].toDouble().pow(2.0)
                    )

                    //Source: https://github.com/jonfroehlich/CSE590Sp2018

                    //Source: https://github.com/jonfroehlich/CSE590Sp2018
                    for (i in 0..2) {
                        mRunningAccelTotal[i] =
                            mRunningAccelTotal[i] - mAccelValueHistory[i][mCurReadIndex]
                        mAccelValueHistory[i][mCurReadIndex] = mRawAccelValues[i]
                        mRunningAccelTotal[i] =
                            mRunningAccelTotal[i] + mAccelValueHistory[i][mCurReadIndex]
                        mCurAccelAvg[i] = mRunningAccelTotal[i] / SMOOTHING_WINDOW_SIZE
                    }
                    mCurReadIndex++
                    if (mCurReadIndex >= SMOOTHING_WINDOW_SIZE) {
                        mCurReadIndex = 0
                    }

                    avgMag = sqrt(
                        mCurAccelAvg[0].toDouble().pow(2.0) + mCurAccelAvg[1].toDouble()
                            .pow(2.0) + mCurAccelAvg[2].toDouble()
                            .pow(2.0)
                    )

                    netMag = lastMag - avgMag //removes gravity effect


                    //update graph data points

                    //update graph data points
                    mGraph1LastXValue += 1.0
                    //mSeries1!!.appendData(DataPoint(mGraph1LastXValue, lastMag), true, 60)

                    mGraph2LastXValue += 1.0
                    //mSeries2!!.appendData(DataPoint(mGraph2LastXValue, netMag), true, 60)
                }
            }

            androidStep.setText("Steps Count:\n\n ${(mStepCounterAndroid - mInitialStepCount).toInt()}")
        }

    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }
}