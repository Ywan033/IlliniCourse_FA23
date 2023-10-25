/*
 * This file configures the build system that creates your Android app.
 * The syntax is Kotlin, but it may use idioms unfamiliar to you.
 * You do not need to understand the contents of this file, nor should you modify it.
 * ALL CHANGES TO THIS FILE WILL BE OVERWRITTEN DURING OFFICIAL GRADING.
 */

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

rootProject.name = "AY2023-MP-Kotlin"
include(":app")
