package com.example.smartzone.activities

import android.util.Log
import android.widget.Button
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.smartzone.FirebaseTestInit
import com.example.smartzone.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
class ZonesActivityTests {

    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null


    @Before
    fun setup() {
        FirebaseTestInit.initIfNeeded()
        auth = FirebaseAuth.getInstance()

        val email = "abcd@gmail.com"
        val password = "123456aB"

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

        Intents.init()
    }
    @After
    fun tearDown() {
        auth.signOut()
        Intents.release()
    }

    @Test
    fun testZoneListIsDisplayed() {
        ActivityScenario.launch(ZonesActivity::class.java)

        onView(withId(R.id.zonesRecyclerView)).check(matches(isDisplayed()))
    }

    @Test
    fun createZone_addButtonIsDisplayed() {
        ActivityScenario.launch(ZonesActivity::class.java)

        onView(withId(R.id.addZoneButton)).check(matches(isDisplayed()))
    }

    @Test
    fun searchZones_validInput_findsZoneByName() {
        ActivityScenario.launch(ZonesActivity::class.java)

        onView(withId(R.id.searchZoneEditText)).perform(typeText("Test"))
        onView(withId(R.id.zonesRecyclerView)).check(matches(hasDescendant(withText("Test"))))
    }

    @Test
    fun openZoneDetails_clickOnZone_opensDetailsOfZone() {
        ActivityScenario.launch(ZonesActivity::class.java)

        onView(withText("Test Zone")).perform(click())

        Intents.intended(hasComponent(ZoneDetailActivity::class.java.name))
    }

    @Test
    fun createZone_clicksOnButton_opensAddDialog() {
        ActivityScenario.launch(ZonesActivity::class.java)

        onView(withId(R.id.addZoneButton)).perform(click())
        onView(withId(R.id.zoneNameEditText)).check(matches(isDisplayed()))
        onView(withId(R.id.zoneFocusEditText)).check(matches(isDisplayed()))
    }

    @Test
    fun createZones_validZoneData_addsNewZone() {
        ActivityScenario.launch(ZonesActivity::class.java)

        onView(withId(R.id.addZoneButton)).perform(click())

        onView(withId(R.id.zoneNameEditText)).perform(typeText("Test Zone"))
        onView(withId(R.id.zoneFocusEditText)).perform(typeText("Test"))
        onView(withId(R.id.createButton)).perform(click())

        onView(withText("Test Zone")).check(matches(isDisplayed()))
    }
}
