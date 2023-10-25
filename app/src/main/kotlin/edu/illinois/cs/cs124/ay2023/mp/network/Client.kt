package edu.illinois.cs.cs124.ay2023.mp.network

import android.os.Build
import android.util.Log
import com.android.volley.ExecutorDelivery
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.VolleyLog
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.NoCache
import com.android.volley.toolbox.StringRequest
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.readValue
import edu.illinois.cs.cs124.ay2023.mp.application.CourseableApplication
import edu.illinois.cs.cs124.ay2023.mp.helpers.CHECK_SERVER_RESPONSE
import edu.illinois.cs.cs124.ay2023.mp.helpers.ResultMightThrow
import edu.illinois.cs.cs124.ay2023.mp.helpers.objectMapper
import edu.illinois.cs.cs124.ay2023.mp.models.Summary
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.stream.Collectors

/**
 * Course API client.
 *
 * <p>You will add functionality to the client to complete the project.
 *
 * <p>This class also uses the singleton pattern. By declaring an object rather than a class,
 * we get one copy of this class that we can reference throughout our application.
 */
object Client {
    /** Tag to identify the Client in the logs. */
    private val logTag = Client::class.java.simpleName

    /**
     * Retrieve the list of summaries.
     *
     * @param callback the callback that will receive the result
     */
    fun getSummary(callback: (summaries: ResultMightThrow<List<Summary>>) -> Any?) {
        val request = StringRequest(
            Request.Method.GET,
            "${CourseableApplication.SERVER_URL}/summary/",
            { response: String ->
                try {
                    val summaries: List<Summary> = objectMapper.readValue(response)
                    callback(ResultMightThrow(summaries))
                } catch (e: JsonProcessingException) {
                    callback(ResultMightThrow(e))
                }
            },
            { error: VolleyError -> callback(ResultMightThrow(error)) },
        )

        requestQueue.add(request)
    }

    // You should not need to modify the code below

    /** Initial connection delay. */
    private const val INITIAL_CONNECTION_RETRY_DELAY = 1000L

    /** Max retries to connect to the server. */
    private const val MAX_STARTUP_RETRIES = 8

    /** Size of the thread pool. */
    private const val THREAD_POOL_SIZE = 1

    /** Queue for our requests. */
    private val requestQueue: RequestQueue

    /** Allow getConnected to wait for startup to complete. */
    private val _connected = CompletableFuture<Boolean>()

    /** Whether the client is connected or not. */
    val connected: Boolean
        get() {
            // Retrieve the result from the CompletableFuture
            return _connected.get()
        }

    /**
     * Object initialization.
     */
    init {
        // Whether we're in a testing configuration
        val testing = Build.FINGERPRINT == "robolectric"
        // Disable debug logging
        VolleyLog.DEBUG = false
        // Follow redirects so POST works
        HttpURLConnection.setFollowRedirects(true)

        // Configure the Volley queue used for our network requests
        val cache = NoCache()
        val network = BasicNetwork(HurlStack())
        requestQueue = if (testing) {
            RequestQueue(
                cache,
                network,
                THREAD_POOL_SIZE,
                ExecutorDelivery(Executors.newSingleThreadExecutor()),
            )
        } else {
            RequestQueue(cache, network)
        }

        // Make sure the backend URL is valid
        val serverURL = try {
            URL(CourseableApplication.SERVER_URL)
        } catch (e: MalformedURLException) {
            Log.e(logTag, "Bad server URL: " + CourseableApplication.SERVER_URL)
            throw e
        }

        // Start a background thread to establish the server connection
        Thread {
            repeat(MAX_STARTUP_RETRIES) {
                @Suppress("EmptyCatchBlock")
                try {
                    // Issue a GET request for the root URL
                    val connection = serverURL.openConnection() as HttpURLConnection
                    val body = BufferedReader(InputStreamReader(connection.inputStream))
                        .lines()
                        .collect(Collectors.joining("\n"))
                    check(body == CHECK_SERVER_RESPONSE) { "Invalid response from server" }
                    connection.disconnect()

                    // Once this succeeds, we're connected and can start the Volley queue
                    _connected.complete(true)
                    requestQueue.start()
                    return@Thread
                } catch (_: Exception) {
                }
                // If the connection fails, delay and then retry
                try {
                    Thread.sleep(INITIAL_CONNECTION_RETRY_DELAY)
                } catch (ignored: InterruptedException) {
                }
            }
            Log.e(logTag, "Client couldn't connect to server")
            error { "Could not connect to server" }
        }.start()
    }
}
