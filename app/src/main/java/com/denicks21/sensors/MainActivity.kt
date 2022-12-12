package com.denicks21.sensors

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Switch to "Compass"
        val btnCompass = findViewById<Button>(R.id.btnCompass)
        btnCompass.setOnClickListener {
            startActivity(Intent(this, Compass::class.java))
        }

        // Switch to "Luxmeter"
        val btnLuxmeter = findViewById<Button>(R.id.btnLuxmeter)
        btnLuxmeter.setOnClickListener {
            startActivity(Intent(this, Luxmeter::class.java))
        }

        // Switch to "Accelerometer"
        val btnAccelerometer = findViewById<Button>(R.id.btnAccelerometer)
        btnAccelerometer.setOnClickListener {
            startActivity(Intent(this, Accelerometer::class.java))
        }

        // Switch to "Phonometer"
        val btnPhonometer = findViewById<Button>(R.id.btnPhonometer)
        btnPhonometer.setOnClickListener {
            startActivity(Intent(this, Phonometer::class.java))
        }

        // Switch to "Measure"
        val btnMeasure = findViewById<Button>(R.id.btnMeasure)
        btnMeasure.setOnClickListener {
            startActivity(Intent(this, Measure::class.java))
        }

        if (checkPermission()) {
            createFolder()
        } else {
            requestPermission()
        }
    }

    // Create folder
    private fun createFolder() {
        pathName = "Sensors"
        val filePathName = File(Environment.getExternalStorageDirectory().toString() +
                "/" + pathName)
        filePathName.mkdir()
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android is 11 (R) or above
            try {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                storageActivityResultLauncher.launch(intent)
            } catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                storageActivityResultLauncher.launch(intent)
            }
        } else {
            // Android is below 11 (R)
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        }
    }

    private val storageActivityResultLauncher = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult? ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android is 11 (R) or above
            if (Environment.isExternalStorageManager()) {
                createFolder()
            } else {
                // Android is below 11 (R)

                // Alert message to accept permissions
                Toast.makeText(this, "Accept permissions to continue", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android is 11 (R) or above
            Environment.isExternalStorageManager()
        } else {
            // Android is below 11 (R)
            val write = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val read = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
            write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.size > 0) {
                val write = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val read = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (write && read) {
                    createFolder()
                } else {

                    // Alert message of permissions not granted
                    Toast.makeText(this, "Permissions not granted", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    companion object {
        var pathName : String? = null
        private const val STORAGE_PERMISSION_CODE = 100
        private const val TAG = "PERMISSION_TAG"
    }
}