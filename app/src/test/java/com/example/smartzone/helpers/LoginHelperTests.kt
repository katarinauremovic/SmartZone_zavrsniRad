package com.example.smartzone.helpers

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class LoginHelperTests {

    private lateinit var loginHelper: LoginHelper
    private lateinit var auth: FirebaseAuth
    private lateinit var task: Task<AuthResult>

    @Before
    fun setup() {
        auth = mock()
        task = mock()

        loginHelper = LoginHelper(auth)
    }

    @Test
    fun loginUser_emptyFields_callsOnFailure() {
        //Arrange
        var calledSuccess = false
        var failureMessage = ""

        //Act
        loginHelper.loginUser("", "", onSuccess = {}, onFailure = {
            calledSuccess = true
            failureMessage = it
        })

        //Assert
        assert(calledSuccess)
        assert(failureMessage == "Please fill in all fields")
    }

    @Test
    fun loginUser_validInput_callsOnSuccess() {
        //Arrange
        val email = "test@gmail.com"
        val password = "Password123"
        var successCalled = false

        `when`(auth.signInWithEmailAndPassword(email, password)).thenReturn(task)

        `when`(task.addOnCompleteListener(any())).thenAnswer {
            val listener = it.arguments[0] as OnCompleteListener<AuthResult>
            `when`(task.isSuccessful).thenReturn(true)
            listener.onComplete(task)
            task
        }

        //Act
        loginHelper.loginUser(email, password,
            onSuccess = { successCalled = true },
            onFailure = { }
        )

        //Assert
        assert(successCalled)
    }

    @Test
    fun loginUser_wrongPassword_callsOnFailure() {
        //Arrange
        val email = "test@gmail.com"
        val password = "wrongpassword"
        var successCalled = false
        var failureMessage: String? = null

        val exception = Exception("Login failed")

        `when`(auth.signInWithEmailAndPassword(email, password)).thenReturn(task)

        `when`(task.addOnCompleteListener(any())).thenAnswer {
            val listener = it.arguments[0] as OnCompleteListener<AuthResult>
            `when`(task.isSuccessful).thenReturn(false)
            `when`(task.exception).thenReturn(exception)
            listener.onComplete(task)
            task
        }

        //Act
        loginHelper.loginUser(email, password,
            onSuccess = { successCalled = true },
            onFailure = {
                failureMessage = it
            }
        )

        //Assert
        assertFalse(successCalled)
        assert(failureMessage == "Login failed")
    }
}
