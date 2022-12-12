package com.denicks21.sensors

import android.app.Activity
import android.app.ActivityManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Pose
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.lang.Exception
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ARMeasurements : AppCompatActivity() {
    private var upDistance = 0f
    private var arFragment: ArFragment? = null
    private var andyRenderable: ModelRenderable? = null
    private var myanchornode: AnchorNode? = null
    private val form_numbers = DecimalFormat("#0.00 m")
    private var anchor1: Anchor? = null
    private var anchor2: Anchor? = null
    private var myhit: HitResult? = null
    private var text: TextView? = null
    private var sk_height_control: SeekBar? = null
    private var btn_save: Button? = null
    private var btn_width: Button? = null
    private var btn_height: Button? = null
    var anchorNodes: MutableList<AnchorNode> = ArrayList<AnchorNode>()
    private var measure_height = false
    private val arl_saved = ArrayList<String>()
    private var fl_measurement = 0.0f
    private val message: String? = null
    var timeStamp: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!checkIsSupportedDeviceOrFinish(this)) {
            return
        }
        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }
        setContentView(R.layout.activity_ar_measurements)
        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment?
        text = findViewById<View>(R.id.text) as TextView
        sk_height_control = findViewById<View>(R.id.sk_height_control) as SeekBar
        btn_height = findViewById<View>(R.id.btn_height) as Button
        btn_save = findViewById<View>(R.id.btn_save) as Button
        btn_width = findViewById<View>(R.id.btn_width) as Button
        sk_height_control!!.isEnabled = false

        btn_width!!.setOnClickListener {
            resetLayout()
            measure_height = false
            text!!.text = "Click the extremes you want to measure"
        }

        btn_height!!.setOnClickListener {
            resetLayout()
            measure_height = true
            text!!.text = "Click the base of the object you want to measure"
        }

        btn_save!!.setOnClickListener {
            if (fl_measurement != 0.0f) saveDialog() else Toast.makeText(this@ARMeasurements,
                "Make a measurement before saving",
                Toast.LENGTH_SHORT).show()
        }

        sk_height_control!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                upDistance = progress.toFloat()
                fl_measurement = progress / 100f
                text!!.text = "Height: " + form_numbers.format(fl_measurement.toDouble())
                myanchornode!!.setLocalScale(Vector3(1f, progress / 10f, 1f))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        ModelRenderable.builder()
            .setSource(this, R.raw.cubito3)
            .build()
            .thenAccept { renderable -> andyRenderable = renderable }
            .exceptionally { throwable ->
                val toast =
                    Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
                null
            }

        arFragment!!.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane?, motionEvent: MotionEvent? ->
            if (andyRenderable == null) {
                return@setOnTapArPlaneListener
            }
            myhit = hitResult

            val anchor: Anchor = hitResult.createAnchor()
            val anchorNode = AnchorNode(anchor)
            anchorNode.setParent(arFragment!!.getArSceneView().getScene())
            if (!measure_height) {
                if (anchor2 != null) {
                    emptyAnchors()
                }
                if (anchor1 == null) {
                    anchor1 = anchor
                } else {
                    anchor2 = anchor
                    fl_measurement = getMetersBetweenAnchors(anchor1, anchor2)
                    text!!.text = "Width: " + form_numbers.format(fl_measurement.toDouble())
                }
            } else {
                emptyAnchors()
                anchor1 = anchor
                text!!.text = "Move the slider till the cube reaches the upper base"
                sk_height_control!!.isEnabled = true
            }
            myanchornode = anchorNode
            anchorNodes.add(anchorNode)

            val andy = TransformableNode(arFragment!!.getTransformationSystem())
            andy.setParent(anchorNode)
            andy.setRenderable(andyRenderable)
            andy.select()
            andy.getScaleController().setEnabled(false)
        }
    }

    private fun ascend(an: AnchorNode, up: Float) {
        val anchor: Anchor = myhit!!.getTrackable().createAnchor(
            myhit!!.getHitPose().compose(Pose.makeTranslation(0F, up / 100f, 0F)))
        an.setAnchor(anchor)
    }

    private fun getMetersBetweenAnchors(anchor1: Anchor?, anchor2: Anchor?): Float {
        val distance_vector: FloatArray = anchor1!!.getPose().inverse()
            .compose(anchor2!!.getPose()).getTranslation()
        var totalDistanceSquared = 0f
        for (i in 0..2) totalDistanceSquared += distance_vector[i] * distance_vector[i]
        return Math.sqrt(totalDistanceSquared.toDouble()).toFloat()
    }

    private fun checkIsSupportedDeviceOrFinish(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT < VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later")
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG)
                .show()
            activity.finish()
            return false
        }
        val openGlVersionString = (activity.getSystemService(ACTIVITY_SERVICE) as ActivityManager)
            .deviceConfigurationInfo
            .glEsVersion
        if (openGlVersionString.toDouble() < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later")
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                .show()
            activity.finish()
            return false
        }
        return true
    }

    private fun saveDialog() {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
        timeStamp = current.format(formatter)

        /*
        val mBuilder = AlertDialog.Builder(this@Measure)
        val mView: View = layoutInflater.inflate(R.layout.dialog_save, null)
        val et_measure = mView.findViewById<View>(R.id.et_measure) as EditText
        mBuilder.setTitle("Measurement title")
        mBuilder.setPositiveButton("Ok"
        ) { dialogInterface, i ->
            if (et_measure.length() != 0) {
                arl_saved.add(et_measure.text.toString() + ": " + form_numbers.format(fl_measurement.toDouble()))
                dialogInterface.dismiss()
            } else Toast.makeText(this@Measure, "Title can't be empty", Toast.LENGTH_SHORT)
                .show()
        }
        mBuilder.setView(mView)
        val dialog = mBuilder.create()
        dialog.show()
         */

        val output = File(Environment.getExternalStorageDirectory().toString() +
                "/" + MainActivity.pathName +
                "/" + "Measure_$timeStamp.txt")

        try {
            val fileout = FileOutputStream(output)
            val outputWriter = OutputStreamWriter(fileout)
            outputWriter.write(text!!.text.toString())
            outputWriter.close()

            // Message to confirm of save
            val savedUri = Uri.fromFile(output)
            Toast.makeText(this, "Saved: "+savedUri!!.lastPathSegment,
                Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun resetLayout() {
        sk_height_control!!.progress = 10
        sk_height_control!!.isEnabled = false
        measure_height = false
        emptyAnchors()
    }

    private fun emptyAnchors() {
        anchor1 = null
        anchor2 = null
        for (n in anchorNodes) {
            arFragment!!.getArSceneView().getScene().removeChild(n)
            n.getAnchor()!!.detach()
            n.setParent(null)
        }
    }

    companion object {
        private val TAG = ARMeasurements::class.java.simpleName
        private const val MIN_OPENGL_VERSION = 3.0
    }
}