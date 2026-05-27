dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
    }
}

plugins {
    // Use the Foojay Toolchains plugin to automatically download JDKs required by subprojects.
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

// Learn more about structuring projects with Gradle - https://docs.gradle.org/8.7/userguide/multi_project_builds.html
include(":server")
include(":dto")

rootProject.name = "mafia-companion-api"
