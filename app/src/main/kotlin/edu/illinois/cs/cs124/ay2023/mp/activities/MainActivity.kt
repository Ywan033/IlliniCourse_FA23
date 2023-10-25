package edu.illinois.cs.cs124.ay2023.mp.activities

import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.illinois.cs.cs124.ay2023.mp.R
import edu.illinois.cs.cs124.ay2023.mp.adapters.SummaryListAdapter
import edu.illinois.cs.cs124.ay2023.mp.helpers.ResultMightThrow
import edu.illinois.cs.cs124.ay2023.mp.models.Summary
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
        title = "Search Course"

        // Setup the list adapter for the list of summaries
        listAdapter = SummaryListAdapter(summaries, this)

        // Add the list to the layout
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = listAdapter

        // Initiate a request for the summary list
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
            // Sort the list for nice initial display
            summaries = result.value!!
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
        return true
    }

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
