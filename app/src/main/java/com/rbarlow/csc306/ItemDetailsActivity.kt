package com.rbarlow.csc306

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*


class ItemDetailsActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private var isPlaying = false
    private lateinit var progressBar: LinearProgressIndicator
    private lateinit var textToSpeech: TextToSpeech
    private val utteranceId = "descriptionUtterance"
    private lateinit var editFab: FloatingActionButton
    private val firebaseRepository = FirebaseRepository()

    private val editItemLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            // If the edit was a success, reload the item details from the database to show the updated information
            val itemId = intent.getStringExtra("id")
            if (itemId != null) {
                firebaseRepository.getItem(itemId).observe(this) { item: Item ->
                    // Set title
                    val titleTextView = findViewById<TextView>(R.id.title)
                    titleTextView.text = item.name

                    // Set description
                    val descriptionTextView = findViewById<TextView>(R.id.description)
                    descriptionTextView.text = item.description

                    // Set image
                    val image = findViewById<ImageView>(R.id.image)
                    Glide.with(this)
                        .load(item.image)
                        .override(1024)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(image)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item)

        textToSpeech = TextToSpeech(this, this)

        // Create an instance of FirebaseRepository
        val currentUser = FirebaseAuth.getInstance().currentUser
        val descriptionTextView = findViewById<TextView>(R.id.description)
        val itemId = intent.getStringExtra("id")

        setupItemDetails(currentUser, itemId)
        setupToolBar(itemId)
        setupEditFab(currentUser, itemId)

        setupAudioButton(descriptionTextView)
    }

    private fun setupItemDetails(currentUser: FirebaseUser?, itemId: String?) {
        if (itemId != null) {
            firebaseRepository.getItem(itemId).observe(this) { item: Item ->
                // Set title, description, image, and upload info
                setTitleAndDescription(item)
                setImage(item)
                setUploadInfo(item)

                // Update the bookmark icon based on the current user's bookmark status
                if (currentUser != null) {
                    firebaseRepository.isItemBookmarked(currentUser, item.id)
                        .observe(this) { isBookmarked ->
                            updateBookmarkIcon(isBookmarked)
                        }
                }

                // Add a view
                firebaseRepository.addViewToItem(item.id)

                if (currentUser != null) {
                    firebaseRepository.hasUserViewedItem(currentUser, item.id)
                        .observe(this) {
                            firebaseRepository.userViewedItem(currentUser, item.id)
                        }
                }
            }
        }
    }

    private fun setTitleAndDescription(item: Item) {
        val titleTextView = findViewById<TextView>(R.id.title)
        titleTextView.text = item.name

        val descriptionTextView = findViewById<TextView>(R.id.description)
        descriptionTextView.text = item.description
    }

    private fun setImage(item: Item) {
        val imageView = findViewById<ImageView>(R.id.image)
        Glide.with(this)
            .load(item.image)
            .override(1024)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imageView)
    }

    private fun setUploadInfo(item: Item) {
        val uploadInfoTextView = findViewById<TextView>(R.id.upload_info)
        val uploadDate = Date(item.addedOn)
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        uploadInfoTextView.text = getString(R.string.upload_info, item.addedBy, formatter.format(uploadDate))
    }

    private fun setupToolBar(itemId: String?) {
        val toolBar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolBar.setNavigationOnClickListener {
            finish()
        }

        toolBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.bookmarksButton -> {
                    val currentUser = FirebaseAuth.getInstance().currentUser
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
    }

    private fun setupEditFab(currentUser: FirebaseUser?, itemId: String?) {
        editFab = findViewById(R.id.edit_fab)
        // Check if user is logged in, if so, show edit button

        var user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userRoleRef =
                FirebaseDatabase.getInstance("https://csc306b-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("users").child(user.uid).child("role")
            userRoleRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val role = dataSnapshot.value.toString()
                    val isCurator = role == "curator"
                    editFab.isVisible = isCurator
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    println("The read failed: " + databaseError.code)
                }
            })
        } else {
            editFab.isVisible = false
        }

        // If user clicks the edit button, take them to the edit page
        editFab.setOnClickListener {
            val intent = Intent(this, EditItemActivity::class.java)
            intent.putExtra("id", itemId)
            editItemLauncher.launch(intent) // Use editItemLauncher instead of startActivity()
        }

        if (currentUser == null) {
            // Hide the bookmark button
            val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
            val bookmarksButton = toolbar.menu.findItem(R.id.bookmarksButton)
            bookmarksButton.isVisible = false
            editFab.isVisible = false
        }
    }

    private fun setupAudioButton(descriptionTextView: TextView) {
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
        textToSpeech.stop()
        textToSpeech.shutdown()
    }
}