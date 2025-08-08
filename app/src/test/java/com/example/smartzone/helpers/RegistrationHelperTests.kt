package com.example.smartzone.helpers

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.lang.Exception

class RegistrationHelperTests {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var registrationHelper: RegistrationHelper
    private lateinit var task: Task<AuthResult>
    private lateinit var authResult: AuthResult
    private lateinit var user: FirebaseUser
    private val email = "test@gmail.com"
    private val password = "Password123"
    private val firstName = "Test"
    private val lastName = "User"
    private val education = "High School"
    private val birthDate = "01.01.2008."

    @Before
    fun setUp() {
        auth = mock()
        firestore = mock()
        registrationHelper = RegistrationHelper(auth, firestore)
        task = mock()
        authResult = mock()
        user = mock()
    }

    @Test
    fun registerUser_validInput_successfulRegistration() {
        //Arrange
        whenever(auth.createUserWithEmailAndPassword(email, password)).thenReturn(task)

        whenever(task.addOnCompleteListener(any())).thenAnswer {
            val listener = it.arguments[0] as OnCompleteListener<AuthResult>
            whenever(task.isSuccessful).thenReturn(true)
            whenever(task.result).thenReturn(authResult)
            whenever(authResult.user).thenReturn(user)
            whenever(user.uid).thenReturn("user1")
            listener.onComplete(task)
            task
        }

        val documentReference: DocumentReference = mock()
        val setTask: Task<Void> = mock()

        whenever(documentReference.set(any())).thenReturn(setTask)

        whenever(firestore.collection("users")).thenReturn(mock())
        whenever(firestore.collection("users").document("user1")).thenReturn(documentReference)
        whenever(setTask.addOnSuccessListener(any())).thenAnswer {
            val listener = it.arguments[0] as OnSuccessListener<Void>
            listener.onSuccess(null)
            setTask
        }

        whenever(setTask.addOnFailureListener(any())).thenReturn(setTask)

        var successCalled = false
        var failureMessage: String? = null

        //Act
        registrationHelper.registerUser(email, password, firstName, lastName, education, birthDate,
            onSuccess = { successCalled = true },
            onFailure = { failureMessage = it }
        )

        //Assert
        assert(successCalled)
        assert(failureMessage == null)

    }

    @Test
    fun registerUser_userIDisNull_callsOnFailure(){
        //Arrange
        var successCalled = false
        var failureMessage: String? = null

        whenever(auth.createUserWithEmailAndPassword(email,password)).thenReturn(task)
        whenever(task.addOnCompleteListener(any())).thenAnswer {
            val listener = it.arguments[0] as OnCompleteListener<AuthResult>
            whenever(task.isSuccessful).thenReturn(true)
            whenever(task.result).thenReturn(authResult)
            whenever(authResult.user).thenReturn(null)
            listener.onComplete(task)
            task
        }

        //Act
        registrationHelper.registerUser(email, password, firstName, lastName, education, birthDate,
            onSuccess = {successCalled = true},
            onFailure = {failureMessage = it})

        //Assert
        assertFalse(successCalled)
        assertEquals("User ID is null", failureMessage)
    }

    @Test
    fun registerUser_registrationFails_callsOnFailure(){
        //Arrange
        var successCalled = false
        var failureMessage: String? = null

        whenever(auth.createUserWithEmailAndPassword(email,password)).thenReturn(task)
        whenever(task.addOnCompleteListener(any())).thenAnswer {
            val listener = it.arguments[0] as OnCompleteListener<AuthResult>
            whenever(task.isSuccessful).thenReturn(false)
            whenever(task.exception).thenReturn(Exception("Authentication failed"))
            listener.onComplete(task)
            task
        }

        //Act
        registrationHelper.registerUser(email, password, firstName,lastName,education,birthDate,
            onSuccess = { successCalled = true },
            onFailure = {failureMessage = it })

        //Assert
        assertFalse(successCalled)
        assertEquals("Authentication failed", failureMessage)
    }

    @Test
    fun registerUser_firestoreFails_callsOnFailure() {
        //Arrange
        val documentReference: DocumentReference = mock()
        var successCalled = false
        var failureMessage: String? = null

        val firestoreError = Exception("Firestore write failed")
        val mockVoidTask = mock<Task<Void>>()

        whenever(auth.createUserWithEmailAndPassword(email, password)).thenReturn(task)
        whenever(documentReference.set(any())).thenReturn(mockVoidTask)

        whenever(firestore.collection("users")).thenReturn(mock())
        whenever(firestore.collection("users").document("user1")).thenReturn(documentReference)

        whenever(task.addOnCompleteListener(any())).thenAnswer {
            val listener = it.arguments[0] as OnCompleteListener<AuthResult>
            whenever(task.isSuccessful).thenReturn(true)
            whenever(task.result).thenReturn(authResult)
            whenever(authResult.user).thenReturn(user)
            whenever(user.uid).thenReturn("user1")
            listener.onComplete(task)
            task
        }

        whenever(mockVoidTask.addOnFailureListener(any())).thenAnswer {
            val listener = it.arguments[0] as OnFailureListener
            listener.onFailure(firestoreError)
            mockVoidTask
        }

        whenever(mockVoidTask.addOnSuccessListener(any())).thenReturn(mockVoidTask)

        //Act
        registrationHelper.registerUser(email, password, firstName, lastName, education, birthDate,
            onSuccess = { successCalled = true },
            onFailure = { failureMessage = it })

        //Assert
        assertFalse(successCalled)
        assertEquals("Firestore write failed", failureMessage)
    }

    @Test
    fun registerUser_passwordTooShort_callsOnFailure(){
        //Arrange
        val shortPassword = "pass"
        var successCalled = false
        var failureMessage = ""

        //Act
        registrationHelper.registerUser(email,shortPassword, firstName,lastName,education,birthDate,
            onSuccess = {successCalled = true},
            onFailure = {failureMessage = it})

        //Assert
        assertFalse(successCalled)
        assertEquals("Password must be at least 8 characters long and contain uppercase, lowercase letters and a number.", failureMessage)
    }

    @Test
    fun registerUser_passwordNoUppercase_callsOnFailure(){
        //Arrange
        val shortPassword = "password1"
        var successCalled = false
        var failureMessage = ""

        //Act
        registrationHelper.registerUser(email,shortPassword, firstName,lastName,education,birthDate,
            onSuccess = {successCalled = true},
            onFailure = {failureMessage = it})

        //Assert
        assertFalse(successCalled)
        assertEquals("Password must be at least 8 characters long and contain uppercase, lowercase letters and a number.", failureMessage)
    }

    @Test
    fun registerUser_passwordNoLowercase_callsOnFailure(){
        //Arrange
        val shortPassword = "PASSWORD1"
        var successCalled = false
        var failureMessage = ""

        //Act
        registrationHelper.registerUser(email,shortPassword, firstName,lastName,education,birthDate,
            onSuccess = {successCalled = true},
            onFailure = {failureMessage = it})

        //Assert
        assertFalse(successCalled)
        assertEquals("Password must be at least 8 characters long and contain uppercase, lowercase letters and a number.", failureMessage)
    }

    @Test
    fun registerUser_passwordNoDigit_callsOnFailure(){
        //Arrange
        val shortPassword = "Password"
        var successCalled = false
        var failureMessage = ""

        //Act
        registrationHelper.registerUser(email,shortPassword, firstName,lastName,education,birthDate,
            onSuccess = {successCalled = true},
            onFailure = {failureMessage = it})

        //Assert
        assertFalse(successCalled)
        assertEquals("Password must be at least 8 characters long and contain uppercase, lowercase letters and a number.", failureMessage)
    }

    @Test
    fun registerUser_emptyEmailField_callsOnFailure(){
        //Arrange
        var successCalled = false
        var failureMessage = ""

        //Act
        registrationHelper.registerUser("",password, firstName,lastName,education,birthDate,
            onSuccess = {successCalled = true},
            onFailure = {failureMessage = it})

        //Assert
        assertFalse(successCalled)
        assertEquals("Email and password are required fields.", failureMessage)
    }

    @Test
    fun registerUser_emptyPasswordField_callsOnFailure(){
        //Arrange
        var successCalled = false
        var failureMessage = ""

        //Act
        registrationHelper.registerUser(email,"", firstName,lastName,education,birthDate,
            onSuccess = {successCalled = true},
            onFailure = {failureMessage = it})

        //Assert
        assertFalse(successCalled)
        assertEquals("Email and password are required fields.", failureMessage)
    }
}