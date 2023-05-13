package com.rbarlow.csc306

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.rbarlow.csc306.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initBindingAndAuth()

        setupClickListeners()
    }

    private fun initBindingAndAuth() {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
    }

    private fun setupClickListeners() {
        binding.loginButton.setOnClickListener {
            performLogin()
        }

        binding.forgotPasswordTextView.setOnClickListener {
            handleForgotPassword()
        }

        binding.createAccountTextView.setOnClickListener {
            navigateToCreateAccount()
        }

        binding.guestLoginButton.setOnClickListener {
            performGuestLogin()
        }
    }

    private fun performLogin() {
        val username = binding.usernameEditText.editText?.text.toString()
        val password = binding.passwordEditText.editText?.text.toString()

        if (username.isNotEmpty() && password.isNotEmpty()) {
            loginUser(username, password)
        }
    }

    private fun handleForgotPassword() {
        val email = binding.usernameEditText.editText?.text.toString()

        if (email.isNotEmpty()) {
            resetPassword(email)
        } else {
            showToast("Please enter your email")
        }
    }

    private fun navigateToCreateAccount() {
        startActivity(Intent(this, CreateAccountActivity::class.java))
    }

    private fun performGuestLogin() {
        auth.signOut()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun loginUser(username: String, password: String) {
        auth.signInWithEmailAndPassword(username, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    navigateToMainActivity()
                } else {
                    showToast("Login failed. Please check your credentials.")
                }
            }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("username", auth.currentUser?.email ?: "")
        }
        startActivity(intent)
        finish()
    }

    private fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast("Password reset email sent")
                } else {
                    showToast("Failed to send password reset email")
                }
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}