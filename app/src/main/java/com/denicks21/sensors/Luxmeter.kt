package com.denicks21.sensors

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.lang.Exception
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Luxmeter : AppCompatActivity() {
    var luxValue: TextView? = null
    var sensorManager: SensorManager? = null
    var luxSensor: Sensor? = null
    var timeStamp: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_luxmeter)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        luxSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_LIGHT)
        luxValue = findViewById(R.id.lux_value)

        if (luxSensor != null) {
            sensorManager!!.registerListener(listenLux, luxSensor,
                SensorManager.SENSOR_DELAY_FASTEST)
        } else {
            Toast.makeText(this,
                "Nessun sensore di rilevamento", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.save_button).setOnClickListener {
            saveFile()
        }
    }

    private var listenLux: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(sensorEvent: SensorEvent) {
            luxValue!!.text = sensorEvent.values[0].toInt().toString()
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    override fun onPause() {
        super.onPause()
        sensorManager!!.unregisterListener(listenLux, luxSensor)
    }

    fun saveFile() {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
        timeStamp = current.format(formatter)

        if (luxValue?.length() == 0) {

            // Message to alert of missing input
            Toast.makeText(this, "Missing input", Toast.LENGTH_SHORT).show()

        } else {

            // Save file inside folder
            val output = File(Environment.getExternalStorageDirectory().toString() +
                    "/" + MainActivity.pathName +
                    "/" + "Luxmeter_$timeStamp.txt")

            try {
                val fileout = FileOutputStream(output)
                val outputWriter = OutputStreamWriter(fileout)
                outputWriter.write(luxValue!!.text.toString())
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