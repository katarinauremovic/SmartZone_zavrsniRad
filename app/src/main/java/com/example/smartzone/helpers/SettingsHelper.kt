package com.example.smartzone.helpers

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingsHelper(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    fun updateProfile(
        userId: String,
        updates: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("users").document(userId)
            .update(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e.message ?: "Unknown error") }
    }

    fun changePassword(
        newPassword: String,
        confirmPassword: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (newPassword != confirmPassword) {
            onFailure("Passwords do not match")
            return
        }

        if (!PasswordValidator.isValid(newPassword)) {
            onFailure("Password must be at least 8 characters long and contain uppercase, lowercase letters and a number.")
            return
        }
        val user = auth.currentUser
        if (user != null) {
            user.updatePassword(newPassword)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onFailure(e.message ?: "Error while changing password") }
        } else {
            onFailure("User is not logged in")
        }
    }

    fun deleteAccount(
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val user = auth.currentUser
        if (user != null) {
            firestore.collection("users").document(user.uid).delete()
                .addOnSuccessListener {
                    user.delete()
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { e -> onFailure(e.message ?: "Failed to delete user account") }
                }
                .addOnFailureListener { e -> onFailure(e.message ?: "Error deleting data from Firestore") }
        } else {
            onFailure("User is not logged in")
        }
    }
}
