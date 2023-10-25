package edu.illinois.cs.cs124.ay2023.mp.application

import android.app.Application
import android.os.Build
import edu.illinois.cs.cs124.ay2023.mp.network.startServer

/**
 * Application class for the Courseable app.
 *
 * <p>Starts the development server and creates the course API client.
 */
class CourseableApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Start the API server
        if (Build.FINGERPRINT == "robolectric") {
            startServer()
        } else {
            // In a new thread if we're not testing
            Thread { startServer() }.start()
        }
    }

    companion object {
        /** Course API server port. You can change this if needed.  */
        const val DEFAULT_SERVER_PORT = 8023

        /** Course API server URL.  */
        const val SERVER_URL = "http://localhost:$DEFAULT_SERVER_PORT"
    }
}
