package edu.illinois.cs.cs124.ay2023.mp.test

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.truth.Truth.assertWithMessage
import edu.illinois.cs.cs124.ay2023.mp.R
import edu.illinois.cs.cs124.ay2023.mp.activities.CourseActivity
import edu.illinois.cs.cs124.ay2023.mp.models.Rating
import edu.illinois.cs.cs124.ay2023.mp.models.Summary
import edu.illinois.cs.cs124.ay2023.mp.network.Client
import edu.illinois.cs.cs124.ay2023.mp.network.resetServer
import edu.illinois.cs.cs124.ay2023.mp.network.startServer
import edu.illinois.cs.cs124.ay2023.mp.test.helpers.COURSES
import edu.illinois.cs.cs124.ay2023.mp.test.helpers.SUMMARIES
import edu.illinois.cs.cs124.ay2023.mp.test.helpers.configureLogging
import edu.illinois.cs.cs124.ay2023.mp.test.helpers.hasRating
import edu.illinois.cs.cs124.ay2023.mp.test.helpers.objectMapper
import edu.illinois.cs.cs124.ay2023.mp.test.helpers.pause
import edu.illinois.cs.cs124.ay2023.mp.test.helpers.setRating
import edu.illinois.cs.cs124.ay2023.mp.test.helpers.startActivity
import edu.illinois.cs.cs124.ay2023.mp.test.helpers.testClient
import edu.illinois.cs.cs124.ay2023.mp.test.helpers.testServerGet
import edu.illinois.cs.cs124.ay2023.mp.test.helpers.testServerPost
import edu.illinois.cs.cs124.ay2023.mp.test.helpers.toPath
import edu.illinois.cs.cs125.gradlegrader.annotations.Graded
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.robolectric.annotation.LooperMode
import java.net.HttpURLConnection
import kotlin.random.Random

/*
 * This is the MP3 test suite.
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
 * The MP3 test suite includes no ungraded tests.
 * These tests are fairly idiomatic, in that they resemble tests you might write for an actual
 * Android programming project.
 */

@RunWith(Enclosed::class)
class MP3Test {
    // Unit tests that don't require simulating the entire app, and usually complete quickly
    @FixMethodOrder(MethodSorters.NAME_ASCENDING)
    class UnitTests {
        init {
            // Start the API server
            startServer()
        }

        /** Reset the server before each test. */
        @Before
        fun reset() {
            try {
                resetServer()
            } catch (_: Exception) {
            }
        }

        /** Test the GET rating server route. */
        @Test(timeout = 10000L)
        @Graded(points = 10, friendlyName = "Server GET /rating/")
        fun test0_ServerGETRating() {
            // Note that until you complete the POST /rating/ route, this test is fairly limited
            // Test good requests
            for (courseString in COURSES) {
                val node = objectMapper.readTree(courseString)
                val rating = "/rating/${node.toPath()}".testServerGet<Rating>(Rating::class.java)!!
                assertWithMessage("Incorrect rating for unrated course")
                    .that(rating.rating)
                    .isEqualTo(Rating.NOT_RATED)
            }

            // Test bad requests
            // Bad URL
            "/rating/CS/".testServerGet(HttpURLConnection.HTTP_BAD_REQUEST)
            // Non-existent course
            "/rating/CS/188/".testServerGet(HttpURLConnection.HTTP_NOT_FOUND)
            // Non-existent URL
            "/ratings/CS/124/".testServerGet(HttpURLConnection.HTTP_NOT_FOUND)
        }

        /** Test the POST rating server route. */
        @Test(timeout = 20000L)
        @Graded(points = 20, friendlyName = "Server POST /rating/")
        fun test1_ServerPOSTRating() {
            // Proceed through courses in a deterministic random order
            val random = java.util.Random(124)
            // Perform initial GET /rating/ requests
            for (courseString in COURSES.shuffled(random)) {
                // Construct URL
                val node = objectMapper.readTree(courseString)
                val url = "/rating/${node.toPath()}"

                // Perform initial GET
                val rating = url.testServerGet<Rating>(Rating::class.java)!!
                assertWithMessage("Incorrect rating for unrated course")
                    .that(rating.rating)
                    .isEqualTo(Rating.NOT_RATED)
            }

            // Perform POST /rating/ requests to change ratings
            val ratings = mutableMapOf<String, Float>()
            for (courseString in COURSES.shuffled(random)) {
                val node = objectMapper.readTree(courseString)

                // POST to change rating
                val testRating = random.nextInt(51) / 10.0f

                // Construct POST rating body
                val newRating: ObjectNode = objectMapper.createObjectNode().apply {
                    set<JsonNode>("summary", objectMapper.convertValue(node, JsonNode::class.java))
                    set<JsonNode>("rating", objectMapper.convertValue(testRating, JsonNode::class.java))
                }

                val rating = "/rating/".testServerPost<Rating>(newRating, Rating::class.java)!!
                assertWithMessage("Incorrect rating from rating POST")
                    .that(rating.rating)
                    .isEqualTo(testRating)

                // Save rating value for next stage
                ratings[node.toPath()] = testRating
            }

            // Second route of GET /rating/ requests to ensure ratings are saved
            for (courseString in COURSES.shuffled(random)) {
                // Construct URL
                val node = objectMapper.readTree(courseString)
                val url = "/rating/${node.toPath()}"

                // Retrieve saved rating
                val savedRating = ratings[node.toPath()]!!

                // Final GET
                val rating = url.testServerGet<Rating>(Rating::class.java)!!
                assertWithMessage("Incorrect rating for course: should be $savedRating")
                    .that(rating.rating)
                    .isEqualTo(savedRating)
            }

            // Bad requests
            val newRating = objectMapper.createObjectNode().apply {
                set<JsonNode>("summary", objectMapper.convertValue(Summary("CS", "124"), JsonNode::class.java))
                set<JsonNode>("rating", objectMapper.convertValue(3.0, JsonNode::class.java))
            }

            "/ratings/".testServerPost(newRating, HttpURLConnection.HTTP_NOT_FOUND)

            // Non-existing course in rating
            newRating.set<JsonNode>("summary", objectMapper.convertValue(Summary("CS", "123"), JsonNode::class.java))
            "/rating/".testServerPost(newRating, HttpURLConnection.HTTP_NOT_FOUND)

            // Bad body
            "/rating/".testServerPost("test me", HttpURLConnection.HTTP_BAD_REQUEST)
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

        /** Reset the server before each test. */
        @Before
        fun reset() {
            try {
                resetServer()
            } catch (_: Exception) {
            }
        }

        /** Test the client getRating method.  */
        @Test(timeout = 20000L)
        @Graded(points = 10, friendlyName = "Client GET /rating/")
        fun test2_ClientGETRating() {
            // Note that until you complete the POST /rating/ route, this test is fairly limited
            for (summary in SUMMARIES) {
                val rating: Rating = testClient { callback ->
                    Client.getRating(summary, callback)
                }
                assertWithMessage("Incorrect summary subject for unrated course")
                    .that(rating.summary.subject)
                    .isEqualTo(summary.subject)
                assertWithMessage("Incorrect summary number for unrated course")
                    .that(rating.summary.number)
                    .isEqualTo(summary.number)
                assertWithMessage("Incorrect rating for unrated course")
                    .that(rating.rating)
                    .isEqualTo(Rating.NOT_RATED)
            }
        }

        /** Test the client postRating method. */
        @Test(timeout = 10000L)
        @Graded(points = 20, friendlyName = "Client POST /rating/")
        fun test3_ClientPOSTRating() {
            val random = Random(124)
            val testRatings = mutableMapOf<Summary, Float>()

            // Go through all courses twice
            repeat(2) {
                for (summary in SUMMARIES) {
                    // Randomly either GET or POST
                    var rating: Rating
                    if (random.nextBoolean()) {
                        rating = testClient { callback -> Client.getRating(summary, callback) }
                    } else {
                        val testRating = random.nextInt(51) / 10.0f
                        testRatings[summary] = testRating
                        rating = testClient { callback ->
                            Client.postRating(Rating(summary, testRating), callback)
                        }
                    }
                    val expectedRating = testRatings[summary] ?: Rating.NOT_RATED
                    assertWithMessage("Mismatch on rating")
                        .that(rating.rating)
                        .isEqualTo(expectedRating)
                    assertWithMessage("Incorrect summary subject")
                        .that(rating.summary.subject)
                        .isEqualTo(summary.subject)
                    assertWithMessage("Incorrect summary number")
                        .that(rating.summary.number)
                        .isEqualTo(summary.number)
                }
            }
        }

        // Helper method for the UI test
        @Throws(JsonProcessingException::class)
        private fun ratingViewHelper(summaryIndex: Int, startRating: Int, endRating: Int) {
            // Pull Summary and Course details
            val summaryString = objectMapper.writeValueAsString(SUMMARIES[summaryIndex])
            val courseString = COURSES[summaryIndex]

            // Prepare the Intent to start the CourseActivity
            val intent = Intent(ApplicationProvider.getApplicationContext(), CourseActivity::class.java)
            val summaryForIntent = (objectMapper.readTree(summaryString) as ObjectNode).apply {
                remove("description")
            }
            intent.putExtra("summary", summaryForIntent.toString())

            // Start the CourseActivity
            startActivity<CourseActivity>(intent) {
                val course: JsonNode = objectMapper.readTree(courseString)
                pause()
                // Test again that the title and description are shown
                val title =
                    "${course["subject"].asText()} ${course["number"].asText()}: ${course["label"].asText()}"
                onView(withSubstring(title)).check(matches(isDisplayed()))
                onView(withSubstring(course["description"].asText())).check(matches(isDisplayed()))

                // Check that the initial rating is correct, change it, and then verify the change
                onView(withId(R.id.rating))
                    .check(hasRating(startRating))
                    .perform(setRating(endRating))
                    .check(hasRating(endRating))
            }
        }

        /** Test rating view. */
        @Test(timeout = 30000L)
        @Graded(points = 20, friendlyName = "Rating View")
        fun test4_RatingView() {
            // Loop through first four courses, setting initial ratings
            repeat(4) { i ->
                pause()
                ratingViewHelper(i, 0, i)
                pause()
            }
            // Loop through first four courses, modifying ratings
            repeat(4) { i ->
                pause()
                ratingViewHelper(i, i, 5 - i)
                pause()
            }
            // Loop through first four courses, modifying ratings again
            repeat(4) { i ->
                pause()
                ratingViewHelper(i, 5 - i, i)
                pause()
            }
        }
    }
}

// md5: 753edd1282565e0d0a8f4ab6d0c8371b // DO NOT REMOVE THIS LINE
