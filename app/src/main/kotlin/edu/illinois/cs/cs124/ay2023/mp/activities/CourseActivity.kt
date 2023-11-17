package edu.illinois.cs.cs124.ay2023.mp.activities
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import edu.illinois.cs.cs124.ay2023.mp.R
import edu.illinois.cs.cs124.ay2023.mp.helpers.objectMapper
import edu.illinois.cs.cs124.ay2023.mp.models.Summary
import edu.illinois.cs.cs124.ay2023.mp.network.Client

class CourseActivity : AppCompatActivity() {
    override fun onCreate(unused: Bundle?) {
        // load my layout
        // set up the UI, (title and description)
        // Retrieve the intent and deserialize
        // make a request to retrieve the course details using the client
        // when the request complete, update UI by using
        // runOnUiThread {   }

        super.onCreate(unused)
        // load my layout:
        setContentView(R.layout.activity_course)

        // set up the UI:
        // the test suite will test that you displace both the title which you can get via two string and description
        val descriptionTextView: TextView = findViewById(R.id.description)

        // retrieve intent:
        val retrievedIntent = intent
        // retrieve the intent summary from previous:
        val retrievedSummary = retrievedIntent.getStringExtra("summary")
        // deserialize it:
        val newsummary: Summary = try {
            objectMapper.readValue(retrievedSummary, Summary::class.java)
        } catch (e: Exception) {
            Log.e("CourseActivty", "ERROR during deserialization")
            throw e
        }
        // make a request to retrieve the course details using the client:
        Client.getCourse(newsummary) { result ->
            runOnUiThread {
                try {
                    if (result.value != null) {
                        val updatedCourse = result.value!!
                        val title = "${updatedCourse.subject} ${updatedCourse.number}: ${updatedCourse.label} " +
                            updatedCourse.description
                        descriptionTextView.text = title
                    } else {
                        Log.e("CourseActivity", "ERROR HERE!!!")
                    }
                } catch (e: Exception) {
                    Log.e("CourseActivity", "ERROR during updating UI")
                }
            }
        }
    }
}
