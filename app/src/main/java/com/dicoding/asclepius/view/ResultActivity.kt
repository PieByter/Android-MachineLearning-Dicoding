package com.dicoding.asclepius.view

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.asclepius.R
import com.dicoding.asclepius.database.HistoryDatabase
import com.dicoding.asclepius.databinding.ActivityResultBinding
import com.dicoding.asclepius.entity.HistoryResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.sql.SQLException

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_scan -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }

                R.id.navigation_news -> {
                    startActivity(Intent(this, NewsActivity::class.java))
                    true
                }

                R.id.navigation_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    true
                }

                else -> false
            }
        }

        val predictedClassLabel = intent.getStringExtra(PREDICTED_CLASS_LABEL)
        val confidenceScore = intent.getFloatExtra(CONFIDENCE_SCORE, 0f)
        val imageUriString = intent.getStringExtra(IMAGE_URI)

        val confidencePercentage = (confidenceScore * 100).toInt()

        val resultText = if (predictedClassLabel == "Cancer" && confidenceScore != -1f) {
            "Cancer $confidencePercentage%"
        } else {
            "Not Cancer $confidencePercentage%"
        }


        binding.resultText.text = resultText

        binding.saveResult.setOnClickListener {
            val result = HistoryResult(resultText = resultText, imageUri = imageUriString ?: "")
            saveResultToDatabase(result)
        }

        imageUriString?.let { uriString ->
            val imageUri = Uri.parse(uriString)
            try {
                contentResolver.openInputStream(imageUri)?.use { inputStream ->
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    binding.resultImage.setImageBitmap(bitmap)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun saveResultToDatabase(result: HistoryResult) {
        val db = HistoryDatabase.getDatabase(applicationContext)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                db.historyResultDao().insertResult(result)
                runOnUiThread { showToast("Result saved successfully") }
            } catch (sqlException: SQLException) {
                runOnUiThread { showToast("Failed to save result: ${sqlException.message}") }
            } catch (e: Exception) {
                runOnUiThread { showToast("Failed to save result") }
            }
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
