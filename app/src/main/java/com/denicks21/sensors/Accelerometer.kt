package com.denicks21.sensors

import android.app.Activity
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Html
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.lang.Exception
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Accelerometer : Activity(), SensorEventListener {
    private var sensorManager: SensorManager? = null
    var x: TextView? = null
    var y: TextView? = null
    var z: TextView? = null
    var sx: String? = null
    var sy: String? = null
    var sz: String? = null
    var timeStamp: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accelerometer)

        findViewById<Button>(R.id.save_button).setOnClickListener {
            saveFile()
        }

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        x = findViewById<View>(R.id.textViewX) as TextView
        y = findViewById<View>(R.id.textViewY) as TextView
        z = findViewById<View>(R.id.textViewZ) as TextView
        sensorManager!!.registerListener(this,
            sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onAccuracyChanged(arg0: Sensor, arg1: Int) {
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val xVal = event.values[0]
            val yVal = event.values[1]
            val zVal = event.values[2]
            sx = "X Value : <font color = '#800080'> $xVal</font>"
            sy = "Y Value : <font color = '#800080'> $yVal</font>"
            sz = "Z Value : <font color = '#800080'> $zVal</font>"
            x!!.text = Html.fromHtml(sx)
            y!!.text = Html.fromHtml(sy)
            z!!.text = Html.fromHtml(sz)
        }
    }

    fun saveFile() {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
        timeStamp = current.format(formatter)

        if ((x!!.length() == 0) && (y!!.length() == 0) && (z!!.length() == 0)) {

            // Message to alert of missing input
            Toast.makeText(this, "Missing input", Toast.LENGTH_SHORT).show()

        } else {

            // Save file inside folder
            val output = File(Environment.getExternalStorageDirectory().toString() +
                    "/" + MainActivity.pathName +
                    "/" + "Accelerometro_$timeStamp.txt")

            try {
                val fileout = FileOutputStream(output)
                val outputWriter = OutputStreamWriter(fileout)
                outputWriter.write(
                    x!!.text.toString() + "\n" +
                            (y!!.text.toString()) + "\n" +
                            (z!!.text.toString()))
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