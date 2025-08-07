package com.example.smartzone.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.example.smartzone.R
import com.example.smartzone.helpers.SettingsHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingsActivity : AppCompatActivity() {

    private lateinit var settingsHelper: SettingsHelper
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<Toolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        settingsHelper = SettingsHelper(auth, firestore)


        val firstNameEditText = findViewById<EditText>(R.id.editFirstName)
        val lastNameEditText = findViewById<EditText>(R.id.editLastName)
        val emailTextView = findViewById<TextView>(R.id.editEmail)
        val educationSpinner = findViewById<Spinner>(R.id.editEducationSpinner)
        val birthDateEditText = findViewById<EditText>(R.id.editBirthDate)
        val deleteAccountButton = findViewById<Button>(R.id.deleteAccountButton)
        val saveProfileButton = findViewById<Button>(R.id.saveProfileButton)
        val changePasswordButton = findViewById<Button>(R.id.changePasswordButton)

        val user = auth.currentUser

        user?.let { currentUser ->
            emailTextView.text = currentUser.email

            firestore.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { doc ->
                    firstNameEditText.setText(doc.getString("firstName") ?: "")
                    lastNameEditText.setText(doc.getString("lastName") ?: "")
                    birthDateEditText.setText(doc.getString("birthDate") ?: "")


                    val educationLevels = resources.getStringArray(R.array.education_levels)
                    val userEducation = doc.getString("education") ?: ""
                    val spinnerIndex = educationLevels.indexOf(userEducation)
                    if (spinnerIndex >= 0) {
                        educationSpinner.setSelection(spinnerIndex)
                    }
                }
        }
        saveProfileButton.setOnClickListener {
            val firstName = firstNameEditText.text.toString().trim()
            val lastName = lastNameEditText.text.toString().trim()
            val education = educationSpinner.selectedItem.toString()
            val birthDate = birthDateEditText.text.toString().trim()

            val updates = hashMapOf<String, Any>(
                "firstName" to firstName,
                "lastName" to lastName,
                "education" to education,
                "birthDate" to birthDate
            )

            val user = auth.currentUser
            val uid = user?.uid

            if (uid != null) {
                settingsHelper.updateProfile(
                    uid,
                    updates,
                    onSuccess = {
                        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { error ->
                        Toast.makeText(this, "Failed to update profile: $error", Toast.LENGTH_LONG).show()
                    }
                )
            } else {
                Toast.makeText(this, "User is not logged in", Toast.LENGTH_LONG).show()
            }
        }

        changePasswordButton.setOnClickListener {
            showChangePasswordDialog()
        }
        deleteAccountButton.setOnClickListener {
            showDeleteAccountConfirmationDialog()
        }
        val logoutButton = findViewById<ImageButton>(R.id.logoutButton)

        logoutButton.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showChangePasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val newPasswordInput = dialogView.findViewById<EditText>(R.id.newPasswordInput)
        val confirmPasswordInput = dialogView.findViewById<EditText>(R.id.confirmPasswordInput)

        val dialog = AlertDialog.Builder(this, R.style.MyAlertDialog)
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Change", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                ?.setTextColor(ContextCompat.getColor(this, R.color.primaryGreen))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                ?.setTextColor(ContextCompat.getColor(this, R.color.secondaryBlue))
        }

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
            val newPassword = newPasswordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()

            settingsHelper.changePassword(
                newPassword,
                confirmPassword,
                onSuccess = {
                    Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                },
                onFailure = { error ->
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun showDeleteAccountConfirmationDialog() {
        val dialog = AlertDialog.Builder(this, R.style.MyAlertDialog)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to permanently delete your account? This action cannot be undone.")
            .setPositiveButton("Delete", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                ?.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                ?.setTextColor(ContextCompat.getColor(this, R.color.secondaryBlue))
        }

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
            settingsHelper.deleteAccount(
                onSuccess = {
                    Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    finish()
                },
                onFailure = { error ->
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun showLogoutConfirmationDialog() {
        val dialog = AlertDialog.Builder(this, R.style.MyAlertDialog)
            .setTitle("Log out")
            .setMessage("Are you sure you want to log out from your account?")
            .setPositiveButton("Log out", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                ?.setTextColor(ContextCompat.getColor(this, R.color.primaryBlue))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                ?.setTextColor(ContextCompat.getColor(this, R.color.secondaryBlue))
        }

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            dialog.dismiss()


            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
