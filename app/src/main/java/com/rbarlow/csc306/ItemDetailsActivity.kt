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
import com.google.android.material.progressindicator.LinearProgressIndicator
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.View
import android.widget.ImageButton
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
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
        val currentUser = FirebaseAuth.getInstance().currentUser


        // Observe the LiveData using the lifecycle of this activity
        val descriptionTextView = findViewById<TextView>(R.id.description)

        val itemId = intent.getStringExtra("id")


        if (itemId != null) {
            firebaseRepository.getItem(itemId).observe(this) { item: Item ->

                //set title
                val titleTextView = findViewById<TextView>(R.id.title)
                titleTextView.text = item.name


                //set toolbar title
                val toolBar = findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar)
                toolBar.title = item.name
                toolBar.setExpandedTitleColor(ContextCompat.getColor(this, android.R.color.transparent))

                //set description
                descriptionTextView.text = item.description

                // Update the upload_info TextView
                val uploadInfoTextView = findViewById<TextView>(R.id.upload_info)
                val uploadDate = Date(item.addedOn)
                val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                uploadInfoTextView.text ="Uploaded by ${item.addedBy} on ${formatter.format(uploadDate)}"

                //set image
                val imageView = findViewById<ImageView>(R.id.image)
                Glide.with(this)
                    .load(item.image)
                    .override(1024)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView)

                // Update the bookmark icon based on the current user's bookmark status
                if (currentUser != null) {
                    firebaseRepository.isItemBookmarked(currentUser, item.id)
                        .observe(this) { isBookmarked ->
                            updateBookmarkIcon(isBookmarked) }
                }

                //add a view
                firebaseRepository.addViewToItem(item.id)
            }
        }

        //set toolbar
        val toolBar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolBar.setNavigationOnClickListener {
            finish()
        }

        toolBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.bookmarksButton -> {
                    if (currentUser != null && itemId != null) {
                        firebaseRepository.getItem(itemId).observe(this) { item: Item ->
                            // Toggle the bookmark for the current user
                            firebaseRepository.bookmarkItem(currentUser, item.id)

                            // Update the bookmark icon
                            firebaseRepository.isItemBookmarked(currentUser, item.id).observe(this) { isBookmarked ->
                                updateBookmarkIcon(!isBookmarked)
                            }
                        }
                    }
                    true
                }
                else -> false
            }
        }

        // Set the OnClickListener to toggle the button appearance and play/stop the audio
        val audioButton = findViewById<FloatingActionButton>(R.id.fab)

        // Get a reference to the progress bar
        progressBar = findViewById(R.id.progress_indicator)

        // Set the UtteranceProgressListener to update the progress bar as the text is being spoken
        textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) {
                runOnUiThread {
                    progressBar.progress = 0
                }
            }

            // Update the progress bar as the text is being spoken
            override fun onDone(utteranceId: String) {
                runOnUiThread {
                    progressBar.progress = 0
                    audioButton.setImageDrawable(ContextCompat.getDrawable(this@ItemDetailsActivity, R.drawable.ic_play))
                    isPlaying = false
                }
            }

            override fun onError(utteranceId: String) {
                Log.e("TTS", "Error while trying to speak the description")
            }
        })

        // Set the OnClickListener to toggle the button appearance and play/stop the audio
        audioButton.setOnClickListener {
            if (isPlaying) {
                // Stop playing the audio
                textToSpeech.stop()
                progressBar.progress = 0
                isPlaying = false
                audioButton.setImageDrawable(ContextCompat.getDrawable(this@ItemDetailsActivity, R.drawable.ic_play))
            } else {
                // Start playing the audio
                val description = descriptionTextView.text.toString()
                textToSpeech.speak(description, TextToSpeech.QUEUE_FLUSH, null, utteranceId)

                isPlaying = true
                audioButton.setImageDrawable(ContextCompat.getDrawable(this@ItemDetailsActivity, R.drawable.ic_pause))
                updateProgressBar()
            }
        }
    }

    private fun updateProgressBar() {
        val wordsPerMinute = 160
        val words = findViewById<TextView>(R.id.description).text.split("\\s+".toRegex()).size
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

    private fun updateBookmarkIcon(isBookmarked: Boolean) {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        val bookmarksButton = toolbar.menu.findItem(R.id.bookmarksButton)
        if (isBookmarked) {
            bookmarksButton.setIcon(R.drawable.ic_heart_filled_24px)
        } else {
            bookmarksButton.setIcon(R.drawable.ic_heart_24px)
        }
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
