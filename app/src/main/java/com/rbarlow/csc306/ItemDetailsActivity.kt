package com.rbarlow.csc306

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.text.SimpleDateFormat
import java.util.*


class ItemDetailsActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private var isPlaying = false
    private lateinit var progressBar: LinearProgressIndicator
    private lateinit var textToSpeech: TextToSpeech
    private val utteranceId = "descriptionUtterance"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item)

        textToSpeech = TextToSpeech(this, this)

        // Create an instance of FirebaseRepository
        val firebaseRepository = FirebaseRepository()

        // Observe the LiveData using the lifecycle of this activity
        val descriptionTextView = findViewById<TextView>(R.id.item_description)

        val itemName = intent.getStringExtra("itemName")


        if (itemName != null) {
            firebaseRepository.getItem(itemName).observe(this) { item: Item ->
                // Update the UI
                val titleTextView = findViewById<TextView>(R.id.item_title)
                titleTextView.text = item.name

                descriptionTextView.text = item.description

                // Update the upload_info TextView
                val uploadInfoTextView = findViewById<TextView>(R.id.upload_info)
                val uploadDate = Date(item.addedOn) // Assuming item has uploadTimestamp field
                val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                uploadInfoTextView.text = "Uploaded by ${item.addedBy} on ${formatter.format(uploadDate)}" // Assuming item has uploader field


                val imageView = findViewById<ImageView>(R.id.item_image)
                Glide.with(this)
                    .load(item.image)
                    .override(1024)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView)
            }
        }

        val toolBar = findViewById<MaterialToolbar>(R.id.toolBar)

        toolBar.setNavigationOnClickListener {
            finish()
        }
        // Set the OnClickListener to toggle the button appearance and play/stop the audio
        val audioButton = findViewById<MaterialButton>(R.id.audioImage)

        // Get a reference to the progress bar
        progressBar = findViewById(R.id.progress_indicator)

        textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) {
                // Not used in this case
            }

            override fun onDone(utteranceId: String) {
                runOnUiThread {
                    progressBar.progress = 0
                    audioButton.icon =
                        ContextCompat.getDrawable(this@ItemDetailsActivity, R.drawable.ic_play)
                    isPlaying = false
                }
            }

            override fun onError(utteranceId: String) {
                // Handle errors if needed
            }
        })




        audioButton.setOnClickListener {
            if (isPlaying) {
                // Stop playing the audio
                textToSpeech.stop()
                progressBar.progress = 0
                isPlaying = false
                audioButton.icon =
                    ContextCompat.getDrawable(this@ItemDetailsActivity, R.drawable.ic_play)
            } else {
                // Start playing the audio
                val description = descriptionTextView.text.toString()
                textToSpeech.speak(description, TextToSpeech.QUEUE_FLUSH, null, utteranceId)

                isPlaying = true
                audioButton.icon =
                    ContextCompat.getDrawable(this@ItemDetailsActivity, R.drawable.ic_pause)
                updateProgressBar()
            }
        }
    }

    private fun updateProgressBar() {
        val wordsPerMinute = 160
        val words = findViewById<TextView>(R.id.item_description).text.split("\\s+".toRegex()).size
        val estimatedDuration = words * 60 * 1000 / wordsPerMinute

        progressBar.max = estimatedDuration
        val progressUpdateInterval = 500 // milliseconds

        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                if (isPlaying) {
                    progressBar.progress += progressUpdateInterval
                    handler.postDelayed(this, progressUpdateInterval.toLong())
                }
            }
        }
        handler.post(runnable)
    }
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech.setLanguage(Locale.getDefault())
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language not supported")
            } else {
                // The TTS engine is ready
                updateProgressBar()
            }
        } else {
            Log.e("TTS", "Initialization failed")
        }
    }
    override fun onStop() {
        super.onStop()
        if (textToSpeech.isSpeaking) {
            textToSpeech.stop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (textToSpeech != null) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }
}
