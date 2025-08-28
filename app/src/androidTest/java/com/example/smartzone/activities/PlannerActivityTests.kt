package com.example.smartzone.activities

import android.app.Activity
import android.app.Instrumentation
import android.provider.Settings
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.smartzone.FirebaseTestInit
import com.example.smartzone.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.hamcrest.Matchers.*
import org.junit.*
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class PlannerActivityTests {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var currentUser: FirebaseUser? = null

    @Before
    fun setup() {
        FirebaseTestInit.initIfNeeded()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val email = "abcd@gmail.com"
        val password = "123456aB"
        val latch = CountDownLatch(1)
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { latch.countDown() }
        latch.await(10, TimeUnit.SECONDS)
        currentUser = auth.currentUser
        Assert.assertNotNull("Login failed in @Before", currentUser)

        Intents.init()
        Intents.intending(hasAction(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null))
    }

    @After
    fun tearDown() {
        auth.signOut()
        Intents.release()
    }


    private fun awaitDisplayed(matcher: org.hamcrest.Matcher<android.view.View>, timeoutMs: Long = 7000) {
        val start = System.currentTimeMillis()
        var last: Throwable? = null
        while (System.currentTimeMillis() - start < timeoutMs) {
            try {
                onView(matcher).check(matches(isDisplayed()))
                return
            } catch (t: Throwable) {
                last = t
                Thread.sleep(120)
            }
        }
        if (last != null) throw last
    }


    @Test
    fun plannerList_isDisplayed() {
        ActivityScenario.launch(PlannerActivity::class.java)
        onView(withId(R.id.rvPlanner)).check(matches(isDisplayed()))
    }

    @Test
    fun addButton_isDisplayed() {
        ActivityScenario.launch(PlannerActivity::class.java)
        onView(withId(R.id.fabAddEvent)).check(matches(isDisplayed()))
    }

    @Test
    fun openAddDialog_clickFab_showsDialogAndInputs() {
        ActivityScenario.launch(PlannerActivity::class.java)

        onView(withId(R.id.fabAddEvent)).perform(click())

        onView(withText("Add Planner Event")).check(matches(isDisplayed()))

        onView(withId(R.id.etTitle)).check(matches(isDisplayed()))
        onView(withId(R.id.spinnerDay)).check(matches(isDisplayed()))
        onView(withId(R.id.timePicker)).check(matches(isDisplayed()))
        onView(withId(R.id.etReminder)).check(matches(isDisplayed()))
    }

    @Test
    fun cancelDialog_doesNotCreateItem() {
        ActivityScenario.launch(PlannerActivity::class.java)

        val title = "PlannerUIT_Cancel_${System.currentTimeMillis()}"

        onView(withId(R.id.fabAddEvent)).perform(click())
        onView(withId(R.id.etTitle)).perform(typeText(title))
        onView(isRoot()).perform(closeSoftKeyboard())
        onView(withText("Cancel")).perform(click())

        Thread.sleep(300)
        onView(withText(title)).check(doesNotExist())
    }

    @Test
    fun observeRealtime_whenExternalInsert_listUpdates() {
        runBlocking {
            ActivityScenario.launch(PlannerActivity::class.java)

            val title = "PlannerUIT_External_${System.currentTimeMillis()}"
            val uid = currentUser!!.uid
            val coll = firestore.collection("users")
                .document(uid)
                .collection("planner")

            val data = hashMapOf(
                "title" to title,
                "weekday" to 1,
                "startMinutes" to 9 * 60,
                "reminderMinutesBefore" to 10,
                "timezone" to java.time.ZoneId.systemDefault().id
            )
            val added = coll.add(data).await()
            Assert.assertNotNull(added.id)

            awaitDisplayed(withText(title))
            onView(withText(title)).check(matches(isDisplayed()))
        }
    }

}
