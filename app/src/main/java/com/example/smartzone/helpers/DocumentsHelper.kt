package com.example.smartzone.helpers

import android.util.Log
import com.example.smartzone.entities.Document
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp

class DocumentsHelper(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {


    fun addDocument(
        zoneId: String,
        name: String,
        uri: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return

        val docRef = db.collection("users").document(userId)
            .collection("zones").document(zoneId).collection("documents").document()

        val documentData = hashMapOf(
            "name" to name,
            "fileUri" to uri,
            "uploadedAt" to Timestamp.now()
        )

        docRef.set(documentData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun getDocuments(
        zoneId: String,
        onSuccess: (List<Document>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return
        Log.d("DocumentsHelper", "UID: $userId, zoneId: $zoneId")
        db.collection("users").document(userId)
            .collection("zones").document(zoneId).collection("documents")
            .get()
            .addOnSuccessListener { result ->
                val docs = result.mapNotNull { doc ->
                    try {
                        Document(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            fileUri = doc.getString("fileUri") ?: "",
                            uploadedAt = doc.getTimestamp("uploadedAt") ?: Timestamp.now()
                        )
                    } catch (e: Exception) {
                        Log.e("DocumentsHelper", "Parsing error", e)
                        null
                    }
                }
                onSuccess(docs)
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun deleteDocument(
        zoneId: String,
        documentId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId)
            .collection("zones").document(zoneId)
            .collection("documents").document(documentId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}
