package com.dicoding.asclepius.view

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import com.dicoding.asclepius.helper.getImageUri
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var currentImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_scan -> {
                    true
                }

                R.id.navigation_news -> {
                    val intent = Intent(this, NewsActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.navigation_history -> {
                    val intent = Intent(this, HistoryActivity::class.java)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }

        binding.galleryButton.setOnClickListener {
            startGallery()
        }

        binding.cameraButton.setOnClickListener {
            startCamera()
        }
        binding.analyzeButton.setOnClickListener {
            currentImageUri?.let { uri ->
                analyzeImage(uri)
            } ?: showToast(getString(R.string.no_image_selected))
        }
    }

    private fun showImage() {
        currentImageUri?.let { uri ->
            try {
                val inputStream = contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    binding.previewImageView.setImageBitmap(bitmap)
                } else {
                    showToast("Failed to load image")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("Failed to load image")
            }
        }
    }

    private fun startGallery() {
        launcherGallery.launch("image/*")
    }

    private fun startCamera() {
        currentImageUri = getImageUri(this)
        launcherIntentCamera.launch(currentImageUri)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            currentImageUri?.let { uri ->
                startUCrop(uri)
            }
        }
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            startUCrop(uri)
        } else {
            Toast.makeText(this, "No media selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startUCrop(uri: Uri) {
        val destinationUri = Uri.fromFile(createImageFile())
        UCrop.of(uri, destinationUri)
            .withAspectRatio(1F, 1F)
            .withMaxResultSize(2000, 2000)
            .start(this)
    }

    private fun createImageFile(): File {
        val timeStamp =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir("images")
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            val resultUri = UCrop.getOutput(data!!)
            resultUri?.let { uri ->
                currentImageUri = uri
                showImage()
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            cropError?.let {
                showToast("Crop error: ${it.localizedMessage}")
            } ?: run {
                showToast("Unknown crop error")
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun analyzeImage(uri: Uri) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    convertUriToBitmap(uri)
                } else {
                    MediaStore.Images.Media.getBitmap(contentResolver, uri)
                }
                bitmap?.let { bmp ->
                    val tensorImage = TensorImage.fromBitmap(bmp)
                    val imageClassifierHelper = ImageClassifierHelper(
                        context = this@MainActivity,
                        modelName = "cancer_classification.tflite",
                        classifierListener = object : ImageClassifierHelper.ClassifierListener {
                            override fun onError(error: String) {
                                showToast(error)
                            }

                            override fun onResults(
                                results: List<Classifications>?,
                                inferenceTime: Long,
                            ) {
                                results?.let {
                                    GlobalScope.launch(Dispatchers.Main) {
                                        moveToResult(it)
                                    }
                                } ?: showToast("No results found")
                            }
                        })
                    imageClassifierHelper.classifyStaticImage(tensorImage)
                } ?: showToast("Failed to analyze image")
            } catch (e: IOException) {
                e.printStackTrace()
                showToast(getString(R.string.failed_to_analyze_image))
            }
        }
    }

    private fun convertUriToBitmap(uri: Uri): Bitmap? {
        return contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    }

    private fun moveToResult(results: List<Classifications>) {
        if (results.isNotEmpty()) {
            val latestResult = results.last()
            val predictedClassLabel =
                if (latestResult.categories.isNotEmpty() && latestResult.categories[0].label != null) {
                    latestResult.categories[0].label
                } else {
                    "Unknown"
                }
            val confidenceScore = latestResult.categories[0].score

            val intent = Intent(this, ResultActivity::class.java)
            intent.putExtra(PREDICTED_CLASS_LABEL, predictedClassLabel)
            intent.putExtra(CONFIDENCE_SCORE, confidenceScore)

            currentImageUri?.let {
                intent.putExtra(IMAGE_URI, it.toString())
            }

            startActivity(intent)
            showToast("Image analyzed successfully")
        } else {
            showToast("No predictions available")
        }
    }

    companion object {
        const val PREDICTED_CLASS_LABEL = "PREDICTED_CLASS_LABEL"
        const val CONFIDENCE_SCORE = "CONFIDENCE_SCORE"
        const val IMAGE_URI = "IMAGE_URI"
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
