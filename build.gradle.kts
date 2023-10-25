@file:Suppress("SpellCheckingInspection", "GradleDependency", "AndroidGradlePluginVersion")

/*
 * This file configures the build system that creates your Android app.
 * The syntax is Kotlin, but it may use idioms unfamiliar to you.
 * You do not need to understand the contents of this file, nor should you modify it.
 * ALL CHANGES TO THIS FILE WILL BE OVERWRITTEN DURING OFFICIAL GRADING.
 */

plugins {
    id("com.android.application") version "8.1.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
    id("com.diffplug.spotless") version "6.22.0"
}
spotless {
    kotlin {
        ktlint("1.0.0")
            .editorConfigOverride(mapOf("max_line_length" to 120))
        target("app/src/*/kotlin/**/*.kt")
    }
    kotlinGradle {
        ktlint("1.0.0")
        target("**/*.gradle.kts")
    }
}
