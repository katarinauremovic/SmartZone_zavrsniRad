package com.example.smartzone.activities

import ToastMatcher
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.smartzone.FirebaseTestInit
import com.example.smartzone.R
import org.hamcrest.CoreMatchers.anything
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
        Intents.release()
    }


    @Test
    fun registerUser_validInput_opensNewActivity(){
        //Arrange
        val email = "user_${System.currentTimeMillis()}@gmail.com"

        onView(withId(R.id.firstNameEditText)).perform(typeText("test"), closeSoftKeyboard())
        onView(withId(R.id.lastNameEditText)).perform(typeText("user"), closeSoftKeyboard())
        onView(withId(R.id.emailEditText)).perform(typeText(email), closeSoftKeyboard())
        onView(withId(R.id.passwordEditText)).perform(typeText("Password123"), closeSoftKeyboard())

        onView(withId(R.id.educationLevelSpinner)).perform(click())
        onData(anything()).atPosition(1).perform(click())

        onView(withId(R.id.dateOfBirthEditText)).perform(click())
        onView(withText("OK")).perform(click())

        //Act
        onView(withId(R.id.registerButton)).perform(click())
        Thread.sleep(1000)
        //Assert
        intended(hasComponent(SettingsActivity::class.java.name))
    }
}