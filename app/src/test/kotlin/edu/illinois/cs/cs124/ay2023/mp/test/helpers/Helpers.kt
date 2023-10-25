package edu.illinois.cs.cs124.ay2023.mp.test.helpers

import android.app.Activity
import android.content.Intent
import android.os.Looper
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import edu.illinois.cs.cs124.ay2023.mp.activities.MainActivity
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowLog
import java.io.PrintStream

/*
 * This file contains helper code used by the test suites.
 * You should not need to modify it.
 */

// Helper method to start the MainActivity
fun startMainActivity(action: ActivityScenario.ActivityAction<MainActivity>) {
    ActivityScenario.launch(MainActivity::class.java).use { scenario ->
        scenario.moveToState(Lifecycle.State.CREATED)
        scenario.moveToState(Lifecycle.State.RESUMED)
        pause()
        scenario.onActivity(action)
    }
}

// Helper method to start an Activity using an Intent
fun <T : Activity?> startActivity(intent: Intent, action: ActivityScenario.ActivityAction<T>) {
    ActivityScenario.launch<T>(intent).use { scenario ->
        scenario.moveToState(Lifecycle.State.CREATED)
        scenario.moveToState(Lifecycle.State.RESUMED)
        pause()
        scenario.onActivity(action)
    }
}

// Pause helper to improve the stability of our Robolectric tests
fun pause(length: Int = 100) {
    Shadows.shadowOf(Looper.getMainLooper()).runToEndOfTasks()
    Thread.sleep(length.toLong())
}

// Set up logging properly for testing
fun configureLogging() {
    if (System.getenv("OFFICIAL_GRADING") == null) {
        ShadowLog.setLoggable("LifecycleMonitor", Log.WARN)
        ShadowLog.stream = FilteringPrintStream()
    }
}

/**
 * This helper class allows filtering test results to remove extraneous output.
 */

class FilteringPrintStream : PrintStream(nullOutputStream()) {
    override fun println(line: String) {
        val parts = line.split(": ")
        if (parts.size < 2) {
            kotlin.io.println(line)
            return
        }
        val tagParts = parts[0].split("/")
        if (tagParts.size != 2) {
            kotlin.io.println(line)
            return
        }
        if (ignoredTags.contains(tagParts[1])) {
            return
        }
        kotlin.io.println(line)
    }

    companion object {
        private val ignoredTags: List<String> = mutableListOf(
            "LifecycleMonitor",
            "ActivityScenario",
            "AppCompatDelegate",
            "ViewInteraction",
            "Tracing",
            "EventInjectionStrategy",
        )
    }
}

// md5: 6a76da7e0e0e9d826ee190ec2a7ef3aa // DO NOT REMOVE THIS LINE
