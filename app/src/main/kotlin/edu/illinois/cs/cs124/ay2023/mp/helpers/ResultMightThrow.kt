package edu.illinois.cs.cs124.ay2023.mp.helpers

/**
 * Helper class for wrapping exceptions thrown by another thread.
 *
 * <p>Allows the main thread to retrieve exceptions thrown by the API client, rather than having
 * them be thrown on another thread.
 * See use in MainActivity.kt and Client.kt.
 *
 * @param <T> type of the valid result
 */
class ResultMightThrow<T> {
    val exception: Exception?
    val value: T?
        get() {
            if (exception != null) {
                throw exception
            }
            return field
        }

    constructor(setResult: T) {
        value = setResult
        exception = null
    }

    constructor(setException: Exception?) {
        value = null
        exception = setException
    }
}
