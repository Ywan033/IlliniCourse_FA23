package edu.illinois.cs.cs124.ay2023.mp.test.helpers

import android.view.View
import android.widget.RatingBar
import android.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import com.google.common.truth.Truth.assertWithMessage
import org.hamcrest.Matcher
import org.hamcrest.Matchers

/*
 * This file contains helper code used by the test suites.
 * You should not need to modify it.
 *
 * The helper methods in this file assist with checking UI components during testing.
 */

/** Count the number of items in a RecyclerView. */
fun countRecyclerView(expected: Int) = ViewAssertion { v: View, noViewFoundException: NoMatchingViewException? ->
    when {
        noViewFoundException != null -> assertWithMessage("Couldn't find matching view").fail()
        else -> {
            assertWithMessage("RecyclerView should have $expected items")
                .that((v as RecyclerView).adapter!!.itemCount)
                .isEqualTo(expected)
        }
    }
}

/** Search for text in a SearchView component. */
fun searchFor(query: String, submit: Boolean = false) = object : ViewAction {
    override fun getConstraints() = Matchers.allOf(ViewMatchers.isDisplayed())
    override fun getDescription() = when (submit) {
        true -> "Set query to $query and submit"
        false -> "Set query to $query but don't submit"
    }

    override fun perform(uiController: UiController, view: View) = (view as SearchView).setQuery(query, submit)
}

/** ViewAssertion for testing RatingBar components. */
fun hasRating(rating: Int): ViewAssertion {
    return ViewAssertion { view: View, exception: NoMatchingViewException? ->
        assertWithMessage("Should have found view")
            .that(exception)
            .isNull()
        assertWithMessage("View should be a RatingBar")
            .that(view)
            .isInstanceOf(RatingBar::class.java)
        val ratingBar: RatingBar = view as RatingBar
        assertWithMessage("RatingBar should have rating $rating")
            .that(ratingBar.rating)
            .isEqualTo(rating)
    }
}

/** ViewAction for modifying RatingBar components. */
fun setRating(rating: Int): ViewAction {
    return ViewActions.actionWithAssertions(
        object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return ViewMatchers.isAssignableFrom(RatingBar::class.java)
            }

            override fun getDescription(): String {
                return "Custom view action to set rating."
            }

            override fun perform(uiController: UiController, view: View) {
                val ratingBar = view as RatingBar
                ratingBar.rating = rating.toFloat()
            }
        },
    )
}

// md5: 475694d662a3e0893498135fa7fcd66e // DO NOT REMOVE THIS LINE
