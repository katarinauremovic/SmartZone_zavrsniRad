package com.example.smartzone.activities

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.smartzone.R
import com.example.smartzone.helpers.LoginHelper
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var loginHelper: LoginHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        loginHelper = LoginHelper(auth)

        val emailEditText = findViewById<EditText>(R.id.loginEmailEditText)
        val passwordEditText = findViewById<EditText>(R.id.loginPasswordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val goToRegisterText = findViewById<TextView>(R.id.goToRegisterText)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            loginHelper.loginUser(
                email,
                password,
                onSuccess = {
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, ZonesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                },
                onFailure = { message ->
                    Toast.makeText(this, "Login failed: $message", Toast.LENGTH_LONG).show()
                }
            )
        }

        goToRegisterText.setOnClickListener {
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
        }
    }
}

