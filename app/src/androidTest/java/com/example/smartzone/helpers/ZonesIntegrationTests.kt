package com.example.smartzone.helpers

import android.util.Log
import com.example.smartzone.FirebaseTestInit
import com.example.smartzone.entities.Zone
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch

class ZonesIntegrationTests {
    private lateinit var zonesHelper: ZonesHelper
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var currentUser: FirebaseUser? = null


    @Before
    fun setup() {
        FirebaseTestInit.initIfNeeded()
        val testApp = FirebaseApp.getInstance("test")
        auth = FirebaseAuth.getInstance(testApp)
        firestore = FirebaseFirestore.getInstance(testApp)

        val email = "testuser@gmail.com"
        val password = "testPassword123"

        val latch = CountDownLatch(1)
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    currentUser = auth.currentUser
                    Log.d("ZonesHelperTest", "User signed in successfully: ${currentUser?.email}")
                } else {
                    Log.e("ZonesHelperTest", "User sign-in failed: ${task.exception?.message}")
                }
                latch.countDown()
            }
        latch.await()

        zonesHelper = ZonesHelper(auth, firestore)
    }
    @After
    fun tearDown() {
        auth.signOut()
    }

    @Test
    fun createZone_validData_zoneAddSuccessfully() {
        // Arrange
        val name = "Test Zone"
        val focus = "Test Focus"
        val latch = CountDownLatch(1)

        var zoneId: String? = null
        var failureMessage: Exception? = null

        // Act
        zonesHelper.createZone(
            userId = currentUser!!.uid,
            name = name,
            focus = focus,
            onSuccess = { id ->
                zoneId = id
                latch.countDown()
            },
            onFailure = { exception ->
                failureMessage = exception
                latch.countDown()
            }
        )

        latch.await()

        val zoneRef = firestore.collection("users").document(currentUser!!.uid).collection("zones").document(zoneId!!)
        val document = Tasks.await(zoneRef.get())
        // Assert
        assertNotNull(zoneId)
        assertTrue(zoneId!!.isNotEmpty())
        assertNull(failureMessage)
        assertTrue(document.exists())
        assertEquals(name, document.getString("name"))
        assertEquals(focus, document.getString("focus"))
    }
    @Test
    fun loadZones_firestoreSuccess_zonesLoadedSuccessfully() {
        // Arrange
        val name1 = "Zone 1"
        val focus1 = "Focus 1"
        val name2 = "Zone 2"
        val focus2 = "Focus 2"
        val latch = CountDownLatch(1)

        val zone1 = hashMapOf(
            "name" to name1,
            "focus" to focus1,
            "createdAt" to Timestamp.now()
        )

        val zone2 = hashMapOf(
            "name" to name2,
            "focus" to focus2,
            "createdAt" to Timestamp.now()
        )

        val userZonesRef = firestore.collection("users").document(currentUser!!.uid).collection("zones")
        val zoneRef1 = userZonesRef.document()
        val zoneRef2 = userZonesRef.document()

        zoneRef1.set(zone1)
        zoneRef2.set(zone2)

        // Act
        var zones: List<Zone>? = null
        var failureMessage: Exception? = null

        zonesHelper.loadZones(
            userId = currentUser!!.uid,
            onSuccess = { loadedZones ->
                zones = loadedZones
                latch.countDown()
            },
            onFailure = { exception ->
                failureMessage = exception
                latch.countDown()
            }
        )

        latch.await()

        // Assert
        assertNull(failureMessage)
        assertNotNull(zones)
        assertTrue(zones!!.size >= 2)
        assertTrue(zones!!.any { it.name == name1 && it.focus == focus1 })
        assertTrue(zones!!.any { it.name == name2 && it.focus == focus2 })
    }

    @Test
    fun getZoneById_validId_zoneLoadedById() {
        // Arrange
        val name = "Zone to get"
        val focus = "Focus to get"
        val latch = CountDownLatch(1)

        val zoneData = hashMapOf(
            "name" to name,
            "focus" to focus,
            "createdAt" to Timestamp.now()
        )

        val zoneRef = firestore.collection("users").document(currentUser!!.uid).collection("zones").document()
        val zoneId = zoneRef.id
        zoneRef.set(zoneData)

        // Act
        var zone: Zone? = null
        var failureMessage: Exception? = null

        zonesHelper.getZoneById(
            currentUser!!.uid,
            zoneId = zoneId,
            onSuccess = { loadedZone ->
                zone = loadedZone
                latch.countDown()
            },
            onFailure = { exception ->
                failureMessage = exception
                latch.countDown()
            }
        )

        latch.await()

        // Assert
        assertNull(failureMessage)
        assertNotNull(zone)
        assertEquals(zoneId, zone!!.id)
        assertEquals(name, zone!!.name)
        assertEquals(focus, zone!!.focus)
    }

    @Test
    fun updateZone_validData_zoneUpdatedSuccessfully() {
        // Arrange
        val name = "Old Zone"
        val focus = "Old Focus"
        val newName = "Updated Zone"
        val newFocus = "Updated Focus"
        val latch = CountDownLatch(1)

        val zoneData = hashMapOf(
            "name" to name,
            "focus" to focus,
            "createdAt" to Timestamp.now()
        )

        val zoneRef = firestore.collection("users").document(currentUser!!.uid).collection("zones").document()
        val zoneId = zoneRef.id
        zoneRef.set(zoneData)

        // Act
        var failureMessage: Exception? = null

        zonesHelper.updateZone(
            currentUser!!.uid,
            zoneId = zoneId,
            newName = newName,
            newFocus = newFocus,
            onSuccess = {
                latch.countDown()
            },
            onFailure = { exception ->
                failureMessage = exception
                latch.countDown()
            }
        )

        latch.await()

        // Assert
        assertNull(failureMessage)
        val updatedDoc = firestore.collection("users").document(currentUser!!.uid).collection("zones").document(zoneId)
        val document = Tasks.await(updatedDoc.get())

        assertEquals(newName, document.getString("name"))
        assertEquals(newFocus, document.getString("focus"))
    }

    @Test
    fun deleteZone_validZoneId_zoneDeletedSuccessfully() {
        // Arrange
        val name = "Zone to delete"
        val focus = "Focus to delete"
        val latch = CountDownLatch(1)

        val zoneData = hashMapOf(
            "name" to name,
            "focus" to focus,
            "createdAt" to Timestamp.now()
        )

        val zoneRef =
            firestore.collection("users").document(currentUser!!.uid).collection("zones").document()
        val zoneId = zoneRef.id
        zoneRef.set(zoneData)

        // Act
        var failureMessage: Exception? = null

        zonesHelper.deleteZone(
            currentUser!!.uid,
            zoneId = zoneId,
            onSuccess = {
                latch.countDown()
            },
            onFailure = { exception ->
                failureMessage = exception
                latch.countDown()
            }
        )

        latch.await()

        // Assert
        assertNull(failureMessage)
        val deletedDoc =
            firestore.collection("users").document(currentUser!!.uid).collection("zones")
                .document(zoneId)
        val document = Tasks.await(deletedDoc.get())

        assertFalse(document.exists())

    }
}