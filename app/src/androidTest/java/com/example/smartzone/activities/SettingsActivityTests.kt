package com.example.smartzone.activities

import android.widget.Button
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.smartzone.R
import org.hamcrest.CoreMatchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsActivityTests {

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun cleanup() {
        Intents.release()
    }

    @Test
    fun testProfileElementsAreDisplayed() {
        ActivityScenario.launch(SettingsActivity::class.java)

        onView(withId(R.id.editFirstName)).check(matches(isDisplayed()))
        onView(withId(R.id.editLastName)).check(matches(isDisplayed()))
        onView(withId(R.id.editEmail)).check(matches(isDisplayed()))
        onView(withId(R.id.editEducationSpinner)).check(matches(isDisplayed()))
        onView(withId(R.id.editBirthDate)).check(matches(isDisplayed()))
        onView(withId(R.id.saveProfileButton)).check(matches(isDisplayed()))
        onView(withId(R.id.changePasswordButton)).check(matches(isDisplayed()))
        onView(withId(R.id.deleteAccountButton)).check(matches(isDisplayed()))
        onView(withId(R.id.logoutButton)).check(matches(isDisplayed()))
    }

    @Test
    fun changePassword_clickOnButton_OpenChangePasswordDialog() {
        ActivityScenario.launch(SettingsActivity::class.java)

        onView(withId(R.id.changePasswordButton)).perform(scrollTo(), click())
        onView(withText("Change Password")).check(matches(isDisplayed()))
    }

    @Test
    fun deleteAccount_clickOnButton_OpenDeleteAccountDialog() {
        ActivityScenario.launch(SettingsActivity::class.java)

        onView(withId(R.id.deleteAccountButton)).perform(scrollTo(), click())
        onView(withText("Delete Account")).check(matches(isDisplayed()))
    }

    @Test
    fun logout_clickOnButton_OpenLogoutDialog() {
        ActivityScenario.launch(SettingsActivity::class.java)

        onView(withId(R.id.logoutButton))
            .check(matches(isDisplayed()))
            .perform(click())

        onView(withText("Are you sure you want to log out from your account?"))
            .check(matches(isDisplayed()))

        onView(allOf(withText("Log out"), isAssignableFrom(Button::class.java)))
            .check(matches(isDisplayed()))
    }

    @Test
    fun logout_clickOnConfirmButton_navigatesToLoginActivity() {
        ActivityScenario.launch(SettingsActivity::class.java)

        onView(withId(R.id.logoutButton))
            .check(matches(isDisplayed()))
            .perform(click())

        onView(allOf(withText("Log out"), isAssignableFrom(Button::class.java)))
            .perform(click())

        Intents.intended(hasComponent(LoginActivity::class.java.name))
    }
}
