package com.denicks21.sensors

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.lang.Exception
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs

class Phonometer : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {
    private lateinit var soundValue: TextView
    private lateinit var soundInstrument: PhonometerActivity
    private val requestCodeAudioRecord = 1001
    private var handler: Handler? = null
    var timeStamp: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phonometer)

        findViewById<Button>(R.id.save_button).setOnClickListener {
            saveFile()
        }

        initViews()
        checkPermission()
    }

    private fun initViews() {
        soundValue = findViewById(R.id.soundValue)

    }

    private fun checkPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, getString(R.string.permission_denied),
                Toast.LENGTH_SHORT).show()

            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                requestCodeAudioRecord
            )
        } else {
            startListening()
        }
    }

    private fun startListening() {
        soundInstrument = PhonometerActivity()
        soundInstrument.start()
        handler = Handler()

        val runnable: Runnable = object : Runnable {
            override fun run() {
                val amplitude = soundInstrument.getAmplitude()
                soundValue.text = amplitude.toString()
                handler!!.postDelayed(this, 5)
            }
        }
        handler!!.postDelayed(runnable, 5)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == requestCodeAudioRecord){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, getString(R.string.permission_granted),
                    Toast.LENGTH_SHORT).show()

                startListening()
            }
        }
    }

    fun saveFile() {
        // Ottieni data e ora locale
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
        timeStamp = current.format(formatter)

        if (soundValue.length() == 0) {

            // Message to alert of missing input
            Toast.makeText(this, "Missing input", Toast.LENGTH_SHORT).show()

        } else {

            // Save file inside folder
            val output = File(Environment.getExternalStorageDirectory().toString() +
                    "/" + MainActivity.pathName +
                    "/" + "Phonometer_$timeStamp.txt")

            try {
                val fileout = FileOutputStream(output)
                val outputWriter = OutputStreamWriter(fileout)
                outputWriter.write(soundValue.text.toString())
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

    class PhonometerActivity {
        private lateinit var audioRecord : AudioRecord
        var sampleRateInHz = 8000
        var channelConfig = AudioFormat.CHANNEL_IN_MONO
        var audioFormat = AudioFormat.ENCODING_PCM_16BIT
        private var minBufferSize = 0
        private var isStarted = false

        @SuppressLint("MissingPermission")
        fun start() {
            minBufferSize = AudioRecord.getMinBufferSize(
                sampleRateInHz,
                channelConfig,
                audioFormat
            )

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRateInHz,
                channelConfig,
                audioFormat,
                minBufferSize
            )

            audioRecord.startRecording()
            isStarted = true
        }

        fun stop() {
            audioRecord.stop()
            isStarted = false
        }

        fun restart() {
            stop()
            start()
        }

        fun getIsStarted(): Boolean {
            return isStarted
        }

        fun getAmplitude(): Int{
            if(!isStarted){
                return 0
            }

            val buffer = ShortArray(minBufferSize)
            audioRecord.read(buffer, 0, minBufferSize)
            var maxBufferElement = 0
            for (bufferElement in buffer) {
                val bufferElementIntegerAbs = abs(bufferElement.toInt())
                if (maxBufferElement < bufferElementIntegerAbs) {
                    maxBufferElement = bufferElementIntegerAbs
                }
            }

            return maxBufferElement
        }
    }
}