package edu.illinois.cs.cs124.ay2023.mp.network

import com.fasterxml.jackson.module.kotlin.readValue
import edu.illinois.cs.cs124.ay2023.mp.application.CourseableApplication
import edu.illinois.cs.cs124.ay2023.mp.helpers.CHECK_SERVER_RESPONSE
import edu.illinois.cs.cs124.ay2023.mp.helpers.objectMapper
import edu.illinois.cs.cs124.ay2023.mp.models.Summary
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import java.io.IOException
import java.net.HttpURLConnection
import java.util.Locale
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Development course API server.
 *
 * Normally you would run this server on another machine, which the client would connect to over
 * the internet. For the sake of development, we're running the server alongside the app on
 * the same device. However, all communication between the course API client and course API server
 * is still done using the HTTP protocol. Meaning that it would be straightforward to
 * move this code to an actual server, which could provide data for all course API clients.
 *
 * The server uses Kotlin's singleton pattern, allowing us to declare a top-level object that will
 * be created once it is referenced. Kotlin will ensure that only one instance of this class exists
 * at any given time.
 */
object Server : Dispatcher() {
    /** List of summaries as a JSON string. */
    private val summariesJSON: String

    /** Return the JSON with the list of course summaries. */
    private fun getSummaries(): MockResponse {
        return summariesJSON.makeOKJSONResponse()
    }

    /**
     * HTTP request dispatcher.
     *
     * This method receives HTTP requests from clients and determines how to handle them, based on the request path
     * and method.
     */
    override fun dispatch(request: RecordedRequest): MockResponse {
        // Reject requests without a path or method
        if (request.path == null || request.method == null) {
            return httpBadRequest
        }

        return try {
            // Normalize trailing slashes and method
            val path = request.path!!.replace("/+".toRegex(), "/")
            val method = request.method!!.uppercase(Locale.getDefault())

            // Main dispatcher tree
            when {
                // Used by API client to validate server
                path == "/" && method == "GET" ->
                    MockResponse().setBody(CHECK_SERVER_RESPONSE).setResponseCode(HttpURLConnection.HTTP_OK)

                // Used to reset the server during testing
                path == "/reset/" && method == "GET" -> {
                    MockResponse().setBody("200: OK").setResponseCode(HttpURLConnection.HTTP_OK)
                }

                path == "/summary/" && method == "GET" -> getSummaries()

                // Default is not found
                else -> httpNotFound
            }
        } catch (e: Exception) {
            // Log an error and return 500 if an exception is thrown
            e.printStackTrace()
            MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR).setBody("500: Internal Error")
        }
    }

    /**
     * Initialize the API server.
     */
    init {
        // Disable server logging, since this is a bit verbose
        Logger.getLogger(MockWebServer::class.java.name).level = Level.OFF

        // Load data used by the server
        val json = Server::class.java.getResource("/courses.json")!!.readText()

        // Iterate through the list of JsonNodes returned by deserialization
        val summaries = mutableListOf<Summary>()
        for (node in objectMapper.readTree(json)) {
            // Deserialize as Summary and add to the list
            val summary = objectMapper.readValue<Summary>(node.toString())
            summaries += summary
        }
        // Convert the List<Summary> to a String and save it
        summariesJSON = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(summaries)

        val server = MockWebServer().apply { dispatcher = this@Server }
        server.start(CourseableApplication.DEFAULT_SERVER_PORT)
    }
}

/**
 * Determine if the server is currently running.
 *
 * @param wait whether to wait or not
 * @param retryCount how many retries
 * @param retryDelay delay between retries
 * @return whether the server is running or not
 * @throws IllegalStateException if something else is running on our port
 */
fun isRunning(wait: Boolean = true, retryCount: Int = 8, retryDelay: Long = 512): Boolean {
    repeat(retryCount) {
        val client = OkHttpClient()
        val request: Request = Request.Builder().url(CourseableApplication.SERVER_URL).get().build()
        try {
            val response = client.newCall(request).execute()
            check(response.isSuccessful)
            check(response.body?.string() == CHECK_SERVER_RESPONSE) {
                "Another server is running on ${CourseableApplication.DEFAULT_SERVER_PORT}"
            }
            return true
        } catch (ignored: IOException) {
            if (!wait) {
                return false
            }
            try {
                Thread.sleep(retryDelay)
            } catch (ignored1: InterruptedException) {
            }
        }
    }
    return false
}

/**
 * Start the server if has not already been started, and wait for startup to finish.
 *
 * Done in a separate thread to avoid blocking the UI.
 */
fun startServer() {
    if (isRunning(false)) {
        return
    }
    Server
    check(isRunning()) { "Server should be running" }
}

/**
 * Reset the server. Used to reset the server between tests.
 */
@Suppress("unused")
fun resetServer(): Boolean {
    val client = OkHttpClient()
    val request = Request.Builder().url("${CourseableApplication.SERVER_URL}/reset/").get().build()
    client.newCall(request).execute().use { response ->
        return response.isSuccessful
    }
}

/** Helper method to create a 200 HTTP response with a body. */
private fun String.makeOKJSONResponse(): MockResponse = MockResponse()
    .setResponseCode(HttpURLConnection.HTTP_OK)
    .setBody(this)
    .setHeader("Content-Type", "application/json; charset=utf-8")

/** Helper value storing a 404 Not Found response. */
private val httpNotFound = MockResponse()
    .setResponseCode(HttpURLConnection.HTTP_NOT_FOUND)
    .setBody("404: Not Found")

/** Helper value storing a 400 Bad Request response. */
private val httpBadRequest = MockResponse()
    .setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST)
    .setBody("400: Bad Request")
