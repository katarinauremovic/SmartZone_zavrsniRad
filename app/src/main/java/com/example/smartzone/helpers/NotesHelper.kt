package com.example.smartzone.helpers

import com.example.smartzone.entities.Note
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NotesHelper (private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
                   private val db: FirebaseFirestore = FirebaseFirestore.getInstance()){


    fun addNote(
        zoneId: String,
        title: String,
        content: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return
        val noteData = hashMapOf(
            "title" to title,
            "content" to content,
            "createdAt" to Timestamp.now()
        )
        db.collection("users").document(userId)
            .collection("zones").document(zoneId)
            .collection("notes")
            .add(noteData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun getNotes(
        zoneId: String,
        onSuccess: (List<Note>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId)
            .collection("zones").document(zoneId)
            .collection("notes")
            .orderBy("createdAt")
            .get()
            .addOnSuccessListener { result ->
                val notes = result.map { doc ->
                    Note(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        content = doc.getString("content") ?: "",
                        createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now()
                    )
                }
                onSuccess(notes)
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun deleteNote(
        zoneId: String,
        noteId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId)
            .collection("zones").document(zoneId)
            .collection("notes").document(noteId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun updateNote(
        zoneId: String,
        noteId: String,
        newTitle: String,
        newContent: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return
        val updateData = mapOf(
            "title" to newTitle,
            "content" to newContent
        )
        db.collection("users").document(userId)
            .collection("zones").document(zoneId)
            .collection("notes").document(noteId)
            .update(updateData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}
