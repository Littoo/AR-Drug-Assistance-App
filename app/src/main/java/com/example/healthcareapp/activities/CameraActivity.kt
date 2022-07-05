package com.example.healthcareapp.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.healthcareapp.Constants
import com.example.healthcareapp.databinding.ActivityCameraBinding
import com.google.common.util.concurrent.ListenableFuture

import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.detector.Detection
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import java.io.File
import java.io.IOException
import java.util.concurrent.Executors

private const val FILE_NAME = "photo.jpg"
private const val REQUEST_CODE = 42
private lateinit var photoFile: File
private const val FULL_ASPECT_RATIO = AspectRatio.RATIO_16_9

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding

    private val TAG = "Object Detection"
    private lateinit var file: File
    private var imageCapture: ImageCapture? = null
    private var detected_drug: String = "None"

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val takenImage = BitmapFactory.decodeFile(photoFile.absolutePath)
            binding.imagePredicted.setImageBitmap(takenImage)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        if (allPermissionGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, Constants.REQUIRED_PERMISSIONS,
                Constants.REQUEST_CODE_PERMISSIONS
            )
        }

        binding.cameraCaptureButton.setOnClickListener {
            Constants.startTime = SystemClock.elapsedRealtime()
            takePhoto()
        }

        binding.reCaptureButton.setOnClickListener {
            Constants.startTime = SystemClock.elapsedRealtime()
            file.delete()

            binding.imageFrame.visibility = View.GONE
            binding.imagePredicted.visibility = View.GONE
            binding.reCaptureButton.visibility = View.GONE
            binding.viewFinder.visibility = View.VISIBLE
            binding.cameraCaptureButton.visibility = View.VISIBLE
            binding.detectObject.visibility = View.GONE
            detected_drug = "None"
            binding.objectName.text = "None"
            binding.objectName.visibility = View.GONE
            binding.proceedARButton.visibility = View.GONE

        }

        binding.proceedARButton.setOnClickListener {
            Constants.startTime = SystemClock.elapsedRealtime()
            val  ARActivityIntent = Intent(this, ARActivity::class.java)
            ARActivityIntent.putExtra("predictedDrug", detected_drug)
            ARActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(ARActivityIntent)
            finish()
        }
        reportFullyDrawn()
    }

    override fun onBackPressed() {
        super.onBackPressed()

        val  HomeActivityIntent = Intent(this, HomeActivity::class.java)
        HomeActivityIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(HomeActivityIntent)
        finish()

    }

    private fun takePhoto() {

        val dialog = AlertDialog.Builder(this)
            .setMessage("Saving picture...")
            .setCancelable(false)
            .show()

        file = File(
            getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "JPEG_${System.currentTimeMillis().toString()}" + ".jpg"
        )
        file.createNewFile()

        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()

        imageCapture?.takePicture(outputFileOptions,
            Executors.newSingleThreadExecutor(),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    runOnUiThread {
                        val bitmap: Bitmap = rotateImage(file!!.absolutePath)

                        Constants.startTime - SystemClock.elapsedRealtime()
                        val width = resources.displayMetrics.widthPixels
                        val height = resources.displayMetrics.heightPixels
                        binding.imageFrame.visibility = View.VISIBLE
                        binding.imagePredicted.layoutParams.height = (height * 0.6).toInt()
                        binding.imagePredicted.layoutParams.width = (width * 0.8).toInt()
                        binding.imagePredicted.setImageBitmap(bitmap)
                        binding.imagePredicted.visibility = View.VISIBLE
                        binding.reCaptureButton.visibility = View.VISIBLE
                        binding.viewFinder.visibility = View.GONE
                        binding.cameraCaptureButton.visibility = View.GONE

                        dialog.dismiss()
                        runObjectDetection(bitmap)
                        binding.detectObject.visibility = View.VISIBLE
                        binding.objectName.text = detected_drug
                        binding.objectName.visibility = View.VISIBLE
                        if (detected_drug != "None") {
                            binding.proceedARButton.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    runOnUiThread {
                        dialog.dismiss()
                        Toast.makeText(this@CameraActivity, exception.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            })
    }

    private fun startCamera() {


        val cameraProviderFuture = ProcessCameraProvider
            .getInstance(this)

        cameraProviderFuture.addListener({

            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .setTargetResolution(Size(720, 1280))
                .build()
                .also { mPreview ->

                    mPreview.setSurfaceProvider(
                        binding.viewFinder.surfaceProvider
                    )
                }

            imageCapture = ImageCapture.Builder()
                .setTargetResolution(Size(720 , 1280))
                .build()

            val imageAnalysis = ImageAnalysis
                .Builder()
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .setTargetResolution(Size(720,1080))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    this, cameraSelector, imageAnalysis,
                    preview, imageCapture
                )
            } catch(e: Exception) {
                Log.d(Constants.TAG, "startCamera Fail: ", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.REQUEST_CODE_PERMISSIONS) {
            if (allPermissionGranted()) {

            } else {
                Toast.makeText(this,
                    "Permissions Not Granted by the user",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun allPermissionGranted() =
        Constants.REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                baseContext, it
            ) == PackageManager.PERMISSION_GRANTED
        }


    @Throws(IOException::class)
    fun rotateImage(path: String): Bitmap {
        val bitmap = BitmapFactory.decodeFile(path)
        var rotate = 0
        val exif: ExifInterface
        exif = ExifInterface(path)
        val orientation: Int = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270
            ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180
            ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90
        }
        val matrix = Matrix()
        matrix.postRotate(rotate.toFloat())
        return Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width,
            bitmap.height, matrix, true
        )
    }

    private fun runObjectDetection(bitmap: Bitmap) {
        //TODO: Add object detection code here
        val image = TensorImage.fromBitmap(bitmap)

        val options = ObjectDetector.ObjectDetectorOptions.builder()
            .setMaxResults(5)
            .setScoreThreshold(0.487f)
            .build()
        val detector = ObjectDetector.createFromFileAndOptions(
            this, // the application context
            "drug_detection_v3.tflite", // must be same as the filename in assets folder
            options
        )
        val results = detector.detect(image)
        debugPrint(results)
        reformatDrugLabel()

    }

    private fun debugPrint(results : List<Detection>) {
        if (results.isEmpty()){
            Toast.makeText(
                this,
                "No object detected. Try again.",
                Toast.LENGTH_SHORT
            ).show()
        }
        for ((i, obj) in results.withIndex()) {
            val box = obj.boundingBox

            Log.d(TAG, "Detected object: ${i} ")
            Log.d(TAG, "  boundingBox: (${box.left}, ${box.top}) - (${box.right},${box.bottom})")

            for ((j, category) in obj.categories.withIndex()) {
                Log.d(TAG, "Label $j: ${category.label}")
                detected_drug = category.label.trim()

                val confidence: Int = category.score.times(100).toInt()
                Log.d(TAG, "Confidence: ${confidence}%")
                Toast.makeText(
                    this,
                    "${category.label} is detected with confidence of ${confidence}% ",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun reformatDrugLabel(){
        while (detected_drug.contains("_")) {
            detected_drug = detected_drug.replace("_", " ")
        }
    }

}
