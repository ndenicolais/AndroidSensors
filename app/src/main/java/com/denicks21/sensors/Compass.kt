package com.denicks21.sensors

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.lang.Exception
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Compass : AppCompatActivity(), SensorEventListener {
    private var image: ImageView? = null
    private var currentDegree = 0
    private var sensorManager: SensorManager? = null
    var compassValue: TextView? = null
    private var accelerometer: Sensor? = null
    private var magneticField: Sensor? = null
    private val gravity = FloatArray(3)
    private val prevAccReading = FloatArray(3)
    private val prevMagReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationVector = FloatArray(3)
    private var hasMagReading = false
    private var hasAccReading = false
    private var newHeading: String? = null
    var timeStamp: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compass)

        image = findViewById<View>(R.id.imageViewCompass) as ImageView
        compassValue = findViewById<View>(R.id.compassValue) as TextView
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magneticField = sensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        sensorManager!!.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        sensorManager!!.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_GAME)
        hasMagReading = false
        hasAccReading = false
        newHeading = ""

        findViewById<Button>(R.id.save_button).setOnClickListener {
            saveFile()
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event == null) {
            return
        }

        // ACCELEROMETER READING
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            if (!hasAccReading) {
                System.arraycopy(event.values, 0, prevAccReading, 0, event.values.size)
                hasAccReading = true
            } else {
                val valuesClone = event.values.clone()
                for (i in valuesClone.indices) {
                    prevAccReading[i] =
                        prevAccReading[i] + alpha * (valuesClone[i] - prevAccReading[i])
                }
            }

            // MAGNETOMETER READING
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            if (!hasMagReading) {
                System.arraycopy(event.values, 0, prevMagReading, 0, event.values.size)
                hasMagReading = true
            } else {
                val valuesClone = event.values.clone()
                for (i in valuesClone.indices) {
                    prevMagReading[i] =
                        prevMagReading[i] + alpha * (valuesClone[i] - prevMagReading[i])
                }
            }
        }
        if (hasMagReading && hasAccReading) {
            SensorManager.getRotationMatrix(rotationMatrix, null, prevAccReading, prevMagReading)
            val orientations = SensorManager.getOrientation(rotationMatrix, orientationVector)
            val degree = (Math.toDegrees(orientations[0].toDouble()) + 360).toInt() % 360
            updateHeadingText(degree)
            updateAnimation(degree)
            currentDegree = -degree
        }
    }

    private fun updateHeadingText(degree: Int) {
        updateDirection(degree)
        val newDegStr = "$degree $newHeading"
        compassValue!!.text = newDegStr
    }

    private fun updateAnimation(degree: Int) {
        val ra = RotateAnimation(
            currentDegree.toFloat(),
            (-degree).toFloat(),
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f)
        val degDiff = currentDegree - degree
        val t = Math.abs(degDiff * 100)
        ra.duration = t.toLong()
        ra.fillAfter = true

        // Start the animation
        image!!.startAnimation(ra)
    }

    private fun updateBackground() {}

    // returns the current direction
    private fun updateDirection(degree: Int) {
        if (degree >= 350 || degree <= 10) {
            newHeading = "N"
        } else if (degree < 350 && degree > 280) {
            newHeading = "NW"
        } else if (degree <= 280 && degree > 260) {
            newHeading = "W"
        } else if (degree <= 260 && degree > 190) {
            newHeading = "SW"
        } else if (degree <= 190 && degree > 170) {
            newHeading = "S"
        } else if (degree <= 170 && degree > 100) {
            newHeading = "SE"
        } else if (degree <= 100 && degree > 80) {
            newHeading = "E"
        } else if (degree <= 80 && degree > 10) {
            newHeading = "NE"
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager!!.unregisterListener(this)
        sensorManager!!.unregisterListener(this, accelerometer)
        sensorManager!!.unregisterListener(this, magneticField)
    }

    override fun onResume() {
        super.onResume()
        sensorManager!!.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        sensorManager!!.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onAccuracyChanged(sensor: Sensor, i: Int) {}

    companion object {
        private const val alpha = 0.10f
    }

    fun saveFile() {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
        timeStamp = current.format(formatter)

        if (compassValue?.length() == 0) {

            // Message to alert of missing input
            Toast.makeText(this, "Missing input", Toast.LENGTH_SHORT).show()

        } else {

            // Save file inside folder
            val output = File(Environment.getExternalStorageDirectory().toString() +
                    "/" + MainActivity.pathName +
                    "/" + "Compass_$timeStamp.txt")

            try {
                val fileout = FileOutputStream(output)
                val outputWriter = OutputStreamWriter(fileout)
                outputWriter.write(compassValue!!.text.toString())
                outputWriter.close()

                // Message to confirm of save
                val savedUri = Uri.fromFile(output)
                Toast.makeText(this, "Saved: "+savedUri!!.lastPathSegment,
                    Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}