package com.example.smartzone.helpers

import android.util.Log
import com.example.smartzone.FirebaseTestInit
import com.example.smartzone.entities.Document
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch

class DocumentsIntegrationTests {
    private lateinit var documentsHelper: DocumentsHelper
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

        documentsHelper = DocumentsHelper(auth, firestore)
    }
    @After
    fun tearDown() {
        auth.signOut()
    }

    @Test
    fun addDocument_firestoreSuccess_documentAddedSuccessfully() {
        // Arrange
        val zoneId = "hp19L05f93x3YuvYzKBq"
        val name = "Test Document"
        val uri = "https://example.com/testdocument"
        val latch = CountDownLatch(1)

        var error: Exception? = null

        // Act
        documentsHelper.addDocument(
            zoneId = zoneId,
            name = name,
            uri = uri,
            onSuccess = {
                latch.countDown()
            },
            onFailure = { exception ->
                error = exception
                latch.countDown()
            }
        )

        latch.await()

        // Assert
        assertNull(error)
        val docRef = firestore.collection("users").document(currentUser!!.uid)
            .collection("zones").document(zoneId)
            .collection("documents")
        val docQuery = docRef.whereEqualTo("name", name).whereEqualTo("fileUri", uri)
        val document = Tasks.await(docQuery.get())

        assertTrue(document.size() > 0)
    }
    @Test
    fun getDocuments_firestoreSuccess_documentsLoadedSuccessfully() {
        // Arrange
        val zoneId = "hp19L05f93x3YuvYzKBq"
        val latch = CountDownLatch(1)
        val name = "Test Document"
        val uri = "https://example.com/testdocument"
        val documentData = hashMapOf(
            "name" to name,
            "fileUri" to uri,
            "uploadedAt" to Timestamp.now()
        )

        // Add a document to Firestore first
        val docRef = firestore.collection("users").document(currentUser!!.uid)
            .collection("zones").document(zoneId)
            .collection("documents").document()
        docRef.set(documentData)

        var documents: List<Document>? = null
        var error: Exception? = null

        // Act
        documentsHelper.getDocuments(
            zoneId = zoneId,
            onSuccess = { loadedDocuments ->
                documents = loadedDocuments
                latch.countDown()
            },
            onFailure = { exception ->
                error = exception
                latch.countDown()
            }
        )

        latch.await()

        // Assert
        assertNull(error)
        assertNotNull(documents)
        assertTrue(documents!!.isNotEmpty())
        assertTrue(documents!!.any { it.name == name && it.fileUri == uri })
    }

    @Test
    fun deleteDocument_firestoreSuccess_documentDeletedSuccessfully() {
        // Arrange
        val zoneId = "hp19L05f93x3YuvYzKBq"
        val latch = CountDownLatch(1)
        val name = "Test Document"
        val uri = "https://example.com/testdocument"
        val documentData = hashMapOf(
            "name" to name,
            "fileUri" to uri,
            "uploadedAt" to Timestamp.now()
        )

        val docRef = firestore.collection("users").document(currentUser!!.uid)
            .collection("zones").document(zoneId)
            .collection("documents").document()
        val documentId = docRef.id
        docRef.set(documentData)

        var error: Exception? = null

        // Act
        documentsHelper.deleteDocument(
            zoneId = zoneId,
            documentId = documentId,
            onSuccess = {
                latch.countDown()
            },
            onFailure = { exception ->
                error = exception
                latch.countDown()
            }
        )

        latch.await()

        // Assert
        assertNull(error)
        val deletedDoc = firestore.collection("users").document(currentUser!!.uid)
            .collection("zones").document(zoneId)
            .collection("documents").document(documentId)
        val document = Tasks.await(deletedDoc.get())

        assertFalse(document.exists())
    }


}