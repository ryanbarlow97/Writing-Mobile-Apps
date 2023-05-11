package com.rbarlow.csc306

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth


class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var usernameEditText: TextInputLayout
    private lateinit var passwordEditText: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)

        val loginButton = findViewById<Button>(R.id.loginButton)
        val forgotPasswordTextView = findViewById<TextView>(R.id.forgotPasswordTextView)
        val createAccountTextView = findViewById<TextView>(R.id.createAccountTextView)
        val guestLoginButton = findViewById<Button>(R.id.guestLoginButton)

        loginButton.setOnClickListener {
            val username = usernameEditText.editText?.text.toString()
            val password = passwordEditText.editText?.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                loginUser(username, password)
            }
        }

        forgotPasswordTextView.setOnClickListener {
            val email = usernameEditText.editText?.text.toString()

            if (email.isNotEmpty()) {
                resetPassword(email)
            } else {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            }
        }


        createAccountTextView.setOnClickListener {
            val intent = Intent(this, CreateAccountActivity::class.java)
            startActivity(intent)
        }

        guestLoginButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun loginUser(username: String, password: String) {
        auth.signInWithEmailAndPassword(username, password)
            .addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Login successful, proceed to the main activity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                // Login failed, display an error message
                Toast.makeText(this, "Login failed. Please check your credentials.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Password reset email sent successfully
                Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show()
            } else {
                // Password reset email failed
                Toast.makeText(this, "Failed to send password reset email", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
