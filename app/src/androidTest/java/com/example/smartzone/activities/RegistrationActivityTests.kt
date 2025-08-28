package com.example.smartzone.activities

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.smartzone.FirebaseTestInit
import com.example.smartzone.R
import com.google.firebase.auth.FirebaseAuth
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegistrationActivityTests {

    @get:Rule
    val activityRule = ActivityScenarioRule(RegistrationActivity::class.java)

    @Before
    fun setUp() {
        FirebaseTestInit.initIfNeeded()
        Intents.init()
    }

    @After
    fun tearDown() {
        FirebaseAuth.getInstance().signOut()
        Intents.release()
    }


    @Test
    fun registerUser_validInput_opensNewActivity(){
        //Arrange
        val email = "user_${System.currentTimeMillis()}@gmail.com"

        onView(withId(R.id.firstNameEditText))
            .perform(typeText("test"))
        onView(withId(R.id.lastNameEditText))
            .perform(typeText("user"))
        onView(withId(R.id.emailEditText))
            .perform(typeText(email))
        onView(withId(R.id.passwordEditText))
            .perform(typeText("Password123"))
        onView(isRoot())
            .perform(closeSoftKeyboard())

        //Act
        onView(withId(R.id.registerButton))
            .perform(scrollTo(), click())

    }

    @Test
    fun registerUser_emptyEmailOrPassword_showsMessage() {
        // Arrange

        // Act
        onView(withId(R.id.registerButton)).perform(scrollTo(), click())
        Thread.sleep(1000)

        // Assert
        onView(withText("Registration failed: Email and password are required fields."))
            .check(matches(isDisplayed()))
    }

    @Test
    fun registerUser_weakPassword_showsMessage() {
        // Arrange
        val email = "user_${System.currentTimeMillis()}@gmail.com"

        onView(withId(R.id.emailEditText)).perform(typeText(email))
        onView(withId(R.id.passwordEditText)).perform(typeText("pass"))

        // Act
        onView(withId(R.id.registerButton)).perform(scrollTo(), click())
        Thread.sleep(1000)

        // Assert
        onView(withText("Registration failed: Password must be at least 8 characters long and contain uppercase, lowercase letters and a number."))
            .check(matches(isDisplayed()))
    }
}