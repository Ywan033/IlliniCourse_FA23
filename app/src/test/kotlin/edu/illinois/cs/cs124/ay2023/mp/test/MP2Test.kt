package edu.illinois.cs.cs124.ay2023.mp.test

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.common.truth.Truth.assertWithMessage
import edu.illinois.cs.cs124.ay2023.mp.R
import edu.illinois.cs.cs124.ay2023.mp.activities.CourseActivity
import edu.illinois.cs.cs124.ay2023.mp.models.Course
import edu.illinois.cs.cs124.ay2023.mp.network.Client
import edu.illinois.cs.cs124.ay2023.mp.network.startServer
import edu.illinois.cs.cs124.ay2023.mp.test.helpers.COURSES
import edu.illinois.cs.cs124.ay2023.mp.test.helpers.SUMMARIES
import edu.illinois.cs.cs124.ay2023.mp.test.helpers.SUMMARY_COUNT
import edu.illinois.cs.cs124.ay2023.mp.test.helpers.compareCourses
import edu.illinois.cs.cs124.ay2023.mp.test.helpers.configureLogging
import edu.illinois.cs.cs124.ay2023.mp.test.helpers.countRecyclerView
import edu.illinois.cs.cs124.ay2023.mp.test.helpers.objectMapper
import edu.illinois.cs.cs124.ay2023.mp.test.helpers.pause
import edu.illinois.cs.cs124.ay2023.mp.test.helpers.startActivity
import edu.illinois.cs.cs124.ay2023.mp.test.helpers.startMainActivity
import edu.illinois.cs.cs124.ay2023.mp.test.helpers.testClient
import edu.illinois.cs.cs124.ay2023.mp.test.helpers.testServerGet
import edu.illinois.cs.cs124.ay2023.mp.test.helpers.toPath
import edu.illinois.cs.cs124.ay2023.mp.test.helpers.withRecyclerView
import edu.illinois.cs.cs125.gradlegrader.annotations.Graded
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.robolectric.Shadows
import org.robolectric.annotation.LooperMode
import java.net.HttpURLConnection
import java.util.Random

/*
 * This is the MP2 test suite.
 * The code below is used to evaluate your app during testing, local grading, and official grading.
 * You may not understand all of the code below, but you'll need to have some understanding of how
 * it works so that you can determine what is wrong with your app and what you need to fix.
 *
 * ALL CHANGES TO THIS FILE WILL BE OVERWRITTEN DURING OFFICIAL GRADING.
 * You can and should modify the code below if it is useful during your own local testing,
 * but any changes you make will be discarded during official grading.
 * The local grader will not run if the test suites have been modified, so you'll need to undo any
 * local changes before you run the grader.
 *
 * Note that this means that you should not fix problems with the app by modifying the test suites.
 * The test suites are always considered to be correct.
 *
 * Our test suites are broken into two parts.
 * The unit tests (in the UnitTests class) are tests that we can perform without running your app.
 * They test things like whether a method works properly or the behavior of your API server.
 * Unit tests are usually fairly fast.
 *
 * The integration tests (in the IntegrationTests class) are tests that require simulating your app.
 * This allows us to test things like your API client, and higher-level aspects of your app's
 * behavior, such as whether it displays the right thing on the display.
 * Because integration tests require simulating your app, they run more slowly.
 *
 * The MP2 test suite includes no ungraded tests.
 * These tests are fairly idiomatic, in that they resemble tests you might write for an actual
 * Android programming project.
 */

@RunWith(Enclosed::class)
class MP2Test {
    // Unit tests that don't require simulating the entire app, and usually complete quickly
    @FixMethodOrder(MethodSorters.NAME_ASCENDING)
    class UnitTests {
        init {
            // Start the API server
            startServer()
        }

        /** Test the Course class. */
        @Test(timeout = 1000L)
        @Graded(points = 10, friendlyName = "Course Class Design")
        fun test0_CourseClassDesign() {
            for (expectedString in COURSES) {
                val course: Course = objectMapper.readValue(expectedString)
                val courseString: String = objectMapper.writeValueAsString(course)
                compareCourses(expectedString, courseString)
            }
        }

        /** Test GET /course/ server route. */
        @Test(timeout = 10000L)
        @Graded(points = 20, friendlyName = "Server GET /course/")
        fun test1_ServerCourseRoute() {
            // Test good GET /course/ requests for all courses
            for (expectedString in COURSES) {
                val node = objectMapper.readTree(expectedString)!!
                val course: Course = "/course/${node.toPath()}".testServerGet(Course::class.java)!!
                val courseString: String = objectMapper.writeValueAsString(course)
                compareCourses(expectedString, courseString)
            }

            // Test bad requests
            // Bad URL
            "/course/CS/".testServerGet(HttpURLConnection.HTTP_BAD_REQUEST)
            // Non-existent course
            "/course/CS/188/".testServerGet(HttpURLConnection.HTTP_NOT_FOUND)
            // Non-existent URL
            "/courses/CS/124/".testServerGet(HttpURLConnection.HTTP_NOT_FOUND)
        }
    }

    // Integration tests that require simulating the entire app, and are usually slower
    @RunWith(AndroidJUnit4::class)
    @LooperMode(LooperMode.Mode.PAUSED)
    @FixMethodOrder(MethodSorters.NAME_ASCENDING)
    class IntegrationTests {
        init {
            // Set up logging so that you can see log output during testing
            configureLogging()
        }

        /** Test the client getCourse method. */
        @Test(timeout = 20000L)
        @Graded(points = 10, friendlyName = "Client GET /course/")
        fun test2_ClientGetCourse() {
            // Test getCourse requests for all courses
            for (i in 0 until SUMMARY_COUNT) {
                val summary = SUMMARIES[i]
                val expectedString = COURSES[i]
                val course: Course = testClient { callback -> Client.getCourse(summary, callback) }
                compareCourses(expectedString, objectMapper.writeValueAsString(course))
            }
        }

        /** Test onClick intent generation in MainActivity. */
        @Test(timeout = 10000L)
        @Graded(points = 20, friendlyName = "Summary Click Launch")
        fun test3_SummaryClickLaunch() {
            // Launch the main activity and confirm correct transition to CourseActivity
            startMainActivity { activity ->
                // Sanity checks
                onView(withId(R.id.recycler_view)).check(countRecyclerView(SUMMARY_COUNT))
                onView(withRecyclerView(R.id.recycler_view).atPosition(0))
                    .check(matches(hasDescendant(withText("CS 100: Computer Science Orientation"))))

                // Perform the click
                onView(withRecyclerView(R.id.recycler_view).atPosition(0)).perform(click())

                // Make sure the intent generated is correct
                val courseExtra = Shadows.shadowOf(activity).nextStartedActivity.getStringExtra("summary")
                val node = objectMapper.readTree(courseExtra)
                assertWithMessage("Intent contains incorrect subject")
                    .that(node["subject"].asText())
                    .isEqualTo("CS")
                assertWithMessage("Intent contains incorrect number")
                    .that(node["number"].asText())
                    .isEqualTo("100")
                assertWithMessage("Intent contains incorrect label")
                    .that(node["label"].asText())
                    .isEqualTo("Computer Science Orientation")
                assertWithMessage("Intent should not contain description")
                    .that(node["description"])
                    .isNull()
                assertWithMessage("Intent contains extra fields")
                    .that(node.size())
                    .isEqualTo(3)
            }
        }

        /** Test CourseActivity UI launched via intent. */
        @Test(timeout = 10000L)
        @Graded(points = 20, friendlyName = "Course View")
        fun test4_CourseView() {
            // Pick four random courses
            val random = Random(124)

            repeat(4) {
                // Create the Intent
                val summaryIndex = random.nextInt(SUMMARY_COUNT)
                val intent = Intent(ApplicationProvider.getApplicationContext(), CourseActivity::class.java).apply {
                    putExtra("summary", objectMapper.writeValueAsString(SUMMARIES[summaryIndex]))
                }

                // Start the CourseActivity and ensure that the title and description are shown
                startActivity<CourseActivity>(intent) {
                    val course = objectMapper.readTree(COURSES[summaryIndex])
                    pause()
                    val title =
                        "${course["subject"].asText()} ${course["number"].asText()}: ${course["label"].asText()}"
                    onView(withSubstring(title)).check(matches(isDisplayed()))
                    onView(withSubstring(course["description"].asText())).check(matches(isDisplayed()))
                }
            }
        }
    }
}

// md5: 28d6be10565ad178e88e505a9f172f27 // DO NOT REMOVE THIS LINE
