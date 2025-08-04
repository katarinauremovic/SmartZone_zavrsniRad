package com.example.smartzone.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smartzone.R
import com.example.smartzone.helpers.RegistrationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class RegistrationActivity : AppCompatActivity() {
    private lateinit var registrationHelper: RegistrationHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        registrationHelper =
            RegistrationHelper(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())

        val firstNameEditText = findViewById<EditText>(R.id.firstNameEditText)
        val lastNameEditText = findViewById<EditText>(R.id.lastNameEditText)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val educationSpinner = findViewById<Spinner>(R.id.educationLevelSpinner)
        val birthDateEditText = findViewById<EditText>(R.id.dateOfBirthEditText)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val goToLoginText = findViewById<TextView>(R.id.goToLogin)

        birthDateEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog =
                DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                    val date = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                    birthDateEditText.setText(date)
                }, year, month, day)

            datePickerDialog.show()
        }

        registerButton.setOnClickListener {
            val firstName = firstNameEditText.text.toString().trim()
            val lastName = lastNameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val education = educationSpinner.selectedItem.toString()
            val birthDate = birthDateEditText.text.toString().trim()

            registrationHelper.registerUser(
                email, password, firstName, lastName, education, birthDate,
                onSuccess = {
                    Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, SettingsActivity::class.java))
                    finish()
                },
                onFailure = { message ->
                    Toast.makeText(this, "Registration failed: $message", Toast.LENGTH_LONG).show()
                }
            )
        }
        goToLoginText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}