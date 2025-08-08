package com.example.smartzone.helpers

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.smartzone.FirebaseTestInit
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class SettingsIntegrationTests {

    private lateinit var helper: SettingsHelper
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    @Before
    fun setup() {
        FirebaseTestInit.initIfNeeded()
        val testApp = FirebaseApp.getInstance("test")
        auth = FirebaseAuth.getInstance(testApp)
        firestore = FirebaseFirestore.getInstance(testApp)
        helper = SettingsHelper(auth, firestore)
    }

    @Test
    fun updateProfile_updatesFieldsCorrectly_callsOnSuccess() {
        //Arrange
        val email = "update_${System.currentTimeMillis()}@gmail.com"
        val password = "Password123"

        runBlocking {
            auth.createUserWithEmailAndPassword(email, password).await()
        }

        val userId = auth.currentUser!!.uid

        runBlocking {
            firestore.collection("users").document(userId)
                .set(mapOf("email" to email))
                .await()
        }

        val updates = mapOf("firstName" to "newName", "education" to "updateEducation")
        val latch = CountDownLatch(1)
        var calledSuccess = false
        var failureMessage = ""

        //Act
        helper.updateProfile(userId, updates,
            onSuccess = {
                calledSuccess = true
                latch.countDown()
            },
            onFailure = {
                failureMessage = it
                latch.countDown()
            }
        )

        latch.await(10, TimeUnit.SECONDS)

        //Assert
        assert(calledSuccess)

        runBlocking {
            val snapshot = firestore.collection("users").document(userId).get().await()
            assertEquals("newName", snapshot.getString("firstName"))
            assertEquals("updateEducation", snapshot.getString("education"))
        }
    }

    @Test
    fun changePassword_newPasswordValid_changesPassword() {
        //Arrange
        val email = "changepass_${System.currentTimeMillis()}@gmail.com"
        val password = "Password123"
        val newPassword = "newPassword123"

        runBlocking {
            auth.createUserWithEmailAndPassword(email, password).await()
        }

        val latch = CountDownLatch(1)
        var calledSuccess = false
        var failureMessage = ""

        //Act
        helper.changePassword(newPassword, newPassword,
            onSuccess = {
                calledSuccess = true
                latch.countDown()
            },
            onFailure = {
                failureMessage = it
                latch.countDown()
            }
        )

        latch.await(10, TimeUnit.SECONDS)

        //Assert
        assert( calledSuccess)

        runBlocking {
            auth.signOut()

            auth.signInWithEmailAndPassword(email, newPassword).await()
            assertEquals(email, auth.currentUser?.email)
        }
    }

    @Test
    fun deleteAccount_deleteSuccess_deletesFromFirestoreAndAuth() {
        //Assert
        val email = "delete_${System.currentTimeMillis()}@gmail.com"
        val password = "Delete123"

        runBlocking {
            auth.createUserWithEmailAndPassword(email, password).await()
        }

        val userId = auth.currentUser!!.uid

        runBlocking {
            firestore.collection("users").document(userId)
                .set(mapOf("email" to email))
                .await()
        }

        val latch = CountDownLatch(1)
        var calledSuccess = false
        var failureMessage = ""

        //Act
        helper.deleteAccount(
            onSuccess = {
                calledSuccess = true
                latch.countDown()
            },
            onFailure = {
                failureMessage = it
                latch.countDown()
            }
        )

        latch.await(10, TimeUnit.SECONDS)

        //Assert
        assert(calledSuccess)

        runBlocking {
            val snapshot = firestore.collection("users").document(userId).get().await()
            assertFalse(snapshot.exists())
        }

    }
}

