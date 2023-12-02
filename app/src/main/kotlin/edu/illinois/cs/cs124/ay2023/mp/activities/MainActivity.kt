package edu.illinois.cs.cs124.ay2023.mp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.illinois.cs.cs124.ay2023.mp.R
import edu.illinois.cs.cs124.ay2023.mp.adapters.SummaryListAdapter
import edu.illinois.cs.cs124.ay2023.mp.helpers.ResultMightThrow
import edu.illinois.cs.cs124.ay2023.mp.helpers.objectMapper
import edu.illinois.cs.cs124.ay2023.mp.models.Summary
import edu.illinois.cs.cs124.ay2023.mp.models.filter
import edu.illinois.cs.cs124.ay2023.mp.network.Client

/** Main activity showing the course summary list. */
class MainActivity :
    AppCompatActivity(),
    SearchView.OnQueryTextListener {

    /** Tag to identify the MainActivity in the logs. */
    @Suppress("unused")
    private val logTag = MainActivity::class.java.simpleName

    /** List of summaries received from the server, initially empty. */
    private var summaries = listOf<Summary>()

    /**
     * Adapter that connects our list of summaries with the list displayed on the display.
     * lateinit vars do not need to be initialized when declared but must be initialized before being read.
     * */
    private lateinit var listAdapter: SummaryListAdapter

    /**
     * Called when this activity is created.
     *
     * <p>This method is called when the activity is first launched, and at points later if the app is terminated to
     * save memory. For more details, see consult the Android activity lifecycle documentation.
     *
     * @param unused saved instance state, currently unused and always empty or null
     */
    override fun onCreate(unused: Bundle?) {
        super.onCreate(unused)

        // Load this activity's layout and set the title
        setContentView(R.layout.activity_main)
        title = "Search Courses"

        // Setup the list adapter for the list of summaries
        listAdapter = SummaryListAdapter(summaries, this) { summary ->
            val intent = Intent(this, CourseActivity::class.java)
            // add information to the intent and use the field summary
            // convert this object into a string containing the information
            // do this by using serialization
            // we have object "summary" and we want to convert it to string by serialization
            // then, stick it into the intent

            val convertedString = try {
                objectMapper.writeValueAsString(summary)
            } catch (e: Exception) {
                Log.e(logTag, "ERROR HAPPEN DURING SERIALIZATION!!!")
                throw e
            }
            // add the serialization string to the intent
            intent.putExtra("summary", convertedString)
            startActivity(intent)
        }

        // Add the list to the layout
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = listAdapter

        // Initiate a request for the summary list
        Log.d("DataFetch", "MainActivity calling getSummary")
        Client.getSummary(summaryCallback)

        // Register this component as a callback for changes to the search view component shown above
        // the summary list. We'll use these events to initiate summary list filtering.
        findViewById<SearchView>(R.id.search).setOnQueryTextListener(this)

        // Register our toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
    }

    /** Callback used to update the list of summaries during onCreate. */
    private val summaryCallback = { result: ResultMightThrow<List<Summary>> ->
        try {
            Log.d("DataFetch", "Client returned data")
            // Sort the list for nice initial display
            summaries = result.value!!.sorted() // might be nice to sort the list here..
            listAdapter.summaries = summaries
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(logTag, "getSummary threw an exception: $e")
        }
    }

    /**
     * Callback fired when the user edits the text in the search query box.
     *
     * <p>This fires every time the text in the search bar changes. We'll handle this by updating the
     * list of visible summaries.
     *
     * @param query the text to use to filter the summary list
     * @return true because we handled the action
     */
    override fun onQueryTextChange(query: String): Boolean {
        // TODO
        // Log.d("SearchBar", "User entered: $query")

        // filter the list
        // filter the search bar by user's input and update the list shown to the user
        try {
            val filteredQuery = summaries.filter(query)
            listAdapter.summaries = filteredQuery
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(logTag, "Error occurred during filtering: $e")
        }
        return true
    }

    // request rating for the course and display that as well
    // rating bar needs a way to notify the activity when the rating has been changed.
    // when add that you will be run code when the rating has been changed
    // and that is the place where you are going to use that client postRating method to move that rating to the server

    /**
     * Callback fired when the user submits a search query.
     *
     * <p>This would correspond to them hitting enter or a submit button. Because we update the list
     * on each change to the search value, we do not handle this callback.
     *
     * @param unused current query text
     * @return false because we did not handle this action
     */
    override fun onQueryTextSubmit(unused: String) = false
}
