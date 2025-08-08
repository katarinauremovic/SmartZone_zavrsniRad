package com.example.smartzone.helpers

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.CollectionReference
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import org.junit.Assert.assertEquals

class SettingsHelperTests {

    private lateinit var helper: SettingsHelper
    private val auth: FirebaseAuth = mock()
    private val firestore: FirebaseFirestore = mock()
    private val collectionReference: CollectionReference = mock()
    private val documentReference: DocumentReference = mock()
    private val user: FirebaseUser = mock()
    private val task: Task<Void> = mock()

    @Before
    fun setup() {
        helper = SettingsHelper(auth, firestore)
    }

    @Test
    fun updateProfile_validInput_callsOnSuccess() {
        //Arrange
        val updates = mapOf("name" to "newName")
        var calledSuccess = false

        whenever(firestore.collection("users")).thenReturn(collectionReference)
        whenever(collectionReference.document("user123")).thenReturn(documentReference)
        whenever(documentReference.update(updates)).thenReturn(task)

        whenever(task.addOnSuccessListener(any())).thenAnswer {
            val listener = it.arguments[0] as OnSuccessListener<Void>
            listener.onSuccess(null)
            task
        }

        whenever(task.addOnFailureListener(any())).thenReturn(task)

        //Act
        helper.updateProfile("user123", updates,
            onSuccess = { calledSuccess = true },
            onFailure = { })

        //Assert
        assert(calledSuccess)
    }

    @Test
    fun updateProfile_firestoreFails_callsOnFailure() {
        //Arrange
        val updates = mapOf("name" to "newName")
        val exception = Exception("Update failed")
        var failureMessage = ""

        whenever(firestore.collection("users")).thenReturn(collectionReference)
        whenever(collectionReference.document("user123")).thenReturn(documentReference)
        whenever(documentReference.update(updates)).thenReturn(task)

        whenever(task.addOnFailureListener(any())).thenAnswer {
            val listener = it.arguments[0] as OnFailureListener
            listener.onFailure(exception)
            task
        }

        whenever(task.addOnSuccessListener(any())).thenReturn(task)

        helper.updateProfile("user123", updates,
            onSuccess = { },
            onFailure = { failureMessage = it })

        assertEquals("Update failed", failureMessage)
    }

    @Test
    fun changePassword_passwordsDoNotMatch_callsOnFailure() {
        var failureMessage = ""

        helper.changePassword("password123", "otherPassword",
            onSuccess = { },
            onFailure = { failureMessage = it })

        assertEquals("Passwords do not match", failureMessage)
    }

    @Test
    fun changePassword_newPasswordIsWeak_callsOnFailure() {
        //Arange
        var failureMessage = ""

        //Act
        helper.changePassword("short", "short",
            onSuccess = { },
            onFailure = { failureMessage = it })

        //Assert
        assert(failureMessage.contains("Password must be"))
    }

    @Test
    fun changePassword_userNotLoggedIn_callsOnFailure() {
        //Arrange
        whenever(auth.currentUser).thenReturn(null)
        var failureMessage = ""

        //Act
        helper.changePassword("Password123", "Password123",
            onSuccess = { },
            onFailure = { failureMessage = it })

        //Assert
        assertEquals("User is not logged in", failureMessage)
    }

    @Test
    fun changePassword_newPasswordValid_callsOnSuccess() {
        //Arrange
        var calledSuccess = false

        whenever(auth.currentUser).thenReturn(user)
        whenever(user.updatePassword("Password123")).thenReturn(task)

        whenever(task.addOnSuccessListener(any())).thenAnswer {
            val listener = it.arguments[0] as OnSuccessListener<Void>
            listener.onSuccess(null)
            task
        }

        whenever(task.addOnFailureListener(any())).thenReturn(task)

        //Assert
        helper.changePassword("Password123", "Password123",
            onSuccess = { calledSuccess = true },
            onFailure = { })

        assert(calledSuccess)
    }

    @Test
    fun deleteAccount_userNotLoggedIn_callsOnFailure() {
        //Arrange
        whenever(auth.currentUser).thenReturn(null)
        var failureMessage = ""

        //Act
        helper.deleteAccount(
            onSuccess = { },
            onFailure = { failureMessage = it }
        )

        //Assert
        assertEquals("User is not logged in", failureMessage)
    }

    @Test
    fun deleteAccount_firestoreDeleteSuccess_callsOnSuccess() {
        //Arrange
        var calledSuccess = false

        whenever(auth.currentUser).thenReturn(user)
        whenever(user.uid).thenReturn("user123")
        whenever(firestore.collection("users")).thenReturn(collectionReference)
        whenever(collectionReference.document("user123")).thenReturn(documentReference)
        whenever(documentReference.delete()).thenReturn(task)
        whenever(user.delete()).thenReturn(task)

        whenever(task.addOnSuccessListener(any())).thenAnswer {
            val listener = it.arguments[0] as OnSuccessListener<Void>
            listener.onSuccess(null)
            task
        }

        whenever(task.addOnFailureListener(any())).thenReturn(task)

        //Act
        helper.deleteAccount(
            onSuccess = { calledSuccess = true },
            onFailure = { }
        )

        //Assert
        assert(calledSuccess)
    }

    @Test
    fun deleteAccount_firestoreDeleteFails_callsOnFailure() {
        //Arrange
        val exception = Exception("Firestore delete failed")
        var failureMessage = ""

        whenever(auth.currentUser).thenReturn(user)
        whenever(user.uid).thenReturn("user123")
        whenever(firestore.collection("users")).thenReturn(collectionReference)
        whenever(collectionReference.document("user123")).thenReturn(documentReference)
        whenever(documentReference.delete()).thenReturn(task)

        whenever(task.addOnFailureListener(any())).thenAnswer {
            val listener = it.arguments[0] as OnFailureListener
            listener.onFailure(exception)
            task
        }

        whenever(task.addOnSuccessListener(any())).thenReturn(task)

        //Act
        helper.deleteAccount(
            onSuccess = { },
            onFailure = { failureMessage = it }
        )

        //Assert
        assertEquals("Firestore delete failed", failureMessage)
    }
}
