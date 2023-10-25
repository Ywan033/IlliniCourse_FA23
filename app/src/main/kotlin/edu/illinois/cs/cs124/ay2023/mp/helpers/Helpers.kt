package edu.illinois.cs.cs124.ay2023.mp.helpers

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

/**
 * Helper file holding a few broadly-useful items.
 */

// Jackson instance for serialization and deserialization, configured to support Kotlin
val objectMapper = jacksonObjectMapper().apply {
    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
}

// Magic server response used by the client to determine that it's properly connected
const val CHECK_SERVER_RESPONSE = "AY2023"
