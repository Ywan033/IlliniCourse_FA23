package edu.illinois.cs.cs124.ay2023.mp.test

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertWithMessage
import edu.illinois.cs.cs124.ay2023.mp.R
import edu.illinois.cs.cs124.ay2023.mp.models.Summary
import edu.illinois.cs.cs124.ay2023.mp.models.filter
import edu.illinois.cs.cs124.ay2023.mp.test.helpers.SUMMARIES
import edu.illinois.cs.cs124.ay2023.mp.test.helpers.SUMMARY_COUNT
import edu.illinois.cs.cs124.ay2023.mp.test.helpers.countRecyclerView
import edu.illinois.cs.cs124.ay2023.mp.test.helpers.pause
import edu.illinois.cs.cs124.ay2023.mp.test.helpers.searchFor
import edu.illinois.cs.cs124.ay2023.mp.test.helpers.startMainActivity
import edu.illinois.cs.cs124.ay2023.mp.test.helpers.withRecyclerView
import edu.illinois.cs.cs125.gradlegrader.annotations.Graded
import okhttp3.internal.toImmutableList
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.robolectric.annotation.LooperMode
import kotlin.random.Random

/*
 * This is the MP1 test suite.
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
 * The MP1 test suite includes no ungraded tests.
 * Note that test0_SummaryComparison and test1_SummaryFilter were generated from the MP reference
 * solution, and as such do not represent what a real-world test suite would typically look like.
 * (It would have fewer examples chosen more carefully.)
 */

@RunWith(Enclosed::class)
class MP1Test {
    // Unit tests that don't require simulating the entire app, and usually complete quickly
    @FixMethodOrder(MethodSorters.NAME_ASCENDING)
    class UnitTests {
        // Private copy of the summaries list, shuffled to improve testing
        // Seed the random number generator for reproducibility, and make the list immutable to
        // detect modifications during testing
        private val shuffledSummaries = SUMMARIES.shuffled(Random(124)).toImmutableList()

        // Helper method for test0_SummaryComparison
        private fun summaryComparisonHelper(firstIndex: Int, secondIndex: Int, expected: Int) {
            // first and second should be indices into our shuffled list of summaries
            val first = shuffledSummaries[firstIndex]
            assertWithMessage("Invalid summary index: $firstIndex").that(first).isNotNull()
            val second = shuffledSummaries[secondIndex]
            assertWithMessage("Invalid summary index: $secondIndex").that(second).isNotNull()

            // Test the forward comparison
            val forward: Int = first.compareTo(second)
            when {
                expected == 0 -> assertWithMessage("Summaries $first and $second should be compared equal")
                    .that(forward)
                    .isEqualTo(0)

                expected < 0 -> assertWithMessage("Summary $first should be less than $second")
                    .that(forward)
                    .isLessThan(0)

                else -> assertWithMessage("Summary $first should be greater than $second")
                    .that(forward)
                    .isGreaterThan(0)
            }

            // Test the reverse comparison
            val reverse: Int = second.compareTo(first)
            when {
                expected == 0 -> assertWithMessage("Summaries $second and $first should be compared equal")
                    .that(reverse)
                    .isEqualTo(0)

                expected < 0 -> assertWithMessage("Summary $second should be less than $first")
                    .that(reverse)
                    .isGreaterThan(0)

                else -> assertWithMessage("Summary $second should be greater than $first")
                    .that(reverse)
                    .isLessThan(0)
            }
        }

        /** Test the summary default comparison (compareTo). */
        @Test(timeout = 1000L)
        @Graded(points = 25, friendlyName = "Summary Comparison")
        @Suppress("LongMethod")
        fun test0_SummaryComparison() {
            // Test a variety of pairs chosen randomly from our shuffled list of summaries
            // Test self-comparisons
            summaryComparisonHelper(134, 134, 0)
            summaryComparisonHelper(228, 228, 0)
            summaryComparisonHelper(280, 280, 0)
            summaryComparisonHelper(138, 138, 0)
            // Test random pairs
            summaryComparisonHelper(268, 133, -4)
            summaryComparisonHelper(159, 198, 1)
            summaryComparisonHelper(41, 259, -1)
            summaryComparisonHelper(96, 268, 4)
            summaryComparisonHelper(41, 41, 0)
            summaryComparisonHelper(56, 247, 6)
            summaryComparisonHelper(144, 144, 0)
            summaryComparisonHelper(265, 265, 0)
            summaryComparisonHelper(178, 178, 0)
            summaryComparisonHelper(133, 133, 0)
            summaryComparisonHelper(243, 20, -1)
            summaryComparisonHelper(254, 131, -1)
            summaryComparisonHelper(275, 126, -3)
            summaryComparisonHelper(57, 57, 0)
            summaryComparisonHelper(232, 300, -3)
            summaryComparisonHelper(98, 38, -2)
            summaryComparisonHelper(249, 249, 0)
            summaryComparisonHelper(116, 73, 4)
            summaryComparisonHelper(224, 224, 0)
            summaryComparisonHelper(78, 78, 0)
            summaryComparisonHelper(82, 82, 0)
            summaryComparisonHelper(176, 75, -2)
            summaryComparisonHelper(83, 180, 10)
            summaryComparisonHelper(193, 193, 0)
            summaryComparisonHelper(5, 5, 0)
            summaryComparisonHelper(119, 119, 0)
            summaryComparisonHelper(184, 145, -6)
            summaryComparisonHelper(234, 234, 0)
            summaryComparisonHelper(27, 279, -2)
            summaryComparisonHelper(232, 232, 0)
            summaryComparisonHelper(289, 289, 0)
            summaryComparisonHelper(168, 117, -2)
            summaryComparisonHelper(226, 226, 0)
            summaryComparisonHelper(259, 86, 2)
            summaryComparisonHelper(197, 197, 0)
            summaryComparisonHelper(207, 61, 5)
            summaryComparisonHelper(232, 232, 0)
            summaryComparisonHelper(76, 76, 0)
            summaryComparisonHelper(23, 152, -1)
            summaryComparisonHelper(201, 268, 10)
            summaryComparisonHelper(182, 182, 0)
            summaryComparisonHelper(114, 22, 1)
            summaryComparisonHelper(253, 61, -1)
            summaryComparisonHelper(228, 276, 5)
            summaryComparisonHelper(277, 207, -1)
            summaryComparisonHelper(214, 214, 0)
            summaryComparisonHelper(9, 9, 0)
            summaryComparisonHelper(247, 56, -6)
            summaryComparisonHelper(238, 303, -3)
            summaryComparisonHelper(128, 128, 0)
            summaryComparisonHelper(249, 300, -3)
            summaryComparisonHelper(71, 71, 0)
            summaryComparisonHelper(240, 24, 4)
            summaryComparisonHelper(143, 143, 0)
            summaryComparisonHelper(24, 24, 0)
            summaryComparisonHelper(201, 268, 10)
            summaryComparisonHelper(26, 236, 1)
            summaryComparisonHelper(238, 238, 0)
            summaryComparisonHelper(256, 291, -6)
            summaryComparisonHelper(140, 167, -6)
            summaryComparisonHelper(74, 164, 3)
            summaryComparisonHelper(300, 18, 6)
            summaryComparisonHelper(158, 112, 1)
            summaryComparisonHelper(50, 279, -1)
        }

        // Helper method to convert a list of summaries into a list of indices into our
        // shuffled list of summaries
        private fun summaryListToPositionList(list: List<Summary?>) =
            list.map { summary -> shuffledSummaries.indexOf(summary) }

        // Helper method for test1_SummaryFilter
        private fun summaryFilterHelper(list: List<Summary>, filter: String, size: Int, expectedPositions: List<Int>?) {
            // Filter the list using the summary filter
            val filteredList = list.filter(filter)
            // Filtered list should never be null
            assertWithMessage("List filtered with \"$filter\" should not be null").that(filteredList).isNotNull()
            // Filtered list should return a new list, except if the result is the empty list
            if (filteredList.isNotEmpty()) {
                assertWithMessage("List filter should return a new list").that(filteredList).isNotSameInstanceAs(list)
            }
            // Check the size of the filtered list
            assertWithMessage("List filtered with \"$filter\" should have size $size").that(filteredList).hasSize(size)
            // Check whether the filtered list includes the right summaries in the correct positions
            if (expectedPositions != null) {
                val positions = summaryListToPositionList(filteredList)
                assertWithMessage("List positions filtered with \"$filter\" is not the right size").that(positions)
                    .hasSize(expectedPositions.size)
                for (i in positions.indices) {
                    assertWithMessage("Summary in incorrect position using filter \"$filter\"").that(positions[i])
                        .isEqualTo(
                            expectedPositions[i],
                        )
                }
            }
        }

        /** Test summary filtering. */
        @Test(timeout = 1000L)
        @Graded(points = 25, friendlyName = "Summary Filtering")
        @Suppress("LongMethod", "SpellCheckingInspection")
        fun test1_SummaryFilter() {
            // Test a variety of filtering calls, most on our shuffled list of summaries
            // Test a few searches on the empty list
            summaryFilterHelper(emptyList(), "", 0, emptyList())
            summaryFilterHelper(emptyList(), " ", 0, emptyList())
            summaryFilterHelper(emptyList(), "test", 0, emptyList())

            // Test a variety of searches on the shuffled list of summaries
            summaryFilterHelper(shuffledSummaries, "verification", 1, listOf(156))
            summaryFilterHelper(shuffledSummaries, "IS 559: CAS", 1, listOf(264))
            summaryFilterHelper(shuffledSummaries, "cs", 169, null)
            summaryFilterHelper(shuffledSummaries, "topics", 39, null)
            summaryFilterHelper(shuffledSummaries, "study", 9, null)
            summaryFilterHelper(shuffledSummaries, " Advanced Topics In Information Organi", 1, listOf(157))
            summaryFilterHelper(shuffledSummaries, "Intro to Combinat", 1, listOf(155))
            summaryFilterHelper(shuffledSummaries, "200:", 2, listOf(46, 150))
            summaryFilterHelper(shuffledSummaries, "in", 178, null)
            summaryFilterHelper(shuffledSummaries, "is", 177, null)
            summaryFilterHelper(shuffledSummaries, "accelerated", 4, listOf(187, 184, 57, 253))
            summaryFilterHelper(shuffledSummaries, ": Advanced Topic", 22, null)
            summaryFilterHelper(shuffledSummaries, "2: ", 25, null)
            summaryFilterHelper(shuffledSummaries, "S 501", 1, listOf(123))
            summaryFilterHelper(shuffledSummaries, "information", 39, null)
            summaryFilterHelper(shuffledSummaries, "and", 41, null)
            summaryFilterHelper(shuffledSummaries, "401:", 2, listOf(184, 145))
            summaryFilterHelper(shuffledSummaries, "rganization & ", 1, listOf(280))
            summaryFilterHelper(shuffledSummaries, "n", 266, null)
            summaryFilterHelper(shuffledSummaries, "cs ", 169, null)
            summaryFilterHelper(shuffledSummaries, "TAT ", 66, null)
            summaryFilterHelper(shuffledSummaries, "data", 36, null)
            summaryFilterHelper(shuffledSummaries, "user", 5, listOf(101, 4, 21, 74, 173))
            summaryFilterHelper(shuffledSummaries, "S 426: Compil", 1, listOf(91))
            summaryFilterHelper(shuffledSummaries, "of Ma", 1, listOf(113))
            summaryFilterHelper(shuffledSummaries, "visualization", 2, listOf(38, 82))
            summaryFilterHelper(shuffledSummaries, "480:", 1, listOf(295))
            summaryFilterHelper(shuffledSummaries, "s", 306, null)
            summaryFilterHelper(shuffledSummaries, ": Computers and", 1, listOf(94))
            summaryFilterHelper(shuffledSummaries, "ificia", 1, listOf(109))
            summaryFilterHelper(shuffledSummaries, "S 233: Compu", 1, listOf(176))
            summaryFilterHelper(shuffledSummaries, ":", 306, null)
            summaryFilterHelper(shuffledSummaries, " 40", 19, null)
            summaryFilterHelper(shuffledSummaries, "atabase D", 1, listOf(81))
            summaryFilterHelper(shuffledSummaries, "n Histo", 1, listOf(163))
            summaryFilterHelper(shuffledSummaries, "STAT 107", 1, listOf(83))
            summaryFilterHelper(shuffledSummaries, "u", 140, null)
            summaryFilterHelper(shuffledSummaries, "S 439: Web Development Using Applicat", 1, listOf(221))
            summaryFilterHelper(shuffledSummaries, " 309: Compute", 1, listOf(94))
            summaryFilterHelper(shuffledSummaries, "541:", 2, listOf(183, 144))
            summaryFilterHelper(shuffledSummaries, "ndam", 5, listOf(234, 187, 184, 57, 253))
            summaryFilterHelper(shuffledSummaries, "formation Prof", 4, listOf(183, 32, 299, 29))
            summaryFilterHelper(shuffledSummaries, "S 542:", 2, listOf(153, 15))
            summaryFilterHelper(shuffledSummaries, "13: Teen Mate", 1, listOf(19))
            summaryFilterHelper(shuffledSummaries, "4", 148, null)
            summaryFilterHelper(shuffledSummaries, "6: Topic", 2, listOf(284, 207))
            summaryFilterHelper(shuffledSummaries, ": U", 8, listOf(218, 101, 4, 74, 21, 51, 78, 170))
            summaryFilterHelper(shuffledSummaries, "599: The", 3, listOf(104, 272, 7))
            summaryFilterHelper(shuffledSummaries, "analytics", 4, listOf(295, 282, 144, 298))
            summaryFilterHelper(shuffledSummaries, "research", 14, null)
            summaryFilterHelper(shuffledSummaries, "intelligence", 2, listOf(109, 89))
            summaryFilterHelper(shuffledSummaries, "i", 295, null)
            summaryFilterHelper(shuffledSummaries, "the", 22, null)
            summaryFilterHelper(shuffledSummaries, " 6", 46, null)
            summaryFilterHelper(shuffledSummaries, " ndergradu", 2, listOf(218, 78))
            summaryFilterHelper(shuffledSummaries, "431: Embedded System", 1, listOf(131))
            summaryFilterHelper(shuffledSummaries, "distributed", 3, listOf(127, 294, 114))
            summaryFilterHelper(shuffledSummaries, " ies An", 4, listOf(62, 31, 29, 35))
            summaryFilterHelper(shuffledSummaries, " 5", 149, null)
            summaryFilterHelper(shuffledSummaries, " 43", 19, null)
            summaryFilterHelper(shuffledSummaries, "bioinformatics ", 2, listOf(201, 68))
            summaryFilterHelper(shuffledSummaries, "science", 30, null)
            summaryFilterHelper(shuffledSummaries, "S 571: Advanced Topics in Use a", 1, listOf(173))
            summaryFilterHelper(shuffledSummaries, "AT 578: ", 1, listOf(223))
            summaryFilterHelper(shuffledSummaries, " the ", 22, null)
            summaryFilterHelper(shuffledSummaries, "'s Mate ", 1, listOf(250))
            summaryFilterHelper(shuffledSummaries, "bsis", 2, listOf(79, 108))
            summaryFilterHelper(shuffledSummaries, " open", 2, listOf(218, 78))
            summaryFilterHelper(shuffledSummaries, " Game Develkpmen", 0, listOf())
            summaryFilterHelper(shuffledSummaries, "history", 6, listOf(86, 71, 73, 185, 163, 190))
            summaryFilterHelper(shuffledSummaries, "icas Modeling", 0, listOf())
            summaryFilterHelper(shuffledSummaries, "9f: Adva", 0, listOf())
            summaryFilterHelper(shuffledSummaries, "statisticae", 0, listOf())
            summaryFilterHelper(shuffledSummaries, "ation Slience Study Abr", 0, listOf())
            summaryFilterHelper(shuffledSummaries, "gS 3", 0, listOf())
            summaryFilterHelper(shuffledSummaries, "eb Tech", 1, listOf(161))
            summaryFilterHelper(shuffledSummaries, "fod", 0, listOf())
            summaryFilterHelper(shuffledSummaries, "computatiok", 0, listOf())
            summaryFilterHelper(shuffledSummaries, "iq", 2, listOf(161, 29))
            summaryFilterHelper(shuffledSummaries, " anv D", 0, listOf())
            summaryFilterHelper(shuffledSummaries, "dota", 0, listOf())
            summaryFilterHelper(shuffledSummaries, "TAT 587: Hieh", 0, listOf())
            summaryFilterHelper(shuffledSummaries, "ltural Heripage, Collection Management & Preservati", 0, listOf())
            summaryFilterHelper(shuffledSummaries, "iotechnical Informatcon Sys", 0, listOf())
            summaryFilterHelper(shuffledSummaries, "sn", 0, listOf())
            summaryFilterHelper(shuffledSummaries, "hopics", 0, listOf())
            summaryFilterHelper(shuffledSummaries, "compuqer", 0, listOf())
            summaryFilterHelper(shuffledSummaries, "opiys ", 0, listOf())
            summaryFilterHelper(shuffledSummaries, "40l:", 0, listOf())
            summaryFilterHelper(shuffledSummaries, "5p4:", 0, listOf())
            summaryFilterHelper(shuffledSummaries, "celerated Fundgmentals of Comp", 0, listOf())
            summaryFilterHelper(shuffledSummaries, "TATs", 0, listOf())
            summaryFilterHelper(shuffledSummaries, " Inforvati", 0, listOf())
            summaryFilterHelper(shuffledSummaries, "10c:", 0, listOf())
            summaryFilterHelper(shuffledSummaries, "a65:", 0, listOf())
            summaryFilterHelper(shuffledSummaries, ": Advanced Topics in Information", 3, listOf(58, 107, 157))
            summaryFilterHelper(shuffledSummaries, " 59v: ", 0, listOf())
            summaryFilterHelper(shuffledSummaries, "Race, Gendei, and ", 0, listOf())
            summaryFilterHelper(shuffledSummaries, "3:v", 0, listOf())
            summaryFilterHelper(shuffledSummaries, "storytelling", 3, listOf(85, 305, 27))
        }
    }

    // Integration tests that require simulating the entire app, and are usually slower
    @RunWith(AndroidJUnit4::class)
    @LooperMode(LooperMode.Mode.PAUSED)
    @FixMethodOrder(MethodSorters.NAME_ASCENDING)
    class IntegrationTests {
        /** Test summary view to make sure that the correct courses are displayed in the right order.  */
        @Test(timeout = 10000L)
        @Graded(points = 20, friendlyName = "Summary View")
        fun test2_SummaryView() {
            startMainActivity {
                // Check that the right number of summaries are displayed
                onView(withId(R.id.recycler_view)).check(countRecyclerView(SUMMARY_COUNT))

                // Check that full summary titles are shown, and the the order is correct
                onView(withRecyclerView(R.id.recycler_view).atPosition(0))
                    .check(matches(hasDescendant(withText("CS 100: Computer Science Orientation"))))
                onView(withRecyclerView(R.id.recycler_view).atPosition(1))
                    .check(matches(hasDescendant(withSubstring("IS 100"))))
                onView(withRecyclerView(R.id.recycler_view).atPosition(2))
                    .check(matches(hasDescendant(withSubstring("STAT 100"))))

                // Check a pair that won't sort properly just based on number
                onView(withId(R.id.recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(73))
                onView(withRecyclerView(R.id.recycler_view).atPosition(73))
                    .check(matches(hasDescendant(withSubstring("CS 403"))))
                onView(withId(R.id.recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(74))
                onView(withRecyclerView(R.id.recycler_view).atPosition(74))
                    .check(matches(hasDescendant(withSubstring("IS 403"))))

                // Check the endpoint
                onView(withId(R.id.recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(SUMMARY_COUNT - 1))
                onView(withRecyclerView(R.id.recycler_view).atPosition(SUMMARY_COUNT - 1))
                    .check(matches(hasDescendant(withText("STAT 599: Thesis Research"))))
            }
        }

        /**
         * Test search interaction to make sure that the correct courses are shown when the search
         * feature is used.
         */
        @Test(timeout = 10000L)
        @Graded(points = 20, friendlyName = "Filtered View")
        fun test3_FilteredView() {
            startMainActivity {
                // Check that the right number of courses are displayed initially
                onView(withId(R.id.recycler_view)).check(countRecyclerView(SUMMARY_COUNT))

                // Make sure blank searches work
                // Some manual delay is required for these tests to run reliably
                onView(withId(R.id.search)).perform(searchFor("  "))
                pause()
                onView(withId(R.id.recycler_view)).check(countRecyclerView(SUMMARY_COUNT))

                // Illinois has no super boring courses!
                onView(withId(R.id.search)).perform(searchFor("Super Boring Course"))
                pause()
                onView(withId(R.id.recycler_view)).check(countRecyclerView(0))

                // CS 124 should return one result
                onView(withId(R.id.search)).perform(searchFor("CS 124"))
                pause()
                onView(withId(R.id.recycler_view)).check(countRecyclerView(1))

                // study matches several courses
                onView(withId(R.id.search)).perform(searchFor("study"))
                pause()
                onView(withId(R.id.recycler_view)).check(countRecyclerView(9))
                onView(withRecyclerView(R.id.recycler_view).atPosition(2))
                    .check(matches(hasDescendant(withText("IS 189: Independent Study"))))
            }
        }
    }
}

// md5: 2cebcb7bd2754ae1c8f08bb1d2872fd3 // DO NOT REMOVE THIS LINE
