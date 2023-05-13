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
import java.util.*

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

        auth = FirebaseAuth.getInstance()

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        createAccountButton = findViewById(R.id.createAccountButton)

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
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Account creation successful, display a success message
                    Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                    val user = FirebaseAuth.getInstance().currentUser
                    val database =
                        Firebase.database("https://csc306b-default-rtdb.europe-west1.firebasedatabase.app")

                    val userRole = "normal user" // or "curator"

                    // Create a reference to the user's node using their UID
                    val userRef = database.getReference("users").child(user?.uid ?: "")

                    // Set the role, email, and category under the user's node
                    userRef.child("role").setValue(userRole)
                    userRef.child("email").setValue(user?.email)

                    // Log in the user
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { signInTask ->
                            if (signInTask.isSuccessful) {
                                val intent = Intent(this, MainActivity::class.java).apply {
                                    putExtra("username", auth.currentUser?.email ?: "")
                                }
                                startActivity(intent)
                                finish()
                            } else {
                                // User login failed, display an error message
                                Toast.makeText(
                                    this,
                                    "Login failed. Please try again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    // Account creation failed, display an error message
                    Toast.makeText(
                        this,
                        "Account creation failed. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

}
