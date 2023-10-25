package edu.illinois.cs.cs124.ay2023.mp.test.helpers

import com.fasterxml.jackson.databind.JsonNode
import com.google.common.truth.Truth.assertWithMessage
import edu.illinois.cs.cs124.ay2023.mp.application.CourseableApplication
import edu.illinois.cs.cs124.ay2023.mp.helpers.ResultMightThrow
import edu.illinois.cs.cs124.ay2023.mp.network.Client
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.HttpURLConnection
import java.util.concurrent.CompletableFuture

/*
 * This file contains helper code used by the test suites.
 * You should not need to modify it.
 *
 * The helper methods in this file assist with testing the API server and client.
 */

// Private HTTP client for testing
private val httpClient = OkHttpClient()

/** Test a GET call to the backend API server .*/
fun <T> String.testServerGet(
    responseCode: Int = HttpURLConnection.HTTP_OK,
    responseClass: Class<*>? = JsonNode::class.java,
): T? {
    // Create the request
    val request = Request.Builder().url(CourseableApplication.SERVER_URL + this).build()

    // .use ensures the response is cleaned up properly.
    httpClient.newCall(request).execute().use { response ->

        if (responseCode == 200) {
            // The request should have succeeded
            assertWithMessage("GET request for $this should have succeeded")
                .that(response.isSuccessful)
                .isTrue()
        } else {
            // The request should have failed with the correct code
            assertWithMessage("GET request for $this should have failed with code $responseCode")
                .that(response.code)
                .isEqualTo(responseCode)
            return null
        }

        // The response body should not be null
        val body = response.body
        assertWithMessage("GET response for $this body should not be null").that(body).isNotNull()

        // Deserialize based on type passed to the method
        @Suppress("UNCHECKED_CAST")
        return when (responseClass) {
            JsonNode::class.java -> objectMapper.readTree(body!!.string()) as T
            else -> objectMapper.readValue(body!!.string(), responseClass) as T
        }
    }
}

// testServerGet overrides
fun <T> String.testServerGet(responseClass: Class<*>) = testServerGet<T>(HttpURLConnection.HTTP_OK, responseClass)

fun String.testServerGet(responseCode: Int = HttpURLConnection.HTTP_OK) = testServerGet<Nothing>(responseCode)

/** Test a POST to the backend API server. */
fun <T> String.testServerPost(
    responseCode: Int = HttpURLConnection.HTTP_OK,
    requestBody: Any,
    responseClass: Class<*>? = JsonNode::class.java,
): T? {
    // Create the request
    val request = Request.Builder().url(CourseableApplication.SERVER_URL + this)
        .post(objectMapper.writeValueAsString(requestBody).toRequestBody("application/json".toMediaTypeOrNull()))
        .build()

    // .use ensures the response is cleaned up properly.
    httpClient.newCall(request).execute().use { response ->

        if (responseCode == HttpURLConnection.HTTP_OK) {
            // The request should have succeeded
            assertWithMessage("POST request for $this should have succeeded")
                .that(response.code)
                .isEqualTo(HttpURLConnection.HTTP_OK)
        } else {
            // The request should have failed with the correct code
            assertWithMessage("POST request for $this should have failed with code $responseCode")
                .that(response.code)
                .isEqualTo(responseCode)
            return null
        }

        // The response body should not be null
        val body = response.body
        assertWithMessage("POST response for $this body should not be null").that(body).isNotNull()

        // Deserialize based on type passed to the method
        @Suppress("UNCHECKED_CAST")
        return when (responseClass) {
            JsonNode::class.java -> objectMapper.readTree(body!!.string()) as T
            else -> objectMapper.readValue(body!!.string(), responseClass) as T
        }
    }
}

// testServerPost overrides
fun <T> String.testServerPost(requestBody: Any, responseClass: Class<*>? = JsonNode::class.java) =
    testServerPost<T>(HttpURLConnection.HTTP_OK, requestBody, responseClass)

fun String.testServerPost(requestBody: Any, responseCode: Int = HttpURLConnection.HTTP_OK) =
    testServerPost<Nothing>(responseCode, requestBody)

/** Helper method for API client testing. */
fun <T> testClient(method: (callback: (result: ResultMightThrow<T>) -> Any?) -> Any?): T {
    // Ensure the client started up properly
    assertWithMessage("Client should be connected").that(Client.connected).isTrue()

    // A CompletableFuture allows us to wait for the result of an asynchronous call
    val completableFuture = CompletableFuture<ResultMightThrow<T>>()

    // When the client call returns, it causes the CompletableFuture to complete
    method.invoke { value: ResultMightThrow<T> ->
        completableFuture.complete(
            value,
        )
    }

    // Wait for the CompletableFuture to complete
    val result = completableFuture.get()

    // Throw if the call threw
    if (result.exception != null) {
        throw result.exception!!
    }

    // Shouldn't ever happen, but doesn't hurt to check
    assertWithMessage("Client call expected to succeed returned null")
        .that(result.value)
        .isNotNull()
    return result.value!!
}

// md5: 1e0bd9685580919882275bc88deb3199 // DO NOT REMOVE THIS LINE
