package com.rbarlow.csc306

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class CreateAccountActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var createAccountButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContentView(R.layout.activity_create_account)

        initFirebaseAuth()
        initViews()
        setCreateAccountButtonClickListener()
    }

    private fun initFirebaseAuth() {
        auth = FirebaseAuth.getInstance()
    }

    private fun initViews() {
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        createAccountButton = findViewById(R.id.createAccountButton)
    }

    private fun setCreateAccountButtonClickListener() {
        createAccountButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (password == confirmPassword) {
                createAccount(email, password, "normal user")
            } else {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createAccount(email: String, password: String, category: String) {
        createUserWithEmailAndPassword(email, password) { uid, errorMessage ->
            if (uid != null) {
                saveUserDetailsToDatabase(uid, email, category)
                loginUser(email, password)
            } else {
                showErrorMessage(errorMessage)
            }
        }
    }

    private fun createUserWithEmailAndPassword(
        email: String, password: String,
        callback: (uid: String?, errorMessage: String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    callback(uid, null)
                } else {
                    callback(null, "Account creation failed. Please try again.")
                }
            }
    }

    private fun saveUserDetailsToDatabase(uid: String, email: String, category: String) {
        val database = Firebase.database("https://csc306b-default-rtdb.europe-west1.firebasedatabase.app")
        val userRef = database.getReference("users").child(uid)
        userRef.child("role").setValue(category)
        userRef.child("email").setValue(email)
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { signInTask ->
                if (signInTask.isSuccessful) {
                    startMainActivity()
                } else {
                    showErrorMessage("Login failed. Please try again.")
                }
            }
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("username", auth.currentUser?.email ?: "")
        }
        startActivity(intent)
        finish()
    }

    private fun showErrorMessage(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}