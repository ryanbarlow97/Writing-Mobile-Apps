package com.rbarlow.csc306

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val loginButton = findViewById<Button>(R.id.loginButton)
        val forgotPasswordTextView = findViewById<TextView>(R.id.forgotPasswordTextView)
        val createAccountTextView = findViewById<TextView>(R.id.createAccountTextView)
        val guestLoginButton = findViewById<Button>(R.id.guestLoginButton)

        loginButton.setOnClickListener {
            // Perform login action
        }

        forgotPasswordTextView.setOnClickListener {
            // Handle forgot password action
        }

        createAccountTextView.setOnClickListener {
            // Handle create account action
        }

        guestLoginButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}
