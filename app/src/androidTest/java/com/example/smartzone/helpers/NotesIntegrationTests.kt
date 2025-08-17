package com.example.smartzone.helpers

import android.util.Log
import com.example.smartzone.FirebaseTestInit
import com.example.smartzone.entities.Note
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

class NotesIntegrationTests {
    private lateinit var notesHelper: NotesHelper
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

        notesHelper = NotesHelper(auth, firestore)
    }
    @After
    fun tearDown() {
        auth.signOut()
    }


    @Test
    fun addNote_firestoreSuccess_noteAddedSuccessfully() {
        // Arrange
        val zoneId = "hp19L05f93x3YuvYzKBq"
        val title = "Test Note"
        val content = "This is a test note content"
        val latch = CountDownLatch(1)

        var failureMessage: Exception? = null
        var successCalled = false
        // Act
        notesHelper.addNote(
            zoneId = zoneId,
            title = title,
            content = content,
            onSuccess = {
                successCalled = true
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
        assert(successCalled)
        val noteRef = firestore.collection("users")
            .document(currentUser!!.uid)
            .collection("zones").document(zoneId)
            .collection("notes")
        val noteQuery = noteRef.whereEqualTo("title", title)
            .whereEqualTo("content", content)
        val document = Tasks.await(noteQuery.get())

        assertTrue(document.size() > 0)
    }
    @Test
    fun getNotes_firestoreSuccess_notesLoadedSuccessfully() {
        // Arrange
        val zoneId = "hp19L05f93x3YuvYzKBq"
        val latch = CountDownLatch(1)
        val title = "Test Note"
        val content = "This is a test note content"
        val noteData = hashMapOf(
            "title" to title,
            "content" to content,
            "createdAt" to Timestamp.now()
        )

        val noteRef = firestore.collection("users").document(currentUser!!.uid)
            .collection("zones").document(zoneId)
            .collection("notes").document()
        noteRef.set(noteData)

        var notes: List<Note>? = null
        var error: Exception? = null

        // Act
        notesHelper.getNotes(
            zoneId = zoneId,
            onSuccess = { loadedNotes ->
                notes = loadedNotes
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
        assertNotNull(notes)
        assertTrue(notes!!.isNotEmpty())
        assertTrue(notes!!.any { it.title == title && it.content == content })
    }

    @Test
    fun deleteNote_firestoreSuccess_noteDeletedSuccessfully() {
        // Arrange
        val zoneId = "hp19L05f93x3YuvYzKBq"
        val latch = CountDownLatch(1)
        val title = "Test Note"
        val content = "This is a test note content"
        val noteData = hashMapOf(
            "title" to title,
            "content" to content,
            "createdAt" to Timestamp.now()
        )

        val noteRef = firestore.collection("users").document(currentUser!!.uid)
            .collection("zones").document(zoneId)
            .collection("notes").document()
        val noteId = noteRef.id
        noteRef.set(noteData)

        var error: Exception? = null

        // Act
        notesHelper.deleteNote(
            zoneId = zoneId,
            noteId = noteId,
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
            .collection("notes").document(noteId)
        val document = Tasks.await(deletedDoc.get())

        assertFalse(document.exists())
    }

    @Test
    fun updateNote_firestoreSuccess_noteUpdatedSuccessfully() {
        // Arrange
        val zoneId = "hp19L05f93x3YuvYzKBq"
        val latch = CountDownLatch(1)
        val oldTitle = "Old Title"
        val oldContent = "Old content"
        val newTitle = "Updated Title"
        val newContent = "Updated content"
        val noteData = hashMapOf(
            "title" to oldTitle,
            "content" to oldContent,
            "createdAt" to Timestamp.now()
        )

        val noteRef = firestore.collection("users").document(currentUser!!.uid)
            .collection("zones").document(zoneId)
            .collection("notes").document()
        val noteId = noteRef.id
        noteRef.set(noteData)

        var error: Exception? = null

        // Act
        notesHelper.updateNote(
            zoneId = zoneId,
            noteId = noteId,
            newTitle = newTitle,
            newContent = newContent,
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
        val updatedDoc = firestore.collection("users").document(currentUser!!.uid)
            .collection("zones").document(zoneId)
            .collection("notes").document(noteId)
        val document = Tasks.await(updatedDoc.get())

        assertEquals(newTitle, document.getString("title"))
        assertEquals(newContent, document.getString("content"))
    }
}