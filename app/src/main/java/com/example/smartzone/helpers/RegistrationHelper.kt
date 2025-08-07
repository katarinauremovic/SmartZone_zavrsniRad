package com.example.smartzone.helpers

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegistrationHelper(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    fun registerUser(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        education: String,
        birthDate: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (email == "" || password == ""){
            onFailure("Email and password are required fields.")
            return
        }
        if (!PasswordValidator.isValid(password)) {
            onFailure("Password must be at least 8 characters long and contain uppercase, lowercase letters and a number.")
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = task.result?.user?.uid
                    if (userId != null) {
                        val user = hashMapOf(
                            "firstName" to firstName,
                            "lastName" to lastName,
                            "email" to email,
                            "education" to education,
                            "birthDate" to birthDate
                        )
                        firestore.collection("users").document(userId)
                            .set(user)
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener { onFailure(it.message ?: "Firestore error") }
                    } else {
                        onFailure("User ID is null")
                    }
                } else {
                    onFailure(task.exception?.message ?: "Authentication failed")
                }
            }
    }
}
