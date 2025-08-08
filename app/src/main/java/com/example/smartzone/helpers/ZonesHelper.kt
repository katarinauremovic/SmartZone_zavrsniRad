package com.example.smartzone.helpers

import android.util.Log
import com.example.smartzone.entities.Zone
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ZonesHelper {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun loadZones(
        onSuccess: (List<Zone>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).collection("zones")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val zones = result.mapNotNull { doc ->
                    try {
                        val name = doc.getString("name") ?: return@mapNotNull null
                        val focus = doc.getString("focus") ?: ""
                        val createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now()

                        Zone(
                            id = doc.id,
                            name = name,
                            focus = focus,
                            createdAt = createdAt
                        )
                    } catch (e: Exception) {
                        Log.e("ZonesHelper", "Error parsing zone", e)
                        null
                    }
                }
                onSuccess(zones)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun sortZones(zones: List<Zone>, newestFirst: Boolean): List<Zone> {
        return if (newestFirst) {
            zones.sortedByDescending { it.createdAt }
        } else {
            zones.sortedBy { it.createdAt }
        }
    }

    fun filterZones(zones: List<Zone>, query: String): List<Zone> {
        val lowerQuery = query.trim().lowercase()
        return zones.filter {
            it.name.lowercase().contains(lowerQuery) || it.focus.lowercase().contains(lowerQuery)
        }
    }

    fun createZone(
        userId: String,
        name: String,
        focus: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val zoneRef = db.collection("users").document(userId).collection("zones").document()
        val generatedId = zoneRef.id

        val zoneData = hashMapOf(
            "name" to name,
            "focus" to focus,
            "createdAt" to Timestamp.now()
        )

        zoneRef.set(zoneData)
            .addOnSuccessListener { onSuccess(generatedId) }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun getZoneById(
        zoneId: String,
        onSuccess: (Zone) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users").document(userId)
            .collection("zones").document(zoneId)
            .get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: ""
                val focus = doc.getString("focus") ?: ""
                val createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now()
                onSuccess(Zone(doc.id, name, focus, createdAt))
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun updateZone(
        zoneId: String,
        newName: String,
        newFocus: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val updateData = mapOf("name" to newName, "focus" to newFocus)
        FirebaseFirestore.getInstance()
            .collection("users").document(userId)
            .collection("zones").document(zoneId)
            .update(updateData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun deleteZone(
        zoneId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users").document(userId)
            .collection("zones").document(zoneId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}
