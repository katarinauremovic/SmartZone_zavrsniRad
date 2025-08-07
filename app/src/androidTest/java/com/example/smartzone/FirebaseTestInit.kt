package com.example.smartzone
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions


object FirebaseTestInit {

    private var initialized = false

    fun initIfNeeded() {
        if (initialized) return

        val context = InstrumentationRegistry.getInstrumentation().targetContext

        val options = FirebaseOptions.Builder()
            .setProjectId("smartzone-test-zavrsni")
            .setApplicationId("1:1045987300868:android:d4a3dc544c32568160c751")
            .setApiKey("AIzaSyBhPfW_pMQsAd1-Zfqv2gCsXKzo5k9WeGY")
            .build()

        FirebaseApp.initializeApp(context, options, "test")
        initialized = true
    }
}