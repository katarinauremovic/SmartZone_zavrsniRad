package com.example.smartzone.helpers

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.smartzone.FirebaseTestInit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.Before
import org.junit.runner.RunWith
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class RegistrationIntegrationTests {
    private lateinit var registrationHelper: RegistrationHelper
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    val firstName = "Test"
    val lastName = "User"
    val education = "High School"
    val birthDate = "01.01.2008."

    @Before
    fun setUp() {
        FirebaseTestInit.initIfNeeded()

        val testFirebase = FirebaseApp.getInstance("test")
        auth = FirebaseAuth.getInstance(testFirebase)
        firestore = FirebaseFirestore.getInstance(testFirebase)

        registrationHelper = RegistrationHelper(auth, firestore)
    }

    @Test
    fun registerUser_validInput_registerUserAndSavesUserInFirestore(){
        //Arrange
        val email = "testuser_${System.currentTimeMillis()}@gmail.com"
        val password = "Password123"

        val latch = CountDownLatch(1)
        var calledSuccess = false
        var failureMessage = ""

        //Act
        registrationHelper.registerUser(email,password,firstName,lastName,education,birthDate,
            onSuccess = {
                calledSuccess = true
                latch.countDown()
            },
            onFailure = {
                failureMessage = it
                latch.countDown()
            })
        latch.await(10,TimeUnit.SECONDS)
        val userId = auth.currentUser?.uid

        //Assert
        assert(calledSuccess)
        assertNotNull(userId)
        runBlocking {
            val snapshot = firestore.collection("users").document(userId!!).get().await()
            assertTrue(snapshot.exists())
            assertEquals(firstName,snapshot.getString("firstName"))
            assertEquals(email, snapshot.getString("email"))
        }
    }

    @Test
    fun registerUser_emailAlreadyInUse_callsOnFailure(){
        //Arrange
        val email = "duplicate_${System.currentTimeMillis()}@gmail.com"
        val password = "Password123"

        val latch1 = CountDownLatch(1)
        registrationHelper.registerUser(email,password, firstName,lastName,education,birthDate,
            onSuccess = {latch1.countDown()},
            onFailure = {latch1.countDown()})
        latch1.await(10,TimeUnit.SECONDS)

        val latch2 = CountDownLatch(1)
        var failureMessage = ""

        //Act
        registrationHelper.registerUser(email,password,firstName,lastName,education,birthDate,
            onSuccess = {latch2.countDown()},
            onFailure = {
                failureMessage = it
                latch2.countDown()
            })
        latch2.await(10, TimeUnit.SECONDS)

        //Assert
        assertTrue(failureMessage!!.contains("email") || failureMessage!!.contains("already"))


    }
}