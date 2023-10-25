package edu.illinois.cs.cs124.ay2023.mp.test.helpers

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.common.truth.Truth.assertWithMessage
import edu.illinois.cs.cs124.ay2023.mp.models.Summary
import okhttp3.internal.toImmutableList
import java.math.BigInteger
import java.security.MessageDigest

/*
 * This file contains helper code used by the test suites.
 * You should not need to modify it.
 *
 * The helper methods in this file assist with loading course data for testing.
 */

// Object mapper used by the test suites
internal val objectMapper = jacksonObjectMapper().apply {
    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
}

// Fingerprint of the courses.json file
private const val COURSES_FINGERPRINT = "25d6470e35976403cb1f37163a3f948a"

// Load courses.json into a String and check the fingerprint to make sure it hasn't been modified
private var coursesJSON = object {}.javaClass.getResource("/courses.json")!!.readText().also { contents ->
    val cleanContents = contents.lines().joinToString("\n") { it.trimEnd() }
    val currentFingerprint = BigInteger(1, MessageDigest.getInstance("MD5").digest(cleanContents.toByteArray()))
        .toString(16)
        .padStart(32, '0')
    check(currentFingerprint == COURSES_FINGERPRINT) {
        "courses.json has been modified. Please restore the original version of the file."
    }
}

// Load summaries only from courses.json
internal val SUMMARIES = coursesJSON.let { contents ->
    objectMapper.readTree(contents).map { node ->
        // Use only summary fields
        val summary: ObjectNode = objectMapper.createObjectNode().apply {
            set<JsonNode>("subject", node["subject"])
            set<JsonNode>("number", node["number"])
            set<JsonNode>("label", node["label"])
        }
        objectMapper.readValue<Summary>(summary.toPrettyString())
    }.toImmutableList()
}

// Number of summaries we expect
internal val SUMMARY_COUNT = SUMMARIES.size

// Load full course information from courses.json
internal val COURSES = coursesJSON.let { contents ->
    objectMapper.readTree(contents).map { node -> node.toPrettyString()!! }.toImmutableList()
}

// Helper method to convert a JsonNode containing a summary into a backend API path suffix
fun JsonNode.toPath() = "${this["subject"].asText()}/${this["number"].asText()}"

// Helper method to compare two Strings containing full course information as JSON
fun compareCourses(expectedString: String?, foundString: String?) {
    try {
        val expected = objectMapper.readTree(expectedString)
        val found = objectMapper.readTree(foundString)
        for (component in mutableListOf("subject", "number", "label", "description")) {
            assertWithMessage("Summary $component is incorrect")
                .that(found[component].asText())
                .isEqualTo(expected[component].asText())
        }
    } catch (e: JsonProcessingException) {
        assertWithMessage("Deserialization failed: " + e.message).fail()
        throw e
    }
}

// md5: 88ea1b8aab822055a4894552795c6840 // DO NOT REMOVE THIS LINE
