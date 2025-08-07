package com.example.smartzone.helpers

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.smartzone.FirebaseTestInit
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class LoginIntegrationTests {
    private lateinit var loginHelper: LoginHelper
    private lateinit var auth: FirebaseAuth

    private val testEmail = "test_user@gmail.com"
    private val testPassword = "Password123"

    @Before
    fun setUp() =  runBlocking{
        FirebaseTestInit.initIfNeeded()

        val testFirebase = FirebaseApp.getInstance("test")
        auth = FirebaseAuth.getInstance(testFirebase)

        loginHelper = LoginHelper(auth)

        try {
            auth.signInWithEmailAndPassword(testEmail, testPassword).await()
            auth.signOut()
        } catch (e: Exception) {
            auth.createUserWithEmailAndPassword(testEmail, testPassword).await()
            auth.signOut()
        }
    }

    @Test
    fun loginUser_validInput_callsOnSuccess(){
        //Arrange
        val latch = CountDownLatch(1)
        var calledSucces = false
        var failureMessage = ""

        //Act
        loginHelper.loginUser(testEmail,testPassword,
            onSuccess = {
                calledSucces = true
                latch.countDown()
            },
            onFailure = {
                failureMessage = it
                latch.countDown()
            })
        latch.await(10, TimeUnit.SECONDS)

        //Assert
        assert(calledSucces)
    }
    @Test
    fun loginUser_wrongPassword_callsOnFailure(){
        //Arrange
        val latch = CountDownLatch(1)
        var calledSucces = false
        var failureMessage = ""

        //Act
        loginHelper.loginUser(testEmail,"wrongPassword1",
            onSuccess = {
                calledSucces = true
                latch.countDown()
            },
            onFailure = {
                failureMessage = it
                latch.countDown()
            })
        latch.await(10, TimeUnit.SECONDS)

        //Assert
        assertFalse(calledSucces)
        assertTrue(failureMessage?.contains("auth credential is incorrect") == true)
    }

    @Test
    fun loginUser_emptyFields_callsOnFailure(){
        //Arrange
        val latch = CountDownLatch(1)
        var failureMessage = ""

        //Act
        loginHelper.loginUser("","",
            onSuccess = {latch.countDown()},
            onFailure = {
                failureMessage = it
                latch.countDown()
            })
        latch.await()

        //Assert
        assertEquals("Please fill in all fields", failureMessage)
    }

}